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

    private static String methodNameToScan;
    private String returnTypeToScan = methodNameToScan.substring(methodNameToScan.indexOf(")"));
    private List<String> methodDescriptorList = new ArrayList<String>();
    private List<Integer> accessTypeList = new ArrayList<Integer>();
    private List<Boolean> staticAccessList = new ArrayList<Boolean>();

    public ScanClassAdapter(ClassVisitor cv, String methodNameToScan) {
        super(Opcodes.ASM6, cv);
        this.methodNameToScan = methodNameToScan;
    }

    /**
     * Scan for public, private, protected and static methods of the same name
     * access: public private protected name: method name desc: method descriptor
     * 
     */

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        boolean isSynthetic = (access & Opcodes.ACC_SYNTHETIC) != 0;
        String descReturnType = desc.substring(desc.indexOf(")"));

        if (name.equalsIgnoreCase(methodNameToScan) && !isSynthetic
                && descReturnType.equalsIgnoreCase(returnTypeToScan)) {
            // check method access: public, private or protected. Static method might cause
            // a problem.
            // Some method access is: ACC_PUBLIC + ACC_STATIC
            boolean isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
            boolean isPrivate = (access & Opcodes.ACC_PRIVATE) != 0;
            boolean isProtected = (access & Opcodes.ACC_PROTECTED) != 0;
            boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;

            methodDescriptorList.add(desc);
            accessTypeList.add(access);
            if (isStatic) {
                staticAccessList.add(true);
            } else {
                staticAccessList.add(false);
            }
        }

        return visitMethod(access, name, desc, signature, exceptions);
    }

    public List<String> getMethodDescriptorList() {
        return methodDescriptorList;
    }

    public List<Boolean> getStaticAccessList() {
        return staticAccessList;
    }

    public List<Integer> getAccessTypeList() {
        return accessTypeList;
    }
}
