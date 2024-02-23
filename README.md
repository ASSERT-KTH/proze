## Proze

### Select target methods
- `cd /path/to/proze/select/` 
- `mvn clean install`
- `java -jar target/select-version-jar-with-dependencies.jar /path/to/maven/project/`
  - This generates a report called `report-<project-name>.json`

### Instrument target methods
- `cd /path/to/proze/instrument/`
- `python3 prepare.py /path/to/report/from/select.json`
  - This generates a report called `method-wise-report-<project-name>.json`
- `python3 instrument.py /path/to/method/wise/report.json`
  - This generates aspect classes and prepares the glowroot configuration json file
- `mvn clean install`
  - This generates `target/instrument-version-jar-with-dependencies.jar`
- (skip this step if Glowroot downloaded already) Download Glowroot, create a directory called `plugins` in the directory that contains `glowroot.jar`
- Copy `instrument-version-jar-with-dependencies.jar` into `/path/to/glowroot/plugins/`

### Execution
- Production: Add `-javaagent:/path/to/glowroot/glowroot.jar` when running project jar
- Tests: Configure `maven-surefire-plugin` as follows before running `mvn test`:
```
 <plugin>
   <artifactId>maven-surefire-plugin</artifactId>
     <configuration>
       <argLine>@{argLine} -javaagent:/path/to/glowroot/glowroot.jar</argLine>
     </configuration>
 </plugin>
```
