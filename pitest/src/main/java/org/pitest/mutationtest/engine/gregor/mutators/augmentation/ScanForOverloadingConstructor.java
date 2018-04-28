
package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import bsh.This;

import org.objectweb.asm.MethodVisitor;

/**
 * This scans the class to get the method descriptor. Then return the list.
 * 
 * @author khoa
 *
 */
public class ScanForOverloadingConstructor extends ClassVisitor {

    private String constructorToScan;

    private List<String> descriptorList = new ArrayList<String>();
    private List<Integer> accessTypeList = new ArrayList<Integer>();
    private List<Boolean> staticAccessList = new ArrayList<Boolean>();
    private String oldDescriptor;

    public void setConstructorToScan(String constructorName) {
        this.constructorToScan = constructorName;
    }

    public void setOldDescriptor(String oldDescriptor) {
        this.oldDescriptor = oldDescriptor;
    }

    public ScanForOverloadingConstructor(ClassVisitor cv, String constructorToScan) {
        super(Opcodes.ASM6, cv);
        this.constructorToScan = constructorToScan;
    }

    /**
     * Scan for public, private, protected and static methods of the same name This
     * is a typical visitMethod: (ACC_PUBLIC, "<init>",
     * "(ILjava/lang/String;Ljava/lang/Object;)V,false)
     */

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        boolean isSynthetic = (access & Opcodes.ACC_SYNTHETIC) != 0;
        boolean isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
        boolean isPrivate = (access & Opcodes.ACC_PRIVATE) != 0;
        boolean isProtected = (access & Opcodes.ACC_PROTECTED) != 0;
        boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;

        boolean newIsInOld = oldConstainsNew(constructorToScan, desc);
        boolean oldIsInNew = newContainsOld(constructorToScan, desc);
        boolean sameLength = constructorToScan.length() == desc.length();
        if (isPublic && (newIsInOld || oldIsInNew) && !sameLength) {
            descriptorList.add(desc);
            accessTypeList.add(access);
        }
        return visitMethod(access, name, desc, signature, exceptions);
    }

    public boolean oldConstainsNew(String oldDesc, String newDesc) {
        if (oldDesc.length() < newDesc.length()) {
            return false;
        } else {
            String newDescTrim = newDesc.substring(0, newDesc.indexOf(")"));
            return oldDesc.contains(newDescTrim);
        }
    }

    public boolean newContainsOld(String oldDesc, String newDesc) {
        if (oldDesc.length() > newDesc.length()) {
            return false;
        } else {
            String oldDescTrim = oldDesc.substring(0, oldDesc.indexOf(")"));
            return newDesc.contains(oldDescTrim);
        }
    }

    public List<String> getMethodDescriptorList() {
        return descriptorList;
    }

    public List<Boolean> getStaticAccessList() {
        return staticAccessList;
    }

    public List<Integer> getAccessTypeList() {
        return accessTypeList;
    }

}
