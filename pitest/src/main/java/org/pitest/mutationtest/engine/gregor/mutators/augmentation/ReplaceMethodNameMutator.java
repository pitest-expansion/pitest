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
        return "Replaced method " + name() + "with another method name.";
    }

}

class ReplaceMethodName extends MethodVisitor {
    private final MethodMutatorFactory factory;
    private final MutationContext context;
    private ClassByteArraySource byteSource;
    private int accessType;
    private ArrayList<String> methodNameList;

    ReplaceMethodName(final MethodMutatorFactory factory, MethodInfo methodInfo, final MutationContext context,
            final MethodVisitor delegateMethodVisitor, ClassByteArraySource byteSource) {
        super(Opcodes.ASM6, delegateMethodVisitor);
        this.factory = factory;
        this.context = context;
        this.byteSource = byteSource;
 
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
     * Only check for INVOKESPECIAL (constructor invocation)
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {

        // Only normal invoke

        if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKESTATIC) {
            this.scanMethodName(opcode, owner, name, desc, itf);
            String newName = this.getRandomMethodName(opcode, owner, name, desc, itf);

            final MutationIdentifier muID = this.context.registerMutation(factory,
                    "Replaced" + name + " with overloading constructor here.");

            if (this.context.shouldMutate(muID)) {
                super.visitMethodInsn(opcode, owner, newName, desc, itf);
            }
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    /**
     * Use the ASM Scanner pattern to scan the bytesource for the same class
     * 
     * @param opcode
     * @param owner
     * @param name
     * @param desc
     * @param itf
     */
    private void scanMethodName(int opcode, String owner, String name, String desc, boolean itf) {
        // implement scan in an MV inside CV here

        Optional<byte[]> bytes = this.returnByteArray();
        ClassReader cr = new ClassReader(bytes.get());
        ScanForMethodName cv = new ScanForMethodName(name);
        cv.setMethodNameToScan(name);
        cv.setOldDescriptor(desc);

        if (opcode == Opcodes.INVOKESTATIC) {
            System.out.println(bytes.get());
            cv.setAccessTypeToScan(Opcodes.ACC_STATIC + Opcodes.ACC_PUBLIC);
            System.out.println(bytes.get());
        } else {
            cv.setAccessTypeToScan(Opcodes.ACC_PUBLIC);
            System.out.println(bytes.get());
        }
        System.out.println(bytes.get());
        cr.accept(cv, 0);
        methodNameList = cv.getMethodNameList();

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
    private String getRandomMethodName(int opcode, String owner, String name, String desc, boolean itf) {
        if (methodNameList.size() > 1) {

            // pick a random constructor description here
            int newNameIndex = pickRandomMethodName(name);
            return methodNameList.get(newNameIndex);
            // int newAccess = accessTypeList.get(newNameIndex);

        } else {
            return name;
        }
    }

    /**
     * Pick a random index from the list
     * 
     * @param oldName
     * @return
     */
    public int pickRandomMethodName(String oldName) {
        Random rand = new Random();
        // pick a random constructor description here
        while (true) {
            int n = rand.nextInt(methodNameList.size());
            if (!oldName.equalsIgnoreCase(methodNameList.get(n))) {
                return n;

            }
        }

    }

}
