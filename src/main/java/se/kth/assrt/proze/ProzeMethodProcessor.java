package se.kth.assrt.proze;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.declaration.CtMethod;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProzeMethodProcessor extends AbstractProcessor<CtMethod<?>> {

  private final List<ProzeTargetMethod> targetMethods = new LinkedList<>();

  private boolean methodReturnsPrimitiveOrString(CtMethod<?> method) {
    if (method.getType().toString().equals("void"))
      return false;
    return method.getType().isPrimitive() ||
            method.getType().toString().equals("java.lang.String");
  }

  private boolean methodParametersArePrimitivesOrStrings(CtMethod<?> method) {
    if (method.getParameters().isEmpty())
      return false;
    return (method.getParameters().stream().allMatch(p -> p.getType().isPrimitive() ||
            p.getType().toString().equals("java.lang.String")));
  }

  private boolean methodIsNotFinalStaticOrAbstract(CtMethod<?> method) {
    return !method.isFinal() & !method.isStatic() & !method.isAbstract();
  }

  private boolean methodIsNotEmpty(CtMethod<?> method) {
    Optional<CtBlock<?>> methodBody = Optional.ofNullable(method.getBody());
    return methodBody.isPresent() && !methodBody.get().getStatements().isEmpty();
  }

  public List<ProzeTargetMethod> getListOfTargetMethods() {
    return targetMethods;
  }

  @Override
  public void process(CtMethod<?> method) {
    if (method.isPublic()
            & method.isStatic()
            & methodReturnsPrimitiveOrString(method)
            & methodIsNotEmpty(method)
            & methodParametersArePrimitivesOrStrings(method)) {
      ProzeTargetMethod targetMethod = new ProzeTargetMethod(
              method.getDeclaringType().getQualifiedName(),
              method.getSimpleName(),
              method.getParameters().stream().map((p) -> p.getType().toString()).collect(Collectors.toList()),
              method.getType().toString(),
              method.getSignature());
      targetMethods.add(targetMethod);
    }
  }
}
