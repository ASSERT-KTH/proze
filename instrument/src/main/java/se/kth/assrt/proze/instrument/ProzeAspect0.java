package se.kth.assrt.proze.instrument;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.glowroot.agent.plugin.api.*;
import org.glowroot.agent.plugin.api.weaving.*;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProzeAspect0 {
  static final Gson gson = new GsonBuilder()
          .serializeSpecialFloatingPointValues().create();

  /**
   * <a href="https://github.com/glowroot/glowroot/blob/main/agent/plugin-api/src/main/java/org/glowroot/agent/plugin/api/weaving/Pointcut.java">...</a>
   */
  @Pointcut(className = "fully.qualified.path.to.class",
          methodName = "methodToInstrument",
          methodParameterTypes = {"param1", "param2"},
          timerName = "Timer - name")
  public static class TargetMethodAdvice implements AdviceTemplate {
    private static final boolean saveDataForOriginalTestsOnly = false;
    private static final int COUNT = 0;
    private static Logger logger = Logger.getLogger(TargetMethodAdvice.class);
    private static final String methodParamTypesString = String.join(",",
            TargetMethodAdvice.class.getAnnotation(Pointcut.class).methodParameterTypes());
    private static final String postfix = methodParamTypesString.isEmpty() ? "" : "_" + methodParamTypesString;
    private static final String classNameMethodName = TargetMethodAdvice.class.getAnnotation(Pointcut.class).className() + "."
            + TargetMethodAdvice.class.getAnnotation(Pointcut.class).methodName();
    private static final String methodFQN = TargetMethodAdvice.class.getAnnotation(Pointcut.class).className() + "."
            + TargetMethodAdvice.class.getAnnotation(Pointcut.class).methodName()
            .replaceAll("<", "").replaceAll(">", "") + postfix;
    static MethodInvocation methodInvocation = new MethodInvocation();
    private static String fileToSerializeIn = storageDir + methodFQN + ".json";

    private static List<String> testMethodsThatCallThisMethod = List.of("fully.qualified.names.of.testclasses.and.test.methods");

    private synchronized static void writeObjectToFile(
            MethodInvocation invocationToSerialize) {
      try {
        FileWriter objectFileWriter = new FileWriter(
                fileToSerializeIn, true);
        String json = gson.toJson(invocationToSerialize);
        BufferedReader reader = new BufferedReader(new StringReader(json));
        BufferedWriter writer = new BufferedWriter(objectFileWriter);
        while ((json = reader.readLine()) != null) {
          writer.write(json);
          writer.newLine();
        }
        writer.flush();
        writer.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @IsEnabled
    public static boolean enableProfileCollection() {
      // logger.info(String.format("ProzeAspect %s: %s", COUNT, classNameMethodName));
      AdviceTemplate.setup();
      // Save arguments only if method directly invoked by one of the invoking tests
      if (saveDataForOriginalTestsOnly) {
        return true;
      }
      // Otherwise limit data collection to 3MB
      return new File(fileToSerializeIn).length() < 3000000L;
    }

    @OnBefore
    public static TraceEntry onBefore(@BindParameterArray Object parameterObjects) {
      String[] parameterTypes = TargetMethodAdvice.class.getAnnotation(Pointcut.class)
              .methodParameterTypes();
      methodInvocation.setParameters((Object[]) parameterObjects, parameterTypes);
      StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
      String stackTrace = Arrays.toString(stackTraceElements);
      boolean invokingTestInStack = testMethodsThatCallThisMethod.stream().anyMatch(stackTrace::contains);
      // test is not in stack
      if (!invokingTestInStack) {
        methodInvocation.setCalledByInvokingTest(false);
        methodInvocation.setMethodTestDistance(0);
        methodInvocation.setInvokingTest("");
      }
      // test is in stack
      String invokingTest = "";
      int distanceBetweenMethodAndTest = 0;
      int methodIndex = 0;
      int testIndex = 0;
      if (invokingTestInStack) {
        for (int i = 0; i < stackTraceElements.length; i++) {
          if (methodIndex > 0 && testIndex > 0)
            break;
          String classNameMethodNameInStack = stackTraceElements[i].getClassName()
                  + "." + stackTraceElements[i].getMethodName();
          if (classNameMethodNameInStack.equals(classNameMethodName)) {
            methodIndex = i;
          }
          if (testMethodsThatCallThisMethod.stream().anyMatch(
                  t -> t.equals(classNameMethodNameInStack))) {
            testIndex = i;
            invokingTest = classNameMethodNameInStack;
          }
        }
        distanceBetweenMethodAndTest = testIndex - methodIndex;
        methodInvocation.setCalledByInvokingTest(distanceBetweenMethodAndTest == 1);
        methodInvocation.setMethodTestDistance(distanceBetweenMethodAndTest);
        methodInvocation.setInvokingTest(invokingTest);
      }
      methodInvocation.setStackTrace(stackTrace);
      return null;
    }

    @OnReturn
    public static void onReturn() {
      if (saveDataForOriginalTestsOnly) {
        if (methodInvocation.isCalledByInvokingTest())
          writeObjectToFile(methodInvocation);
      }
      else writeObjectToFile(methodInvocation);
    }
  }
}
