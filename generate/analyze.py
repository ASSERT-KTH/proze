import glob
import json
import os
import pandas as pd
import re
import sys


def serialize_sets(obj):
  if isinstance(obj, set):
    return list(obj)
  return obj

def merge_prod_test_results(result_prod, result_test):
  methods_invoked_prod = set()
  methods_invoked_test = set()
  for p in result_prod:
    methods_invoked_prod.add(p["fullMethodSignature"])
  for t in result_test:
    methods_invoked_test.add(t["fullMethodSignature"])

  # should be equal to number of methods invoked in prod
  print("Methods invoked in both test and prod: ", len(methods_invoked_test.intersection(methods_invoked_prod)))

  # should be zero
  print("Methods invoked in prod but not test: ", len(methods_invoked_prod - methods_invoked_test))

  # should be non-zero
  print("Methods invoked in test but not prod: ", len(methods_invoked_test - methods_invoked_prod))

  # assuming test execution has more invoked methods
  merged_result = result_test
  for m in range(len(merged_result)):
    for p in range(len(result_prod)):
      if merged_result[m]["fullMethodSignature"] == result_prod[p]["fullMethodSignature"]:
        merged_result[m]["numInvocationsProd"] = result_prod[p]["numInvocationsProd"]
        merged_result[m]["numUniqueArgumentsProd"] = result_prod[p]["numUniqueArgumentsProd"]
        merged_result[m]["uniqueArgumentsProd"] = result_prod[p]["uniqueArgumentsProd"]
        merged_result[m]["unionProdAndTestArgs"] = list(set(merged_result[m]["uniqueArgumentsTest"])
                                                          .union(set(result_prod[p]["uniqueArgumentsProd"])))
        merged_result[m]["sizeUnion"] = len(merged_result[m]["unionProdAndTestArgs"])
        continue
    for m in range(len(merged_result)):
      if "numInvocationsProd" not in merged_result[m].keys():
        merged_result[m]["numInvocationsProd"] = 0
        merged_result[m]["numUniqueArgumentsProd"] = 0
        merged_result[m]["uniqueArgumentsProd"] = []
        merged_result[m]["unionProdAndTestArgs"] = merged_result[m]["uniqueArgumentsTest"]
        merged_result[m]["sizeUnion"] = len(merged_result[m]["uniqueArgumentsTest"])
  return merged_result

def prepare_analysis_report(union_result, method_wise_report):
  final_report = []
  method_df = pd.read_json(method_wise_report)
  for i in range(len(union_result)):
    data = method_df[method_df["fullMethodSignature"] == union_result[i]["fullMethodSignature"]].to_dict('records')[0]
    data["originalTestArgs"] = union_result[i]["originalTestArgs"]
    data["numInvocationsProd"] = union_result[i]["numInvocationsProd"]
    data["numInvocationsTest"] = union_result[i]["numInvocationsTest"]
    data["totalNumInvocations"] = union_result[i]["numInvocationsProd"] + union_result[i]["numInvocationsTest"]
    data["numTestArgs"] = union_result[i]["numUniqueArgumentsTest"]
    data["numProdArgs"] = union_result[i]["numUniqueArgumentsProd"]
    data["sizeUnion"] = union_result[i]["sizeUnion"]
    data["unionProdAndTestArgs"] = sorted(union_result[i]["unionProdAndTestArgs"])
    # we want to parameterize only if we have more than one argument in the union
    if data["sizeUnion"] > 1:
      # we want multiple sources of info for the arguments
      if data["numProdArgs"] > 0 or (len(set(data["invokedByTests"])) > 1):
        final_report.append(data)
  return final_report

def add_arguments_as_string_in_df(df):
  return [','.join(map(str, l)) for l in df['parameters']]

def analyze_data(data_files, source):
  result = []
  print("[INFO] Working with data in source", source)
  for data_file in data_files:
    print("[INFO] Analyzing file", data_file)
    data = pd.read_json(data_file)
    print("[INFO] Invocations in execution:", len(data))
    # convert parameter list to string before converting to set
    data['argumentsAsString'] = add_arguments_as_string_in_df(data)
    print("=================================================================================================")
    full_method_signature = re.sub(r".+\/(.+)\.json", r"\g<1>", data_file)
    # merge original arguments from tests that directly invoke method
    if (source == "Test"):
      corresponding_original_test_file = data_file.replace("proze-object-data-test", "proze-object-data-original")
      if os.path.isfile(corresponding_original_test_file):
        print("Merging original test arguments for", full_method_signature)
        original_test_data = pd.read_json(corresponding_original_test_file)
        original_test_data['argumentsAsString'] = add_arguments_as_string_in_df(original_test_data)
        data = pd.concat([data, original_test_data], ignore_index=True, sort=False)

    original_test_args = {}
    if (source == "Test"):
      for i in range(len(data)):
        if (data["calledByInvokingTest"][i]):
          if (data["invokingTest"][i] in original_test_args.keys()):
            original_test_args[data["invokingTest"][i]].add(data["argumentsAsString"][i])
          else:
            original_test_args[data["invokingTest"][i]] = set(data["argumentsAsString"][i])

    result.append({"fullMethodSignature": full_method_signature,
                   "numInvocations" + source: len(data["argumentsAsString"]),
                   "numUniqueArguments" + source: len(set(data["argumentsAsString"])),
                   "uniqueArguments" + source: sorted(set(data["argumentsAsString"])),
                   "originalTestArgs": original_test_args})
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
  print("[INFO] Found", len(original_test_files), "file(s) with test data with original args")
  for p in prod_files:
    sanitize_file_to_json(p)
  for t in test_files:
    sanitize_file_to_json(t)
  for o in original_test_files:
    sanitize_file_to_json(o)
  print("=================================================================================================")

def main(argv):
  try:
    if len(argv) < 2:
      raise Exception("method-wise-report not provided")
    global prod_files, test_files, original_test_files
    prod_files = glob.glob("/tmp/proze-object-data-prod/*.json")
    test_files = glob.glob("/tmp/proze-object-data-test/*.json")
    original_test_files = glob.glob("/tmp/proze-object-data-original/*.json")
    get_data_files_and_sanitize()
    result_prod = analyze_data(prod_files, "Prod")
    result_test = analyze_data(test_files, "Test")
    print("PRODUCTION")
    print(len(result_prod))
    print("TEST")
    print(len(result_test))
    union_result = merge_prod_test_results(result_prod, result_test)
    print("UNION")
    print(len(union_result))
    final_report = prepare_analysis_report(union_result, argv[1])
    output_report_file = "./analyzed-" + argv[1].split("/")[-1]
    with open(output_report_file, "w") as outfile:
      json.dump(final_report, outfile, indent = 2, default=serialize_sets)
    print("Generated analysis report for these", len(final_report), "methods:", output_report_file)
  except Exception as e:
    print("USAGE: python analyze.py </path/to/method/wise/report>.json")
    print(e)
    sys.exit()

if __name__ == "__main__":
  main(sys.argv)
