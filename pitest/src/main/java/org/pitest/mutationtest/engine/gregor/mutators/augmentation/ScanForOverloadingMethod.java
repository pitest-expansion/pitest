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
public class ScanForOverloadingMethod extends ClassVisitor {

    private static String methodNameToScan;

    private String returnTypeToScan = methodNameToScan.substring(methodNameToScan.indexOf(")"));
    private List<String> methodDescriptorList = new ArrayList<String>();
    private List<Integer> accessTypeList = new ArrayList<Integer>();
    private List<Boolean> staticAccessList = new ArrayList<Boolean>();
    private String oldDescriptor;

    public void setOldDescriptor(String oldDescriptor) {
        this.oldDescriptor = oldDescriptor;
    }

    public ScanForOverloadingMethod(ClassVisitor cv, String methodNameToScan) {
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
        boolean isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
        boolean isPrivate = (access & Opcodes.ACC_PRIVATE) != 0;
        boolean isProtected = (access & Opcodes.ACC_PROTECTED) != 0;
        boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;
        String descReturnType = desc.substring(desc.indexOf(")"));

        // if the method name is the same, method is not synthetic and it is public, the
        // return type is the same
        if (name.equalsIgnoreCase(methodNameToScan) && !isSynthetic && isPublic
                && descReturnType.equalsIgnoreCase(returnTypeToScan)) {

            // If one descriptor contains the other descriptor (has similar parameters
            ArrayList<Character> oldArgs = new ArrayList<Character>();
            ArrayList<Character> newArgs = new ArrayList<Character>();
            for (int i = 0; i < oldDescriptor.length(); i++) {
                oldArgs.add(oldDescriptor.charAt(i));
            }
            oldArgs.remove(oldArgs.size() - 1);
            oldArgs.remove(oldArgs.size() - 1);
            oldArgs.remove(0);

            for (int i = 0; i < desc.length(); i++) {
                newArgs.add(desc.charAt(i));
            }
            newArgs.remove(newArgs.size() - 1);
            newArgs.remove(newArgs.size() - 1);
            newArgs.remove(0);
            int minLength = Math.min(oldArgs.size(), newArgs.size());
            boolean sameArgs = true;

            for (int i = 0; i < minLength; i++) {
                if (!(oldArgs.get(i).equals(newArgs.get(i)))) {
                    sameArgs = false;
                    break;
                }
            }

            if (sameArgs) {
                // check method access: public, private or protected. Static method might cause
                // a problem.
                // Some method access is: ACC_PUBLIC + ACC_STATIC

                methodDescriptorList.add(desc);
                accessTypeList.add(access);
                if (isStatic) {
                    staticAccessList.add(true);
                } else {
                    staticAccessList.add(false);
                }
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
