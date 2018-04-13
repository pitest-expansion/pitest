
package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

import java.util.Optional;

import org.objectweb.asm.MethodVisitor;
//import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.engine.MutationIdentifier;
//import org.pitest.mutationtest.engine.MutationIdentifier;
//import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
//import org.pitest.mutationtest.engine.gregor.InsnSubstitution;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.mutationtest.engine.gregor.GregorMutationEngine;
import org.pitest.mutationtest.engine.gregor.GregorMutater;

public enum CallAnotherOverloadingMethod implements MethodMutatorFactory {

    REPLACE_WITH_OVERLOADING_METHOD;

    @Override
    public MethodVisitor create(MutationContext context, MethodInfo methodInfo, MethodVisitor methodVisitor,
            ClassByteArraySource byteSource) {
        ReplaceWithOverloadingMethod mv = new ReplaceWithOverloadingMethod(this, context, methodVisitor);
        mv.setByteSource(byteSource);
        return mv;
    }

    @Override
    public String getGloballyUniqueId() {
        return this.getClass().getName() + "_" + name();
    }

    @Override
    public String getName() {
        return "Replaced with overloading method - " + name();
    }
    /*
     * TODO M2 mutation Invoking object method are called using INVOKEVIRTUAL,
     * INVOKESPECIAL, INVOKEINTERFACE, INVOKESTATIC. Overloading methods are method
     * that have the same name and return type, but different parameters.
     * INVOKEVIRTUAL utd/Add.add(III)I INVOKEVIRTUAL utd/Add.add(II)I
     * 
     * Meaning: static method vs non-static method can overload each other ->
     * INVOKEVIRTUAL and INVOKESTATIC package name is the same (overload, not
     * override) same classname and method name (utd/Add.add) same return type
     * different descriptor/parameter.
     * 
     * This should work with constructor as well.
     * 
     * How do I dissect the method descriptor?
     */

}

class ReplaceWithOverloadingMethod extends MethodVisitor {
    private final MethodMutatorFactory factory;
    private final MutationContext context;
    private ClassByteArraySource byteSource;

    ReplaceWithOverloadingMethod(final MethodMutatorFactory factory, final MutationContext context,
            final MethodVisitor mv) {
        super(Opcodes.ASM6, mv);
        this.factory = factory;
        this.context = context;
    }

    /**
     * Should return an optional object to analyze the byte source. This bytesource can only be extracted with a class name.
     * @param clazz A string to help extract data from bytesource.
     * @return
     */
    private  Optional<byte[]> getBytes(String clazz){
        return Optional;
    }
    public ClassByteArraySource getByteSource() {
        return this.byteSource;
    }

    public void setByteSource(ClassByteArraySource byteSource) {
        this.byteSource = byteSource;
    }

    /**
     * If the INVOKE keywords exist, then start replacing it with other overloading
     * methods.
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == Opcodes.INVOKESPECIAL || opcode == Opcodes.INVOKEINTERFACE || opcode == Opcodes.INVOKESTATIC
                || opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEDYNAMIC) {
            final MutationIdentifier muID = this.context.registerMutation(factory,
                    "Replaced with overloading method here.");

            if (this.context.shouldMutate(muID)) {
                replaceMethodDescriptorMutation(opcode, owner, name, desc, itf);
            }
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    /**
     * Get class info
     */

/**
 * This method actually replace the method descriptor and method signature
 * @param opcode The opcode to write into bytecode.
 * @param owner Fully qualified package name.
 * @param name Fully qualified method name
 * @param desc Method description
 * @param itf method access flag. Usually 0??? Not sure.
 */
    private void replaceMethodDescriptorMutation(int opcode, String owner, String name, String desc, boolean itf) {

        // I should use bytesource from GregorEngineFactory.java
        // -> GregorMutationEngine.java -> GregorMutater.java

        /*
         * 
I changed the method signature to include Class
         */

    }

}
