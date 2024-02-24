package se.kth.assrt.proze.select;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.MavenLauncher;
import spoon.reflect.CtModel;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class ProzeSelector {

  private static Logger logger = LoggerFactory.getLogger(ProzeSelector.class);
  private final Path projectPath;
  private final String projectName;

  public ProzeSelector(Path projectPath) {
    this.projectPath = projectPath;
    this.projectName = projectPath.getFileName().toString();
  }

  private void writeJsonReportToDisk(List<ProzeTestMethod> targetMethodList) {
    String report = "./report-" + projectName + ".json";
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    try (FileWriter writer = new FileWriter(report)) {
      gson.toJson(targetMethodList, writer);
      logger.info("Target methods saved in " + report);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void writeSetOfTestClassesToRun(Set<String> setOfTestClasses) {
    String report = "./run-" + projectName + "-test-classes.sh";
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    try (FileWriter writer = new FileWriter(report)) {
      writer.write("#!/bin/bash\n");
      for (String testClass : setOfTestClasses) {
        writer.write("echo \"[PROZE-INFO] Running tests in " + testClass + "\"\n");
        writer.write("mvn test -Dtest=\"" + testClass + "\"\n");
      }
      logger.info("mvn test script saved in " + report);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void analyzeWithSpoon() {
    logger.info("Processing project " + projectName);
    MavenLauncher launcher;
    try {
      logger.info("Considering all sources (app + test)");
      launcher = new MavenLauncher(projectPath.toString(),
              MavenLauncher.SOURCE_TYPE.ALL_SOURCE);
      launcher.buildModel();
    } catch (Exception e) {
      logger.info(e.getMessage());
      return;
    }
    CtModel model = launcher.getModel();
    ProzeTestMethodProcessor testMethodProcessor = new ProzeTestMethodProcessor();
    model.processWith(testMethodProcessor);
    writeJsonReportToDisk(testMethodProcessor.getTestMethods());
    writeSetOfTestClassesToRun(testMethodProcessor.getSetOfTestClasses());
  }
}
