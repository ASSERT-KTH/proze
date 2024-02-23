package se.kth.assrt.proze.instrument;

import java.nio.file.Files;
import java.nio.file.Paths;

public interface AdviceTemplate {
  String storageDir = "/tmp/proze-object-data/";

  static void setup() {
    try {
      Files.createDirectories(Paths.get(storageDir));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(0);
    }
  }
}
