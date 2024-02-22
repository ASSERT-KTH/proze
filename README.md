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
- ...
