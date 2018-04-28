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
package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
import org.pitest.mutationtest.engine.gregor.InsnSubstitution;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.ZeroOperandMutation;

public enum BitwiseOperatorMutator implements MethodMutatorFactory {

    BITWISE_OPERATOR_MUTATOR;

    @Override
    public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo,
            final MethodVisitor methodVisitor) {
        return new BitwiseOperatorMutatorMethodVisitor(this, methodInfo, context, methodVisitor);
    }

    @Override
    public String getGloballyUniqueId() {
        return this.getClass().getName();
    }

    @Override
    public String getName() {
        return name();
    }

}

class BitwiseOperatorMutatorMethodVisitor extends AbstractInsnMutator {

    private static final Map<Integer, ZeroOperandMutation> MUTATIONS = new HashMap<>();

    static {
        // integers
        // replace AND with OR, XOR
        MUTATIONS.put(Opcodes.IAND, new InsnSubstitution(Opcodes.IOR, "Replaced & with | (integer)"));
        MUTATIONS.put(Opcodes.IAND, new InsnSubstitution(Opcodes.IXOR, "Replaced & with ^ (integer)"));
        
        // replace OR with AND, XOR
        MUTATIONS.put(Opcodes.IOR, new InsnSubstitution(Opcodes.IAND, "Replaced | with & (integer)"));
        MUTATIONS.put(Opcodes.IOR, new InsnSubstitution(Opcodes.IXOR, "Replaced | with ^ (integer)"));    
        
        // replace XOR with AND, OR
        MUTATIONS.put(Opcodes.IXOR, new InsnSubstitution(Opcodes.IAND, "Replaced ^ with & (integer)"));
        MUTATIONS.put(Opcodes.IXOR, new InsnSubstitution(Opcodes.IOR, "Replaced ^ with | (integer)"));    


        // longs
        // replace AND with OR, XOR
        MUTATIONS.put(Opcodes.LAND, new InsnSubstitution(Opcodes.LOR, "Replaced & with | (long)"));
        MUTATIONS.put(Opcodes.LAND, new InsnSubstitution(Opcodes.LXOR, "Replaced & with ^ (long)"));
        
        // replace OR with AND, XOR
        MUTATIONS.put(Opcodes.LOR, new InsnSubstitution(Opcodes.LAND, "Replaced | with & (long)"));
        MUTATIONS.put(Opcodes.LOR, new InsnSubstitution(Opcodes.LXOR, "Replaced | with ^ (long)"));    
        
        // replace XOR with AND, OR
        MUTATIONS.put(Opcodes.LXOR, new InsnSubstitution(Opcodes.LAND, "Replaced ^ with & (long)"));
        MUTATIONS.put(Opcodes.LXOR, new InsnSubstitution(Opcodes.LOR, "Replaced ^ with | (long)")); 

    }

    BitwiseOperatorMutatorMethodVisitor(final MethodMutatorFactory factory, final MethodInfo methodInfo,
            final MutationContext context, final MethodVisitor writer) {
        super(factory, methodInfo, context, writer);
    }

    @Override
    protected Map<Integer, ZeroOperandMutation> getMutations() {
        return MUTATIONS;
    }

}
