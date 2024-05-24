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
      // Limit data collection to 2MB
      return new File(fileToSerializeIn).length() < 2000000L;
    }

    @OnBefore
    public static TraceEntry onBefore(@BindParameterArray Object parameterObjects) {
      String[] parameterTypes = TargetMethodAdvice.class.getAnnotation(Pointcut.class)
              .methodParameterTypes();
      methodInvocation.setParameters((Object[]) parameterObjects, parameterTypes);
      StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
      String stackTrace = Arrays.toString(stackTraceElements);
      boolean calledByInvokingTest = testMethodsThatCallThisMethod.stream().anyMatch(stackTrace::contains);
      methodInvocation.setCalledByInvokingTest(calledByInvokingTest);
      String invokingTest = "";
      if (calledByInvokingTest) {
        for (StackTraceElement e : stackTraceElements) {
          if (testMethodsThatCallThisMethod.stream()
                  .anyMatch(t -> t.equals(e.getClassName() + "." + e.getMethodName()))) {
            invokingTest = e.getClassName() + "." + e.getMethodName();
            break;
          }
        }
      }
      methodInvocation.setInvokingTest(invokingTest);
      methodInvocation.setStackTrace(stackTrace);
      return null;
    }

    @OnReturn
    public static void onReturn() {
      writeObjectToFile(methodInvocation);
    }
  }
}
