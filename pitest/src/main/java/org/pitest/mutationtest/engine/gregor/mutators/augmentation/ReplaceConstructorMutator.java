package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.regex.Matcher;
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
import org.pitest.sequence.Match;

/**
 * Static class doesn't make sense except in Math class. So we can ignore
 * ACC_STATIC.
 * 
 * @author khoa
 *
 */
public enum ReplaceConstructorMutator implements MethodMutatorFactory {

    REPLACE_CONSTRUCTOR_DESCRIPTOR;
    @Override
    public MethodVisitor create(MutationContext context, MethodInfo methodInfo, MethodVisitor methodVisitor,
            ClassByteArraySource byteSource) {
        return new ReplaceConstructor(this, methodInfo, context, methodVisitor, byteSource);
    }

    @Override
    public String getGloballyUniqueId() {
        return this.getClass().getName() + "_" + " name.";
    }

    @Override
    public String getName() {
        return "Replaced constructor " + name() + "with another constructor in the same class.";
    }

}

class ReplaceConstructor extends AbstractInsnMutator {

    private final MethodMutatorFactory factory;
    private final MutationContext context;
    private ClassByteArraySource byteSource;

    private List<String> descriptorList;
    private List<Integer> accessTypeList;

    ReplaceConstructor(final MethodMutatorFactory factory, MethodInfo methodInfo, final MutationContext context,
            final MethodVisitor delegateMethodVisitor, ClassByteArraySource byteSource) {
        super(factory, methodInfo, context, delegateMethodVisitor, byteSource);
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

        // either INVOKESPECIAL or init is ok

        if (opcode == Opcodes.INVOKESPECIAL && isInvokespecial(desc)) {
            this.getOverloadingConstructor(owner, name, desc, itf);

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
     * @param owner
     * @param name
     * @param desc
     * @param itf
     */
    private void getOverloadingConstructor(String owner, String name, String desc, boolean itf) {

        ClassWriter cw = new ClassWriter(0);
        Optional<byte[]> bytes = this.returnByteArray();
        ClassReader cr = new ClassReader(bytes.get());

        // implement scan in an MV inside CV here
        ScanForOverloadingConstructor cv = new ScanForOverloadingConstructor(cw, name);
        cv.setConstructorToScan(name);
        cv.setOldDescriptor(desc);
        cr.accept(cv, 0);
        descriptorList = cv.getMethodDescriptorList();
        accessTypeList = cv.getAccessTypeList();

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
    private void replaceConstructor(int opcode, String owner, String name, String desc, boolean itf) {
        if (descriptorList.size() > 1) {
            Random rand = new Random();

            // pick a random constructor description here
            int newDescIndex = pickRandomConstructor(desc);
            String newDesc = descriptorList.get(newDescIndex);
            int newAccess = accessTypeList.get(newDescIndex);

            // manipulate stack here
            manipulateStack(desc, newDesc);
            super.visitMethodInsn(opcode, owner, name, newDesc, itf);
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    /**
     * find the parameters to push or pop. Put them in an array. Then call push and
     * pop function.
     * 
     * @param oldDesc
     * @param newDesc
     */
    public void manipulateStack(String oldDesc, String newDesc) {
        if (oldDesc.length() < newDesc.length()) {
            String paramListToPush = newDesc.substring(oldDesc.indexOf(")"), newDesc.indexOf(")"));
            List<String> pushList = new ArrayList<String>();
            int index = 0;
            while (index < paramListToPush.length()) {
                Pattern oneWord = Pattern.compile("[ZCBSIF]");
                Pattern twoWord = Pattern.compile("[JD]");
                Pattern arrayPrimitiveType = Pattern.compile("\\[+[ZCBSIFJD]");
                Pattern arrayObject = Pattern.compile("\\[+[L]");
                {
                    char temp = paramListToPush.charAt(index);
                    //

                }
            }

        } else {
            String paramListToPop = oldDesc.substring(newDesc.indexOf(")"), oldDesc.indexOf(")"));
            List<String> popList = new ArrayList<String>();
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
    public int pickRandomConstructor(String oldDesc) {
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
