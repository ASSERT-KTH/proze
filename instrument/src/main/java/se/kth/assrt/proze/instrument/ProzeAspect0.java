package se.kth.assrt.proze.instrument;

import com.google.gson.Gson;
import org.glowroot.agent.plugin.api.*;
import org.glowroot.agent.plugin.api.weaving.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ProzeAspect0 {
  private static int INVOCATION_COUNT;

  @Pointcut(className = "fully.qualified.path.to.class",
          methodName = "methodToInstrument",
          methodParameterTypes = {"param1", "param2"},
          timerName = "Timer - name")
  public static class TargetMethodAdvice {
    private static final TimerName timer = Agent.getTimerName(TargetMethodAdvice.class);
    private static final String transactionType = "Target";
    private static final int COUNT = 0;
    private static Logger logger = Logger.getLogger(TargetMethodAdvice.class);
    private static final String methodParamTypesString = String.join(",",
            TargetMethodAdvice.class.getAnnotation(Pointcut.class).methodParameterTypes());
    private static final String postfix = methodParamTypesString.isEmpty() ? "" : "_" + methodParamTypesString;
    public static final String methodFQN = TargetMethodAdvice.class.getAnnotation(Pointcut.class).className() + "."
            + TargetMethodAdvice.class.getAnnotation(Pointcut.class).methodName() + postfix;
    private static final String methodSimpleName = TargetMethodAdvice.class.getAnnotation(
            Pointcut.class).methodName();
    static MethodInvocation methodInvocation = new MethodInvocation();

    private static List<String> testMethodsThatCallThisMethod = List.of();
    static final Gson gson = new Gson();

    private static void setup() {
    }

    private synchronized static void writeObjectToFile(
            MethodInvocation invocationToSerialize, String fileToSerializeIn) {
      try {
        String storageDir = "/tmp/proze-object-data/";
        Files.createDirectories(Paths.get(storageDir));
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

    @IsEnabled
    public static boolean enableProfileCollection() {
      INVOCATION_COUNT++;
      setup();
      return true;
    }

    @OnBefore
    public static TraceEntry onBefore(OptionalThreadContext context,
                                      @BindReceiver Object receivingObject,
                                      @BindParameterArray Object parameterObjects,
                                      @BindMethodName String methodName) {
      methodInvocation.setInvocationCount(INVOCATION_COUNT);
      methodInvocation.setParameters(parameterObjects);
      methodInvocation.setStackTrace(Arrays.toString(Thread.currentThread().getStackTrace()));
      MessageSupplier messageSupplier = MessageSupplier.create(
              "className: {}, methodName: {}",
              TargetMethodAdvice.class.getAnnotation(Pointcut.class).className(),
              methodName
      );
      return context.startTransaction(transactionType, methodName, messageSupplier, timer,
              OptionalThreadContext.AlreadyInTransactionBehavior.CAPTURE_NEW_TRANSACTION);
    }

    @OnReturn
    public static void onReturn(@BindReturn Object returnedObject,
                                @BindTraveler TraceEntry traceEntry) {
      writeObjectToFile(methodInvocation, methodSimpleName + ".json");
      traceEntry.end();
    }

    @OnThrow
    public static void onThrow(@BindThrowable Throwable throwable,
                               @BindTraveler TraceEntry traceEntry) {
      traceEntry.endWithError(throwable);
    }
  }
}
