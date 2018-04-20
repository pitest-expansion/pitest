
package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.FrameOptions;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ComputeClassWriter;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.mutators.augmentation.ScanMethodDescriptorVisitor;

import bsh.org.objectweb.asm.ClassVisitor;
import bsh.org.objectweb.asm.CodeVisitor;

public enum CallAnotherOverloadingMethod implements MethodMutatorFactory {

    REPLACE_WITH_OVERLOADING_METHOD;

    @Override
    public MethodVisitor create(MutationContext context, MethodInfo methodInfo, MethodVisitor methodVisitor,
            ClassByteArraySource byteSource) {
        return new ReplaceWithOverloadingMethod(this, context, methodVisitor, byteSource);
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
    private ClassByteArraySource byteSource;
    private List<String> descriptorList;

    ReplaceWithOverloadingMethod(final MethodMutatorFactory factory, final MutationContext context,
            final MethodVisitor mv, ClassByteArraySource byteSource) {
        super(Opcodes.ASM6, mv);
        this.factory = factory;
        this.context = context;
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
                String methodToScan = name;
                getOverloadingMethod(methodToScan, owner, name, desc, itf);
                replaceMethodDescriptorMutation(opcode, owner, name, desc, itf);
                
                visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, name, desc, itf);
            }
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    /**
     * Check if this method invokes a constructor (uses INVOKESPECIAL and init in
     * bytecode)
     * 
     * @param name
     * @return
     */
    public boolean isInvokespecial(String name) {
        return name.equals("<init>");
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

    private void getOverloadingMethod(String methodToScan, String owner, String name, String desc, boolean itf) {

        ClassWriter cw = new ClassWriter(0);
        Optional<byte[]> bytes = this.returnByteArray();
        ClassReader cr = new ClassReader(bytes.get());

        // implement scan in an MV inside CV here
        ScanClassAdapter cv = new ScanClassAdapter(cw, methodToScan);
        cr.accept(cv, 0);
        descriptorList = cv.getMethodDescriptorList();

    }

    public ClassByteArraySource getByteSource() {
        return this.byteSource;
    }

    public void setByteSource(ClassByteArraySource byteSource) {
        this.byteSource = byteSource;
    }

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
