package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.MethodVisitor;

/**
 * This scans the class to get the method descriptor. Then return the list.
 * 
 * @author khoa
 *
 */
public class ScanClassAdapter extends ClassVisitor {

    private static String methodToScan;
    private List<String> methodDescriptorList = new ArrayList<String>();

    public ScanClassAdapter(ClassVisitor cv, String methodToScan) {
        super(Opcodes.ASM6, cv);
        this.methodToScan = methodToScan;
    }

    /**
     * access: 000 name: method name desc: method descriptor
     * 
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        boolean isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
        boolean isPrivate = (access & Opcodes.ACC_PRIVATE) != 0;
        boolean isProtected = (access & Opcodes.ACC_PROTECTED) != 0;

        if (name.equalsIgnoreCase(methodToScan)) {
            String temp = desc.substring(desc.indexOf("("), desc.indexOf(")"));
            methodDescriptorList.add(temp);
        }

        // I don't need to implement another visitor. Just scan here and return the
        // list.
        return visitMethod(access, name, desc, signature, exceptions);
    }

    public List<String> getMethodDescriptorList() {
        return methodDescriptorList;
    }

}
