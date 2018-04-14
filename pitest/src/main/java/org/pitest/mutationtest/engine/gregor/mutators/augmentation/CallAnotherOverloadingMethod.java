
package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
//import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.FrameOptions;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ComputeClassWriter;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
//import org.pitest.mutationtest.engine.MutationIdentifier;
//import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
//import org.pitest.mutationtest.engine.gregor.InsnSubstitution;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutatingClassVisitor;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.util.ResourceFolderByteArraySource;

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

}

class ReplaceWithOverloadingMethod extends MethodVisitor {
    private final MethodMutatorFactory factory;
    private final MutationContext context;
    private ResourceFolderByteArraySource byteSource;
    private final Map<String, String> computeCache = new HashMap<>();

    ReplaceWithOverloadingMethod(final MethodMutatorFactory factory, final MutationContext context,
            final MethodVisitor mv) {
        super(Opcodes.ASM6, mv);
        this.factory = factory;
        this.context = context;
    }

    /**
     * This uses getBytes method of ResourceFolderByteArraySource. It uses a
     * className to get the byte[] for a given class.
     * 
     * @return An Option<byte[]> that contains information about this class. Now how
     *         to use it?
     */
    private Optional<byte[]> returnByteArray() {
        return byteSource.getBytes(this.context.getClassInfo().getName());
    }

    private void getOverloadingMethod() {

        final Optional<byte[]> bytes = this.returnByteArray();
        final ClassReader reader = new ClassReader(bytes.get());
        final ClassWriter w = new ComputeClassWriter(this.byteSource, this.computeCache,
                FrameOptions.pickFlags(bytes.get()));
        
        final MutatingClassVisitor mca = new MutatingClassVisitor(w, context, filterMethods(),
                FCollection.filter(this.mutators, isMutatorFor(id)), this.byteSource);
        reader.accept(mca, ClassReader.EXPAND_FRAMES);
        final List<MutationDetails> details = context.getMutationDetails(context.getTargetMutation().get());
        return new Mutant(details.get(0), w.toByteArray());
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
     * 
     * @param opcode
     *            The opcode to write into bytecode.
     * @param owner
     *            Fully qualified package name.
     * @param name
     *            Fully qualified method name
     * @param desc
     *            Method description
     * @param itf
     *            method access flag. Usually 0??? Not sure.
     */
    private void replaceMethodDescriptorMutation(int opcode, String owner, String name, String desc, boolean itf) {

    }

}

/*
 * TODO I got byteSource. Now look in GregorMutater.java to see how to use it at
 * the mutator level.
 */