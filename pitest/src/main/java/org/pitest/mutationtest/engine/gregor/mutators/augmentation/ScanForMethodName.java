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
public class ScanForMethodName extends ClassVisitor {

    private String methodNameToScan;
    private String oldDescriptor;
    private int accessTypeToScan;

    private String returnTypeToScan = methodNameToScan.substring(methodNameToScan.indexOf(")"));
    private List<String> methodNameList = new ArrayList<String>();
    public List<String> getMethodNameList() {
        return methodNameList;
    }

    private List<String> descriptorList = new ArrayList<String>();
    private List<Integer> accessTypeList = new ArrayList<Integer>();
    private List<Boolean> staticAccessList = new ArrayList<Boolean>();

    public void setAccessTypeToScan(int accessTypeToScan) {
        this.accessTypeToScan = accessTypeToScan;
    }

    public void setMethodNameToScan(String methodNameToScan) {
        this.methodNameToScan = methodNameToScan;
    }

    public void setReturnTypeToScan(String returnTypeToScan) {
        this.returnTypeToScan = returnTypeToScan;
    }

    public void setOldDescriptor(String oldDescriptor) {
        this.oldDescriptor = oldDescriptor;
    }

    public ScanForMethodName(ClassVisitor cv, String methodNameToScan) {
        super(Opcodes.ASM6, cv);
        this.methodNameToScan = methodNameToScan;
    }

    /**
     * Scan for methods with same access, same descriptor but different name.
     */

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

        boolean isSameAccessType = (access == accessTypeToScan);
        boolean isSameDescriptor = desc.equals(oldDescriptor);
        boolean isSameName = name.equals(methodNameToScan);

        if (isSameAccessType && isSameDescriptor && !isSameName) {
            methodNameList.add(name);
            accessTypeList.add(access);
            descriptorList.add(desc);
        }
        return visitMethod(access, name, desc, signature, exceptions);

    }

    public List<String> getMethodDescriptorList() {
        return methodNameList;
    }

    public List<Boolean> getStaticAccessList() {
        return staticAccessList;
    }

    public List<Integer> getAccessTypeList() {
        return accessTypeList;
    }

}
