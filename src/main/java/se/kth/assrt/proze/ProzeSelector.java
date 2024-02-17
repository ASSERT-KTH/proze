package se.kth.assrt.proze;

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

public class ProzeSelector {

  private static Logger logger = LoggerFactory.getLogger(ProzeSelector.class);
  private final Path projectPath;
  private final String projectName;

  public ProzeSelector(Path projectPath) {
    this.projectPath = projectPath;
    this.projectName = projectPath.getFileName().toString();
  }

  private void writeJsonReportToDisk(List<ProzeTargetMethod> targetMethodList) {
    String report = "./report-" + projectName + ".json";
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    try (FileWriter writer = new FileWriter(report)) {
      gson.toJson(targetMethodList, writer);
      logger.info("Target methods saved in " + report);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void analyzeWithSpoon() {
    logger.info("Processing project " + projectName);
    MavenLauncher launcher = new MavenLauncher(projectPath.toString(),
            MavenLauncher.SOURCE_TYPE.ALL_SOURCE);
    launcher.buildModel();
    CtModel model = launcher.getModel();
    ProzeMethodProcessor prozeMethodProcessor = new ProzeMethodProcessor();
    model.processWith(prozeMethodProcessor);
    writeJsonReportToDisk(prozeMethodProcessor.getListOfTargetMethods());
  }
}
