## Proze

See [PROZE: Generating Parameterized Unit Tests Informed by Runtime Data](http://arxiv.org/pdf/2407.00768) (Deepika Tiwari, Yogya Gamage, Martin Monperrus and Benoit Baudry), In Proceedings of IEEE Conference on Source Code Analysis and Manipulation, 2024.

```bibtex
@inproceedings{2407.00768,
 title = {PROZE: Generating Parameterized Unit Tests Informed by Runtime Data},
 booktitle = {Proceedings of IEEE Conference on Source Code Analysis and   Manipulation},
 year = {2024},
 doi = {10.1109/SCAM63643.2024.00025},
 author = {Deepika Tiwari and Yogya Gamage and Martin Monperrus and Benoit Baudry},
 url = {http://arxiv.org/pdf/2407.00768},
}
```

### Dataset 🔭

See <https://github.com/ASSERT-KTH/proze-experiments>

### `select`: Select target methods
- `cd /path/to/proze/select/` 
- `mvn clean install`
- `java -jar target/select-version-jar-with-dependencies.jar /path/to/maven/project/`
  - This generates a report called `report-<project-name>.json`
  - This also generates a script called `run-<project-name>-test-classes.sh` which can be (copied into the project directory and) used to run only the test classes that we are interested in
- The report generated by `select` includes methods that
  - are called directly by an `@Test` method
  - take primitive and/or String arguments

### `instrument`: Instrument target methods
1. `cd /path/to/proze/instrument/`

2. `python3 prepare.py /path/to/report/from/select.json`
  - This generates a report called `method-wise-report-<project-name>.json`

3. Run instrumentation script to generate aspect classes and prepare Glowroot configuration json file, follow one of these two (clean slate before each)
  - A. `python3 instrument.py /path/to/method/wise/report.json`
    - This captures method arguments + other data for all invocations of a method (until 3 MB)
  - B. `python3 instrument.py /path/to/method/wise/report.json original`
    - This captures method arguments only if method is directly invoked by an invoking test (no limit on size)

4. `mvn clean install`
  - This generates `target/instrument-version-jar-with-dependencies.jar`

5. (Skip this step if Glowroot downloaded already) Download Glowroot, create a directory called `plugins` in the directory that contains `glowroot.jar`

6. Copy `instrument-version-jar-with-dependencies.jar` into `/path/to/glowroot/plugins/`

### Test and production execution
1. Follow point 3A under `instrument`

2. Production: Add `-javaagent:/path/to/glowroot/glowroot.jar` when running project jar
  - Invocation data is saved to `/tmp/proze-object-data/`
  - Rename this to `/tmp/proze-object-data-prod/`

3. Tests:
  - Configure `maven-surefire-plugin` as follows:
  ```
   <plugin>
     <artifactId>maven-surefire-plugin</artifactId>
       <configuration>
         <argLine>-javaagent:/path/to/glowroot/glowroot.jar</argLine>
       </configuration>
   </plugin>
  ```
  - `mvn test` or `sh run-<project-name>-test-classes.sh` (i.e., the script generated by `select`) 
  - Invocation data is saved to `/tmp/proze-object-data/`
  - Rename this to `/tmp/proze-object-data-test/`

4. Follow point 3B under `instrument`

5. Re-run tests
  - Invocation data is saved to `/tmp/proze-object-data/`
  - Rename this to `/tmp/proze-object-data-original/`

### `generate`: Generate tests
- `cd /path/to/proze/generate/`
- `python3 analyze.py /path/to/method/wise/report.json`
  - This generates `analyzed-method-wise-report-<project-name>.json`
- `mvn clean install`
- `java -jar target/generate-version-jar-with-dependencies.jar /path/to/maven/project/ ./analyzed-method-wise-report-<project-name>.json`
  - The generated test classes are prefixed with `TestProze` under `output/generated/<project-name>/`
  - Add these generated tests within a new module within the project
