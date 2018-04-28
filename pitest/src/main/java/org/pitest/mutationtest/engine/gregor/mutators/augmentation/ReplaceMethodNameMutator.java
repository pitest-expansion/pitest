package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.ZeroOperandMutation;

public enum ReplaceMethodNameMutator implements MethodMutatorFactory {

    REPLACE_METHOD_NAME;
    @Override
    public MethodVisitor create(MutationContext context, MethodInfo methodInfo, MethodVisitor methodVisitor,
            ClassByteArraySource byteSource) {

        return new ReplaceMethodName(this, methodInfo, context, methodVisitor, byteSource);
    }

    @Override
    public String getGloballyUniqueId() {
        return this.getClass().getName() + "_" + name();
    }

    @Override
    public String getName() {
        return "Replaced method " + name() + "with another method name." ;
    }

}

class ReplaceMethodName extends AbstractInsnMutator {
    private final MethodMutatorFactory factory;
    private final MutationContext context;
    private ClassByteArraySource byteSource;
    private int accessType;

    private List<String> descriptorList;
    private List<Integer> accessTypeList;
    private List<String> methodNameList;

    ReplaceMethodName(final MethodMutatorFactory factory, MethodInfo methodInfo, final MutationContext context,
            final MethodVisitor delegateMethodVisitor, ClassByteArraySource byteSource) {
        super(factory, methodInfo, context, delegateMethodVisitor, null);
        this.factory = factory;
        this.context = context;
        this.byteSource = byteSource;
    }

    @Override
    protected Map<Integer, ZeroOperandMutation> getMutations() {
        // TODO Auto-generated method stub
        return null;
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

    /**
     * Only check for INVOKESPECIAL (constructor invocation)
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {

        // Only normal invoke

        if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKESTATIC) {
            this.getOverloadingConstructor(opcode, owner, name, desc, itf);

            final MutationIdentifier muID = this.context.registerMutation(factory,
                    "Replaced" + name + " with overloading constructor here.");

            if (this.context.shouldMutate(muID)) {

                this.replaceConstructor(opcode, owner, name, desc, itf);

                // visitMethodInsn is called inside replaceMethodDescriptorMutation
            }
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    /**
     * Use the ASM Scanner pattern to scan the bytesource for the same class
     * 
     * @param opcode
     *            TODO
     * @param owner
     * @param name
     * @param desc
     * @param itf
     */
    private void getOverloadingConstructor(int opcode, String owner, String name, String desc, boolean itf) {

        ClassWriter cw = new ClassWriter(0);
        
        Optional<byte[]> bytes = this.returnByteArray();
        ClassReader cr = new ClassReader(bytes.get());

        // implement scan in an MV inside CV here
        ScanForMethodName cv = new ScanForMethodName(cw, name);
        cv.setMethodNameToScan(name);
        cv.setOldDescriptor(desc);
        
        if (opcode == Opcodes.INVOKESTATIC) {
            cv.setAccessTypeToScan(Opcodes.ACC_STATIC + Opcodes.ACC_PUBLIC);
        } else {
            cv.setAccessTypeToScan(Opcodes.ACC_PUBLIC);
        }

        cr.accept(cv, 0);
        descriptorList = cv.getMethodDescriptorList();
        accessTypeList = cv.getAccessTypeList();
        methodNameList = cv.getMethodNameList();

    }

    /**
     * This method actually replace the method name but keep the same descriptor and
     * access.
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
    private void replaceConstructor(int opcode, String owner, String name, String desc, boolean itf) {
        if (descriptorList.size() > 1) {
            Random rand = new Random();

            // pick a random constructor description here
            int newNameIndex = pickRandomMethodName(desc);
            String newName = descriptorList.get(newNameIndex);
            //int newAccess = accessTypeList.get(newNameIndex);

            super.visitMethodInsn(opcode, owner, newName, desc, itf);
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    /**
     * Pop parameters from the stack
     * 
     * @param param
     */
    public void popValues(String param) {
        if (param.equalsIgnoreCase("Z") || param.equalsIgnoreCase("C") || param.equalsIgnoreCase("B")
                || param.equalsIgnoreCase("S") || param.equalsIgnoreCase("I") || param.equalsIgnoreCase("F")) {
            super.visitInsn(Opcodes.POP);
        } else if (param.equalsIgnoreCase("D") || param.equalsIgnoreCase("J")) {
            super.visitInsn(Opcodes.POP2);
        } else {
            super.visitInsn(Opcodes.POP);
        }
    }

    /**
     * Come up with a default value of a certain type and push it onto the stack
     */
    public void pushValues(String param) {
        if (param.equalsIgnoreCase("Z") || param.equalsIgnoreCase("C") || param.equalsIgnoreCase("B")
                || param.equalsIgnoreCase("S") || param.equalsIgnoreCase("I")) {
            super.visitInsn(Opcodes.ICONST_0);
        } else if (param.equalsIgnoreCase("D")) {
            super.visitInsn(Opcodes.DCONST_0);
        } else if (param.equalsIgnoreCase("F")) {
            super.visitInsn(Opcodes.FCONST_0);
        } else if (param.equalsIgnoreCase("J")) {
            super.visitInsn(Opcodes.LCONST_0);
        } else {
            // objects or array types
            super.visitInsn(Opcodes.ACONST_NULL);
        }
    }

    /**
     * Pick a random method descriptor from the list
     * 
     * @param oldDesc
     * @return
     */
    public int pickRandomMethodName(String oldDesc) {
        Random rand = new Random();
        // pick a random constructor description here
        while (true) {
            int n = rand.nextInt(descriptorList.size());
            if (!oldDesc.equalsIgnoreCase(descriptorList.get(n))) {
                return n;

            }
        }

    }

}
