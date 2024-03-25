import glob
import json
import os
import pandas as pd
import re
import sys

def prepare_analysis_report(union_result, method_wise_report):
  final_report = []
  method_df = pd.read_json(method_wise_report)
  for i in range(len(union_result)):
    data = method_df[method_df["fullMethodSignature"] == union_result[i]["fullMethodSignature"]].to_dict('records')[0]
    data["numTestArgs"] = union_result[i]["numTestArgs"]
    data["numProdArgs"] = union_result[i]["numProdArgs"]
    data["sizeUnion"] = union_result[i]["sizeUnion"]
    data["unionProdAndTestArgs"] = union_result[i]["unionProdAndTestArgs"]
    data["testArgs"] = union_result[i]["testArgs"]
    final_report.append(data)
  return final_report

def add_parameters_as_string_in_df(df):
  return [','.join(map(str, l)) for l in df['parameters']]

def analyze_data():
  result = []
  # we care about methods that are invoked in production
  for prod_data_file in prod_files:
    print("[INFO] Analyzing file", prod_data_file)
    prod_data = pd.read_json(prod_data_file)
    # read data for same the method from test folder
    corresponding_test_data_file = prod_data_file.replace("object-data-prod", "object-data-test")
    if not os.path.isfile(corresponding_test_data_file):
      continue
    test_data = pd.read_json(corresponding_test_data_file)
    print("[INFO] Invocations in production execution:", len(prod_data))
    print("[INFO] Invocations in test execution:", len(test_data))
    # remove invocations of method during test execution that are not called by test methods
    print("[INFO] Removing invocations from test executions that are not made directly by an invoking test")
    test_data = test_data.drop(test_data[test_data["calledByInvokingTest"] == False].index)
    print("[INFO] Updated number of invocations in test execution:", len(test_data))
    if len(test_data) == 0:
      print("[INFO] No invocations made directly from invoking tests, SKIPPING THIS METHOD")
      print("=================================================================================================")
      continue
    # convert parameter list to string before intersection and union
    prod_data['parametersAsString'] = add_parameters_as_string_in_df(prod_data)
    test_data['parametersAsString'] = add_parameters_as_string_in_df(test_data)
    # only in prod
    only_prod = list(set(prod_data["parametersAsString"]) - set(test_data["parametersAsString"]))
    print("[INFO] Parameters only in production, but not in test executions:", len(only_prod))
    test_args = []
    # map parameters to test that use it
    for index, row in test_data.iterrows():
      first_ten_stack_elements = row["stackTrace"].replace("[", "").replace("]", "").split(", ")[0:10]
      test = list(filter(lambda s: ("Test" in s), first_ten_stack_elements))
      test_args.append({"test": test[0], "arguments": row["parametersAsString"]})
    # only in test
    only_test = list(set(test_data["parametersAsString"]) - set(prod_data["parametersAsString"]))
    print("[INFO] Parameters only in test, but not in production executions:", len(only_test))
    # intersection
    intersection = list(set(prod_data["parametersAsString"]) & set(test_data["parametersAsString"]))
    print("[INFO] Parameters common to both production and test executions:", len(intersection))
    # union
    union = list(set(prod_data["parametersAsString"]).union(set(test_data["parametersAsString"])))
    print("[INFO] Size of union of test and production parameters:", len(union))
    print("=================================================================================================")
    full_method_signature = re.sub(r".+\/(.+)\.json", r"\g<1>", prod_data_file)
    result.append({"fullMethodSignature": full_method_signature,
                   "testArgs": test_args, "unionProdAndTestArgs": sorted(union),
                   "numTestArgs": len(set(test_data["parametersAsString"])),
                   "numProdArgs": len(set(prod_data["parametersAsString"])),
                   "sizeUnion": len(union)})
  return result

def sanitize_file_to_json(data_file):
  # nothing to do if file already sanitized
  with open(data_file, 'r') as f:
    lines = f.readlines()
    if lines[0].startswith("["):
      print("[INFO]", data_file, "is already sanitized")
      return
  with open(data_file, 'r') as f:
    lines = f.readlines()
    # add comma to all but last line
    for i in range(len(lines) - 1):
      lines[i] = re.sub(r"}\n", "},\n", lines[i])
    # add closing brace after last line
    lines[len(lines) - 1] = lines[len(lines) - 1] + "]\n"
    # add opening brace before first line
    lines[0] = "[\n" + lines[0]
  with open(data_file, 'w') as f:
    for l in range(len(lines)):
      f.write(lines[l])

def get_data_files_and_sanitize():
  print("[INFO] Found", len(prod_files), "file(s) with production data")
  print("[INFO] Found", len(test_files), "file(s) with test data")
  for p in prod_files:
    sanitize_file_to_json(p)
  for t in test_files:
    sanitize_file_to_json(t)
  print("=================================================================================================")

def main(argv):
  try:
    if len(argv) < 2:
      raise Exception("method-wise-report not provided")
    global prod_files, test_files
    prod_files = glob.glob("/tmp/proze-object-data-prod/*.json")
    test_files = glob.glob("/tmp/proze-object-data-test/*.json")
    get_data_files_and_sanitize()
    result = analyze_data()
    final_report = prepare_analysis_report(result, argv[1])
    output_report_file = "./analyzed-" + argv[1].split("/")[-1]
    with open(output_report_file, "w") as outfile:
      json.dump(final_report, outfile, indent = 2)
    print("Generated analysis report for these", len(final_report), "methods:", output_report_file)
  except Exception as e:
    print("USAGE: python analyze.py </path/to/method/wise/report>.json")
    print(e)
    sys.exit()

if __name__ == "__main__":
  main(sys.argv)
