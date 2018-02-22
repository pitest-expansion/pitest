/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.engine.gregor.mutators;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.gregor.AbstractJumpMutator;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

public enum RelationalOperatorReplacementMutator implements MethodMutatorFactory {

  // getName() is overridden, so the following line is not actually used.
  // RELATIONAL_OPERATOR_REPLACEMENT_MUTATOR;

  public enum RelationalOperator {
    ACMPEQ("reference-equal"),
    ACMPNE("reference-not-equal"),
    ICMPEQ("int-equal"),
    ICMPGE("int-greater-equal"),
    ICMPGT("int-greater"),
    ICMPLE("int-less-equal"),
    ICMPLT("int-less"),
    ICMPNE("int-not-equal");
    private String desc;

    private RelationalOperator(String desc) {
      this.desc = desc;
    }

    public String description() {
      return this.desc;
    }
  }

  private final RelationalOperator original;
  private final RelationalOperator replacement;

  /**
   * Class constructor taking two RelationalOperator arguments.
   *
   * @param original the operator to replace
   * @param replacement the operator to use as the replacement
   */
  public RelationalOperatorReplacementMutator(
      final RelationalOperator original,
      final RelationalOperator replacement) {
    this.original = original;
    this.replacement = replacement;
  }

  /**
   * Returns a list of relational operator replacement mutations to apply.
   *
   * @return List the list of factories to produce the mutators.
   */
  public static Iterable<MethodMutatorFactory> makeMutators() {
    final List<MethodMutatorFactory> mutations = new ArrayList<>();
    // Get all of the possible values of RelationalOperator.
    final RelationalOperator[] allOperators = RelationalOperator.getEnumConstants();
    // Add all pairings of those values to the mutation list.
    for (int i = 0; i < mutations.length; i++) {
      for (int j = i + 1; j < mutations.length; j++) {
        mutations.add(
            new RelationalOperatorReplacementMutator(
                allOperators[i],
                allOperators[j]));
          }
    }
    return mutations;
  }

  /**
   * Create the mutator.
   *
   * @return MethodVisitor
   */
  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new RelationalOperatorReplacementMethodVisitor(
        this,
        context,
        methodVisitor,
        "replace relational operator " + this.original.description()
          + " with " + this.replacement.description());
  }

  @Override
  public String getGloballyUniqueId() {
    return this.getClass().getName()
      + "_" + this.original + "_" + this.replacement;
  }

  @Override
  public String getName() {
    return "RELATIONAL_OPERATOR_" + this.original + "_"
        + this.replacement + "_MUTATOR";
  }

}

class RelationalOperatorReplacementMethodVisitor extends AbstractJumpMutator {

  private static final String                     DESCRIPTION = "changed conditional boundary";
  private static final Map<Integer, Substitution> MUTATIONS   = new HashMap<>();

  static {
    MUTATIONS.put(Opcodes.IFLE, new Substitution(Opcodes.IFLT, DESCRIPTION));
    MUTATIONS.put(Opcodes.IFGE, new Substitution(Opcodes.IFGT, DESCRIPTION));
    MUTATIONS.put(Opcodes.IFGT, new Substitution(Opcodes.IFGE, DESCRIPTION));
    MUTATIONS.put(Opcodes.IFLT, new Substitution(Opcodes.IFLE, DESCRIPTION));
    MUTATIONS.put(Opcodes.IF_ICMPLE, new Substitution(Opcodes.IF_ICMPLT,
        DESCRIPTION));
    MUTATIONS.put(Opcodes.IF_ICMPGE, new Substitution(Opcodes.IF_ICMPGT,
        DESCRIPTION));
    MUTATIONS.put(Opcodes.IF_ICMPGT, new Substitution(Opcodes.IF_ICMPGE,
        DESCRIPTION));
    MUTATIONS.put(Opcodes.IF_ICMPLT, new Substitution(Opcodes.IF_ICMPLE,
        DESCRIPTION));
  }

  RelationalOperatorReplacementMethodVisitor(final MethodMutatorFactory factory,
      final MutationContext context, final MethodVisitor delegateMethodVisitor) {
    super(factory, context, delegateMethodVisitor);
  }

  @Override
  protected Map<Integer, Substitution> getMutations() {
    return MUTATIONS;
  }

}
