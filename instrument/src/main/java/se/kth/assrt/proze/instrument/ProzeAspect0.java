package se.kth.assrt.proze.instrument;

import com.google.gson.Gson;
import org.glowroot.agent.plugin.api.*;
import org.glowroot.agent.plugin.api.weaving.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

public class ProzeAspect0 {
  private static int INVOCATION_COUNT;

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
    private static final String methodFQN = TargetMethodAdvice.class.getAnnotation(Pointcut.class).className() + "."
            + TargetMethodAdvice.class.getAnnotation(Pointcut.class).methodName()
            .replaceAll("<", "").replaceAll(">", "") + postfix;
    static MethodInvocation methodInvocation = new MethodInvocation();

    private static List<String> testMethodsThatCallThisMethod = List.of("fully.qualified.names.of.testclasses.and.test.methods");

    static final Gson gson = new Gson();

    private synchronized static void writeObjectToFile(
            MethodInvocation invocationToSerialize, String fileToSerializeIn) {
      try {
        FileWriter objectFileWriter = new FileWriter(
                storageDir + fileToSerializeIn, true);
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

    private static boolean isCalledByTest() {
      return Arrays.stream(Thread.currentThread().getStackTrace()).limit(10).anyMatch(e ->
              testMethodsThatCallThisMethod.contains(e.getClassName() + "." + e.getMethodName()));
    }

    @IsEnabled
    public static boolean enableProfileCollection() {
      INVOCATION_COUNT++;
      AdviceTemplate.setup();
      return INVOCATION_COUNT <= 1000;
    }

    @OnBefore
    public static TraceEntry onBefore(@BindParameterArray Object parameterObjects) {
      methodInvocation.setInvocationCount(INVOCATION_COUNT);
      String[] parameterTypes = TargetMethodAdvice.class.getAnnotation(Pointcut.class)
              .methodParameterTypes();
      methodInvocation.setParameters((Object[]) parameterObjects, parameterTypes);
      methodInvocation.setCalledByInvokingTest(isCalledByTest());
      methodInvocation.setStackTrace(Arrays.toString(Thread.currentThread().getStackTrace()));
      return null;
    }

    @OnReturn
    public static void onReturn() {
      writeObjectToFile(methodInvocation, methodFQN + ".json");
    }
  }
}
