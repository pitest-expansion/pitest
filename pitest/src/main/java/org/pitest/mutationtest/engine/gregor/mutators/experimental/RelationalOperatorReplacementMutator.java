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

enum OpcodeCompareToZero {
  IFEQ(Opcodes.IFEQ) {
    public String toString() {
      return "==";
    }
  },
  IFGE(Opcodes.IFGE) {
    public String toString() {
      return ">=";
    }
  },
  IFGT(Opcodes.IFGT) {
    public String toString() {
      return ">";
    }
  },
  IFLE(Opcodes.IFLE) {
    public String toString() {
      return "<=";
    }
  },
  IFLT(Opcodes.IFLT) {
    public String toString() {
      return "<";
    }
  },
  IFNE(Opcodes.IFNE) {
    public String toString() {
      return "!=";
    }
  };

  private final int opcode;

  OpcodeCompareToZero(int opcode) {
    this.opcode = opcode;
  }

  public int getOpcode() {
    return this.opcode;
  }
}

enum OpcodeCompare {
  IF_ICMPEQ(Opcodes.IF_ICMPEQ) {
    public String toString() {
      return "==";
    }
  },
  IF_ICMPGE(Opcodes.IF_ICMPGE) {
    public String toString() {
      return ">=";
    }
  },
  IF_ICMPGT(Opcodes.IF_ICMPGT) {
    public String toString() {
      return ">";
    }
  },
  IF_ICMPLE(Opcodes.IF_ICMPLE) {
    public String toString() {
      return "<=";
    }
  },
  IF_ICMPLT(Opcodes.IF_ICMPLT) {
    public String toString() {
      return "<";
    }
  },
  IF_ICMPNE(Opcodes.IF_ICMPNE) {
    public String toString() {
      return "!=";
    }
  };

  private final int opcode;

  OpcodeCompare(int opcode) {
    this.opcode = opcode;
  }

  public int getOpcode() {
    return this.opcode;
  }
}

public class RelationalOperatorReplacementMutator implements MethodMutatorFactory {

  // getName() is overridden, so the following line is not actually used.
  // RELATIONAL_OPERATOR_REPLACEMENT_MUTATOR;

  private final OpcodeCompareToZero operator;

  /**
   * Class constructor.
   *
   * @param operator to replace
   */
  public RelationalOperatorReplacementMutator(final OpcodeCompareToZero operator) {
    this.operator = operator;
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
      for (int j = 0; j < mutations.length; j++) {
        if (i != j) {
          mutations.add(
              new RelationalOperatorReplacementMutator(
                  allOperators[i],
                  allOperators[j]));
        }
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
    return this.getClass().getName();
  }

  @Override
  public String getName() {
    return "RELATIONAL_OPERATOR_" + this.operator.name()
        + "_REPLACEMENT_MUTATOR";
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
