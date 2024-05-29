package se.kth.assrt.proze.generate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "java -jar <proze-jar-with-dependencies-version>.jar",
        description = "Proze produces parameterized tests",
        version = "1.0",
        mixinStandardHelpOptions = true,
        usageHelpWidth = 200)
public class GenerateMain implements Callable<Integer> {
  @CommandLine.Parameters(
          index = "0",
          description = "The path to the target Maven project")
  private Path projectPath;

  @CommandLine.Parameters(
          index = "1",
          description = "The path to the json report with prod and test data")
  private Path analysisReportPath;

  private static final Logger logger = LoggerFactory.getLogger(GenerateMain.class);

  @Override
  public Integer call() throws Exception {
    List<TargetMethod> targetMethods = ParseAnalysisReport.parseReport(analysisReportPath);
    logger.info(String.format("Found invocation data for %s methods", targetMethods.size()));
    if (!targetMethods.isEmpty()) {
      new GenerateLauncher(projectPath).processWithSpoon(targetMethods);
    }
    return null;
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new GenerateMain()).execute(args);
    System.exit(exitCode);
  }
}
