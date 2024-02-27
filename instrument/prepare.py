import json
import sys

def prepare_method_signature(method_signature):
  return method_signature.replace("(", "_").replace(")", "")

def prepare_method_wise_report(test_methods):
  methods_called = set()
  for i in range(len(test_methods)):
    this_test_method = test_methods[i]
    for j in range(len(this_test_method["invocationWithPrimitiveParams"])):
      this_invocation = this_test_method["invocationWithPrimitiveParams"][j]
      methods_called.add(this_invocation["fullMethodSignature"])
  print("Found", len(methods_called), "unique methods invoked")
  
  method_list = []
  for m in methods_called:
    data = {"fullMethodSignature": "",
            "declaringType": "",
            "methodName": "",
            "parameters": "",
            "invokedByTests": []}
    data["fullMethodSignature"] = prepare_method_signature(m)
    for j in range(len(test_methods)):
      this_test_method = test_methods[j]
      for k in range(len(this_test_method["invocationWithPrimitiveParams"])):
        this_invocation = this_test_method["invocationWithPrimitiveParams"][k]
        if this_invocation["fullMethodSignature"] == m:
          data["declaringType"] = this_invocation["methodDeclaringType"]
          data["methodName"] = this_invocation["methodName"]
          data["parameters"] = this_invocation["methodParameterTypes"]
          data["invokedByTests"].append(str(this_test_method["testClassName"] + "." + this_test_method["testName"]))
    method_list.append(data)
  return method_list

def main(argv):
  try:
    report = open(argv[1])
    test_methods = json.load(report)
    print("Found details for", len(test_methods), "tests")
    method_list = prepare_method_wise_report(test_methods)
    output_report_file = "./method-wise-" + argv[1].split("/")[-1]
    with open(output_report_file, "w") as outfile:
      json.dump(method_list, outfile, indent = 2)
    print("Generated report for these", len(method_list), "methods:", output_report_file)
  except Exception as e:
    print("USAGE: python prepare.py </path/to/select/report>.json")
    print(e)
    sys.exit()

if __name__ == "__main__":
  main(sys.argv)

