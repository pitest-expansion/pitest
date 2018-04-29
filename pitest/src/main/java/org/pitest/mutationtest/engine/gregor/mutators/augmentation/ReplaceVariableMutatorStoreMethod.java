package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import java.util.Random;
import java.util.ArrayList;

/*
 * M4 mutator i.e. replace a local variable by another local variable at random
 */

public enum ReplaceVariableMutatorStoreMethod implements MethodMutatorFactory {
    REPLACE_VARIABLE_MUTATOR_STORE_METHOD;

    @Override
    public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo,
            final MethodVisitor methodVisitor, ClassByteArraySource byteSource) {
        return new RandomVarReplacement1(this, context, methodVisitor);
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

class RandomVarReplacement1 extends MethodVisitor {
    private final MethodMutatorFactory factory;
    private final MutationContext context;
    ArrayList<Integer> varIndex = new ArrayList<Integer>();
    ArrayList<String> varType = new ArrayList<String>();
    int n;

    RandomVarReplacement1(final MethodMutatorFactory factory, final MutationContext context,
            final MethodVisitor delegateMethodVisitor) {
        super(Opcodes.ASM6, delegateMethodVisitor);
        this.factory = factory;
        this.context = context;
    }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
        if (opcode == Opcodes.ILOAD || opcode == Opcodes.LLOAD || opcode == Opcodes.FLOAD || opcode == Opcodes.DLOAD
                || opcode == Opcodes.ALOAD) { // if load detected and a replacment for this variable has been chosen
            if (!(varIndex.contains(var))) {
                varIndex.add(var);
                switch (opcode) {
                case Opcodes.ILOAD:
                    varType.add("Integer");
                    break;
                case Opcodes.DLOAD:
                    varType.add("Double");
                    break;
                case Opcodes.FLOAD:
                    varType.add("Float");
                    break;
                case Opcodes.LLOAD:
                    varType.add("Long");
                    break;
                case Opcodes.ALOAD:
                    varType.add("Object");
                    break;
                }
            }

            int index = varIndex.indexOf(var); // get index of the variable in our saved variable list
            int typeCount = 0;
            for (int i = 0; i < varType.size(); i++) {
                if (varType.get(i).equals(varType.get(index))) {
                    typeCount++;
                }
            }

            if (typeCount > 1) {
                Random rand = new Random();
                n = rand.nextInt(varIndex.size()) + 0; // generate random index for replacment of variable

                while (!(varType.get(n).equals(varType.get(index))) || n == index) { // if random selection is the
                                                                                     // current variable OR selected one
                                                                                     // does not match type
                    n = rand.nextInt(varIndex.size()) + 0; // select a new variable at random
                }

                final MutationIdentifier newId = this.context.registerMutation(this.factory,
                        "Replaced " + varType.get(index) + " with " + varType.get(n));
                if (this.context.shouldMutate(newId)) {
                    super.visitVarInsn(opcode, varIndex.get(n)); // find index of replacment var in our list, and use
                                                                 // that index to find index of the replacment variable
                                                                 // in localvar table
                } else {
                    super.visitVarInsn(opcode, var);
                }
            } else {

                super.visitVarInsn(opcode, var);
            }
        } else if (opcode == Opcodes.ISTORE || opcode == Opcodes.LSTORE || opcode == Opcodes.FSTORE
                || opcode == Opcodes.DSTORE || opcode == Opcodes.ASTORE) {
            if (!(varIndex.contains(var))) {
                varIndex.add(var);
                switch (opcode) {
                case Opcodes.ISTORE:
                    varType.add("Integer");
                    break;
                case Opcodes.DSTORE:
                    varType.add("Double");
                    break;
                case Opcodes.FSTORE:
                    varType.add("Float");
                    break;
                case Opcodes.LSTORE:
                    varType.add("Long");
                    break;
                case Opcodes.ASTORE:
                    varType.add("Object");
                    break;
                }
            } else {

                varIndex.set(varIndex.indexOf(var), var);
                switch (opcode) {
                case Opcodes.ISTORE:
                    varType.set(varIndex.indexOf(var), "Integer");
                    break;
                case Opcodes.DSTORE:
                    varType.set(varIndex.indexOf(var), "Double");
                    break;
                case Opcodes.FSTORE:
                    varType.set(varIndex.indexOf(var), "Float");
                    break;
                case Opcodes.LSTORE:
                    varType.set(varIndex.indexOf(var), "Long");
                    break;
                case Opcodes.ASTORE:
                    varType.set(varIndex.indexOf(var), "Object");
                    break;
                }
            }
            super.visitVarInsn(opcode, var);
        } else {
            super.visitVarInsn(opcode, var);
        }

    }
}