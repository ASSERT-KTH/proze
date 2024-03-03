package se.kth.assrt.proze.generate;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParseAnalysisReport {
  static String sanitizeFullMethodSignature(String fullMethodSignature) {
    return fullMethodSignature.replace("_", "(") + ")";
  }
  static List<TargetMethod> parseReport(Path reportPath) {
    List<TargetMethod> targetMethods = new ArrayList<>();
    try (Reader reader = Files.newBufferedReader(reportPath, StandardCharsets.UTF_8)) {
      Gson gson = new Gson();
      Type listOfStrings = new TypeToken<ArrayList<String>>() {}.getType();

      JsonArray methodArray = gson.fromJson(reader, JsonArray.class);
      for (JsonElement method : methodArray) {
        TargetMethod thisMethod = new TargetMethod();
        // fully.qualified.name(param1,param2)
        thisMethod.setFullMethodSignature(sanitizeFullMethodSignature(
                method.getAsJsonObject().get("fullMethodSignature").getAsString()));

        thisMethod.setDeclaringType(method.getAsJsonObject().get("declaringType").getAsString());
        thisMethod.setMethodName(method.getAsJsonObject().get("methodName").getAsString());
        // parameter types for this method
        List<String> parameters = gson.fromJson(method.getAsJsonObject()
                .get("parameters"), listOfStrings);
        thisMethod.setParameters(parameters);
        // list of tests that method is invoked by
        List<String> invokedByTests = gson.fromJson(method.getAsJsonObject()
                .get("invokedByTests"), listOfStrings);
        thisMethod.setInvokedByTests(invokedByTests);
        // union of production and test arguments
        List<String> unionProdAndTestArgs = gson.fromJson(method.getAsJsonObject()
                .get("unionProdAndTestArgs"), listOfStrings);
        thisMethod.setUnionProdAndTestArgs(unionProdAndTestArgs);
        // map of test and arguments for this method
        List<Map<String, String>> testArgs = new ArrayList<>();
        JsonArray testArgArray = method.getAsJsonObject().get("testArgs").getAsJsonArray();
        for (int i = 0; i < testArgArray.size(); i++) {
          Map<String, String> testAndArg = new LinkedHashMap<>();
          testAndArg.put(testArgArray.get(i).getAsJsonObject().get("test").getAsString(),
                  testArgArray.get(i).getAsJsonObject().get("arguments").getAsString());
          testArgs.add(testAndArg);
        }
        thisMethod.setTestArgs(testArgs);
        targetMethods.add(thisMethod);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
    return targetMethods;
  }
}
