package se.kth.assrt.proze.generate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.MavenLauncher;
import spoon.reflect.CtModel;

import java.nio.file.Path;
import java.util.List;

public class GenerateLauncher {
  private static final Logger logger = LoggerFactory.getLogger(GenerateLauncher.class);
  private final Path projectPath;
  private final String projectName;

  public GenerateLauncher(Path projectPath) {
    this.projectPath = projectPath;
    this.projectName = projectPath.getFileName().toString();
  }

  public void processWithSpoon(List<TargetMethod> targetMethods) {
    logger.info("Processing project " + projectName);
    MavenLauncher launcher;
    try {
      logger.info("Considering all sources (app + test)");
      launcher = new MavenLauncher(projectPath.toString(),
              MavenLauncher.SOURCE_TYPE.ALL_SOURCE);
      launcher.getEnvironment().disableConsistencyChecks();
      launcher.buildModel();
    } catch (Exception e) {
      logger.info(e.getMessage());
      return;
    }
    CtModel model = launcher.getModel();
    model.processWith(new TestMethodProcessor(targetMethods, model));
    String outputDirectory = "./output/generated/" + projectName;
    launcher.setSourceOutputDirectory(outputDirectory);
    launcher.prettyprint();
  }
}
