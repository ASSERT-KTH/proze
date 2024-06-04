import json
import sys

def calculate_metrics(test_methods):
  fontbox = {"module_fqn": "org.apache.fontbox.",
             "all_cuts": 0,
             "eligible_cuts": 0,
             "invocations": 0,
             "assertions": 0,
             "target_methods": set()}

  xmpbox = {"module_fqn": "org.apache.xmpbox.",
            "all_cuts": 0,
            "eligible_cuts": 0,
            "invocations": 0,
            "assertions": 0,
            "target_methods": set()}

  pdfbox = {"module_fqn": "org.apache.pdfbox.",
            "all_cuts": 0,
            "eligible_cuts": 0,
            "invocations": 0,
            "assertions": 0,
            "target_methods": set()}

  sso = {"module_fqn": "org.wso2.carbon.identity.sso.saml",
         "all_cuts": 0,
         "eligible_cuts": 0,
         "invocations": 0,
         "assertions": 0,
         "target_methods": set()}

  query = {"module_fqn": "org.wso2.carbon.identity.query.saml",
           "all_cuts": 0,
           "eligible_cuts": 0,
           "invocations": 0,
           "assertions": 0,
           "target_methods": set()}

  modules = [fontbox, xmpbox, pdfbox, sso, query]

  print("TOTAl #tests in the select report:", len(test_methods))
  for t in range(len(test_methods)):
    # find the number of total CUTs per module (all_cuts)
    for m in range(len(modules)):
      if (test_methods[t]["testClassName"].startswith(modules[m]["module_fqn"])):
        modules[m]["all_cuts"] += 1
    # find eligible CUTs + related info (eligible_cuts)
    if (len(test_methods[t]["invocationWithPrimitiveParams"]) > 0):
      for m in range(len(modules)):
        if (test_methods[t]["testClassName"].startswith(modules[m]["module_fqn"])):
          modules[m]["eligible_cuts"] += 1
          modules[m]["invocations"] += len(test_methods[t]["invocationWithPrimitiveParams"])
          modules[m]["assertions"] += test_methods[t]["numAssertions"]
          for i in range(len(test_methods[t]["invocationWithPrimitiveParams"])):
            modules[m]["target_methods"].add(
              test_methods[t]["invocationWithPrimitiveParams"][i]["fullMethodSignature"])

  print(modules)

def main(argv):
  try:
    select_report = open(argv[1])
    test_methods = json.load(select_report)
    calculate_metrics(test_methods)
  except Exception as e:
    print("USAGE: python metrics.py </path/to/select/report>.json")
    print(e)
    sys.exit()

if __name__ == "__main__":
  main(sys.argv)
