import glob
import numpy as np
import pandas as pd
import re
import sys

# this is an alternative instrumentation script for GraphHopper
# that instruments only those methods that are called in prod;
# this helps avoid test execution that otherwise takes ages

def sanitize_parameter_and_test_list(param_or_test_list):
  parameters_or_tests = param_or_test_list.replace("[", "").replace("]", "").replace("'", "").split(", ")
  parameters_or_tests = map(lambda pt: "\"" + pt + "\"", parameters_or_tests)
  parameters_or_tests = ', '.join(parameters_or_tests)
  return parameters_or_tests


def parameters_per_file_name(param_list):
  parameters = param_list.replace("[", "").replace("]", "").replace("'", "").replace(" ", "")
  return parameters

# generate aspect classes based on the ProzeAspect0 template
def generate_aspects(df):
  base_path = "./src/main/java/se/kth/assrt/proze/instrument/ProzeAspect"
  found_aspects = sorted(glob.glob(base_path + "*.java"), key=lambda x:float(re.findall("(\d+)",x)[0]))
  count = int(re.search(r"(\d+)", found_aspects[-1]).group())
  aspects = []
  template_file_path = base_path + str(0) + ".java"
  df.replace(np.nan, '', regex=True, inplace=True)
  prod_files_path = "/tmp/proze-object-data-prod/"
  for index, row in df.iterrows():
    current_method_file = (prod_files_path
                           + row['declaringType']
                           + "." + row['methodName']
                           + "_" + parameters_per_file_name(str(row['parameters']))
                           + ".json")
    if current_method_file in glob.glob(prod_files_path + "*"):
      with open(template_file_path) as template:
        count += 1
        aspect_string = "\"se.kth.assrt.proze.instrument.ProzeAspect" + str(count) + "\""
        aspects.append(aspect_string)
        new_file_path = base_path + str(count) + ".java"
        with open(new_file_path, "w") as f:
          for line in template:
            if ("public class ProzeAspect0" in line):
              line = line.replace("0", str(count))
            if ("@Pointcut(className =" in line):
              line = re.sub(r"=\s\"(.+)\",", "= \"" + row['declaringType'] + "\",", line)
            if ("methodName = " in line):
              # add angular brackets for constructor calls
              if (row['methodName'] == "init"):
                row['methodName'] = "<" + row['methodName'] + ">"
              line = re.sub(r"=\s\"(.+)\",", "= \"" + row['methodName'] + "\",", line)
            if ("methodParameterTypes = " in line):
              if not (row['parameters']):
                param_list = ""
              else:
                param_list = sanitize_parameter_and_test_list(str(row['parameters']))
              line = re.sub(r"=\s{.*},", "= {" + param_list + "},", line)
            if ("timerName = " in line):
              line = re.sub(r"=\s\".+\"\)", "= \"" + row['declaringType'] + "-" + row['methodName'] + "\")", line)
            if ("int COUNT = " in line):
              line = re.sub(r"=\s\d+;", "= " + str(count) + ";", line)
            if ("testMethodsThatCallThisMethod = " in line):
              test_list = sanitize_parameter_and_test_list(str(row["invokedByTests"]))
              test_list = test_list.replace(", ", ",\n            ")
              line = re.sub(r"\(.+\)", "(\n            " + test_list + ")", line)
            if ("executionContext = " in line):
              line = re.sub(r"=\s\"\"", "= \"" + mode + "\"", line)
            f.write(line)
  print("New aspect classes added in se.kth.assrt.proze.instrument")
  all_aspects = sorted(glob.glob(base_path + "*.java"), key=lambda x:float(re.findall("(\d+)",x)[0]))
  new_aspect_count = int(re.search(r"(\d+)", all_aspects[-1]).group())
  return new_aspect_count

def update_glowroot_plugin_json(aspect_count):
  aspect_path = "se.kth.assrt.proze.instrument.ProzeAspect"
  plugin_json_path = "./src/main/resources/META-INF/glowroot.plugin.json"
  index = 0
  aspect_lines = ""
  for i in range(1, aspect_count + 1):
    aspect_lines += "    \"" + aspect_path + str(i) + "\""
    if i < aspect_count:
      aspect_lines += ","
    aspect_lines += "\n"
  with open(plugin_json_path, "r") as json_file:
    for num, line in enumerate(json_file, 1):
      if "aspects" in line:
        index = num
  # delete previous aspect list
  with open(plugin_json_path, "r+") as json_file:
    contents = json_file.readlines()
    updated_contents = [l for l in contents if "ProzeAspect" not in l]
    json_file.seek(0)
    json_file.truncate()
    json_file.writelines(updated_contents)
  # update aspect list with generated aspects
  with open(plugin_json_path, "r+") as json_file:
    contents = json_file.readlines()
    contents.insert(index, aspect_lines)
    json_file.seek(0)
    json_file.writelines(contents)
  print("resources/META-INF/glowroot.plugin.json updated")

def main(argv):
  try:
    global mode
    mode = "PROD"
    if len(argv) == 3:
      mode = argv[2].upper()
    if mode != "PROD" and mode != "TEST":
      raise Exception("incorrect MODE")
    print("Mode set to", mode)
    df = pd.read_json(argv[1])
    print("Found data for", df.index.size, "methods")
    aspect_count = generate_aspects(df)
    update_glowroot_plugin_json(aspect_count)
  except Exception as e:
    print("USAGE: python instrument.py </path/to/method/wise/report>.json <MODE>")
    print("where MODE: one of TEST, PROD (default)")
    print(e)
    sys.exit()

if __name__ == "__main__":
  main(sys.argv)
