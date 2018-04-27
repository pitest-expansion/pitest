
package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.FrameOptions;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ComputeClassWriter;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
import org.pitest.mutationtest.engine.gregor.AbstractJumpMutator;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.ZeroOperandMutation;
import org.pitest.mutationtest.engine.gregor.mutators.augmentation.ScanClassAdapter;

import bsh.org.objectweb.asm.ClassVisitor;
import bsh.org.objectweb.asm.CodeVisitor;

public enum CallAnotherOverloadingMethod implements MethodMutatorFactory {

    CALL_ANOTHER_OVERLOADING_METHOD;

    @Override
    public MethodVisitor create(MutationContext context, MethodInfo methodInfo, MethodVisitor methodVisitor,
            ClassByteArraySource byteSource) {
        return new ReplaceWithOverloadingMethod(this, methodInfo, context, methodVisitor, byteSource);
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

class ReplaceWithOverloadingMethod extends AbstractInsnMutator {
    private static final Logger LOGGER = Logger.getLogger(ReplaceWithOverloadingMethod.class.getName());
    private final MethodMutatorFactory factory;
    private final MutationContext context;
    private ClassByteArraySource byteSource;
    private List<String> descriptorList;
    private List<Integer> accessTypeList;
    private List<Boolean> staticTypeList;
    int n;

    public void setAccessTypeList(List<Integer> accessTypeList) {
        this.accessTypeList = accessTypeList;
    }

    ReplaceWithOverloadingMethod(final MethodMutatorFactory factory, MethodInfo methodInfo,
            final MutationContext context, final MethodVisitor delegateMethodVisitor, ClassByteArraySource byteSource) {
        super(factory, methodInfo, context, delegateMethodVisitor, null);
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

        // don't deal with INVOKESPECIAL, INVOKEDYNAMIC right now.

        if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKESTATIC) {
            final MutationIdentifier muID = this.context.registerMutation(factory,
                    "Replaced with overloading method here.");

            if (this.context.shouldMutate(muID)) {

                this.getOverloadingMethod(owner, name, desc, itf);
                this.replaceMethodDescriptorMutation(opcode, owner, name, desc, itf);

                // visitMethodInsn is called inside replaceMethodDescriptorMutation
            }
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    /**
     * Use the ASM Scanner pattern to scan the bytesource for the same class
     * 
     * @param owner
     * @param name
     * @param desc
     * @param itf
     */
    private void getOverloadingMethod(String owner, String name, String desc, boolean itf) {

        ClassWriter cw = new ClassWriter(0);
        Optional<byte[]> bytes = this.returnByteArray();
        ClassReader cr = new ClassReader(bytes.get());

        // implement scan in an MV inside CV here
        ScanClassAdapter cv = new ScanClassAdapter(cw, name);
        cv.setOldDescriptor(desc);
        cr.accept(cv, 0);
        descriptorList = cv.getMethodDescriptorList();
        accessTypeList = cv.getAccessTypeList();
        staticTypeList = cv.getStaticAccessList();
        LOGGER.log(Level.FINE, descriptorList.toString());
        LOGGER.log(Level.FINE, accessTypeList.toString());
        LOGGER.log(Level.FINE, staticTypeList.toString());
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
        if (descriptorList.size() > 1) {
            String descReturnType = extractReturnType(desc);

            Random rand = new Random();

            int index = descriptorList.indexOf(desc);
            n = rand.nextInt(descriptorList.size()) + 0;

            while (n == index)
            // if random selection is the current variable
            {
                n = rand.nextInt(descriptorList.size()) + 0;// select a new variable at random
            }

            final MutationIdentifier newId = this.context.registerMutation(this.factory,
                    "Replaced " + descriptorList.get(index) + " with " + descriptorList.get(n));
            if (this.context.shouldMutate(newId)) {
                // find index of replacment var in our list, and use
                // that index to find index of the replacment variable
                // in local var table

                int newOpcode = opcode;
                String newOwner = owner;
                String newName = name;
                String newDesc = descriptorList.get(n);

                if ((accessTypeList.get(n) & Opcodes.ACC_STATIC) != 0) {
                    newOpcode = Opcodes.INVOKESTATIC;
                }

                this.replaceStack(desc, newDesc);
                // manipulate the stack here
                super.visitMethodInsn(newOpcode, newOwner, newName, newDesc, itf);
            }
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    /**
     * Relace the old stack with a new stack. It is guaranteed that the old stack
     * contains the new stack OR the new stack contains the old stack
     * 
     * @param oldDesc
     * @param newDesc
     */
    public void replaceStack(String oldDesc, String newDesc) {
        if (newContainsOld(oldDesc,newDesc)) { // newdesc is longer than old
            int numOfPops = newDesc.length() - oldDesc.length();
            newDesc = newDesc.substring(1, newDesc.length() - 2);
            int index = newDesc.length() - 1;
            for (int i = 0; i < numOfPops; i++) {
                pushValues(""+newDesc.toCharArray()[index - i]);
            }
        } else if (oldContainsNew(oldDesc,newDesc)) { // olddesc is longer than new
            int numOfPops = oldDesc.length() - newDesc.length();
            oldDesc = oldDesc.substring(1, oldDesc.length() - 2);
            int index = oldDesc.length() - 1;
            for (int i = 0; i < numOfPops; i++) {
                popValues(""+oldDesc.toCharArray()[index - i]);
            }
        }
    }

    public boolean oldContainsNew(String oldDesc, String newDesc) {

        // add a method to check if old descriptor contains new descriptor: (III)I -> (I
        // or (II
        return oldDesc.length() > newDesc.length();

    }

    public boolean newContainsOld(String oldDesc, String newDesc) {

        // add a method to check if new descriptor contains old descriptor
        // old: (ID)I
        // new: (IDD)I

        return oldDesc.length() < newDesc.length();
    }

    /**
     * Pop parameters from the stack
     * 
     * @param param
     */
    public void popValues(String param) {
        if (param.equalsIgnoreCase("I") || param.equalsIgnoreCase("F")) {
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
        if (param.equalsIgnoreCase("I")) {
            super.visitInsn(Opcodes.ICONST_0);
        } else if (param.equalsIgnoreCase("D")) {
            super.visitInsn(Opcodes.DCONST_0);
        } else if (param.equalsIgnoreCase("F")) {
            super.visitInsn(Opcodes.FCONST_0);
        } else if (param.equalsIgnoreCase("J")) {
            super.visitInsn(Opcodes.LCONST_0);
        } else {
            super.visitInsn(Opcodes.ACONST_NULL);
        }
    }

    /**
     * For this string: (III)D, return ")D". In ASM, this means get the return type
     * of a method.
     * 
     * @param in
     * @return
     */
    public String extractReturnType(String in) {
        return in.substring(in.indexOf(")"));
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

    public ClassByteArraySource getByteSource() {
        return this.byteSource;
    }

    public void setByteSource(ClassByteArraySource byteSource) {
        this.byteSource = byteSource;
    }

    @Override
    protected Map<Integer, ZeroOperandMutation> getMutations() {
        // TODO Auto-generated method stub
        return null;
    }

}
