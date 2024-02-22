package se.kth.assrt.proze.select;

import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "java -jar <proze-jar-with-dependencies-version>.jar",
        description = "Proze produces parameterized tests",
        version = "1.0",
        mixinStandardHelpOptions = true,
        usageHelpWidth = 200)
public class ProzeMain implements Callable<Integer> {

  @CommandLine.Parameters(
          index = "0",
          description = "The path to the target Maven project")
  private Path projectPath;

  @Override
  public Integer call() throws Exception {
    new ProzeSelector(projectPath).analyzeWithSpoon();
    return 0;
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new ProzeMain()).execute(args);
    System.exit(exitCode);
  }
}
