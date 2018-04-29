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
    private ArrayList<String> methodDescriptorList = new ArrayList<String>();
    private ArrayList<Integer> accessTypeList = new ArrayList<Integer>();
    private ArrayList<Boolean> staticAccessList = new ArrayList<Boolean>();
    private String oldDescriptor;

    public void setOldDescriptor(String oldDescriptor) {
        this.oldDescriptor = oldDescriptor;
    }

    public ScanForOverloadingMethod( String methodNameToScan) {
        super(Opcodes.ASM6);
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
            ArrayList<String> oldArgs = new ArrayList<String>();
            ArrayList<String> newArgs = new ArrayList<String>();
            
            for (int i = 0; i < oldDescriptor.length(); i++) {
                if (oldDescriptor.charAt(i) == 'L') {
                    String oldsub = oldDescriptor.substring(i);
                    oldsub = oldsub.substring(0,oldsub.indexOf(";") + 1);
                    oldArgs.add(oldsub);
                    i += oldsub.length() - 1;
                } else if (oldDescriptor.charAt(i) == '[') {
                    String oldsub = "";
                    int y = i;
                    while (oldDescriptor.charAt(y) == '[') {
                        oldsub += oldDescriptor.charAt(y);
                        y++;
                    }
                    
                    if (oldDescriptor.charAt(y) == 'L') {
                        String oldsub2 = oldDescriptor.substring(y);
                        oldsub2 = oldsub2.substring(0,oldsub2.indexOf(";") + 1);
                        oldsub += oldsub2;
                    } else {
                        oldsub += oldDescriptor.charAt(y);
                    }
                    oldArgs.add(oldsub);
                    i += oldsub.length() - 1;
                } else {
                    oldArgs.add(oldDescriptor.charAt(i) + "");
                }
            }

            if (oldArgs.size() >= 3) {
                oldArgs.remove(oldArgs.size() - 1);
                oldArgs.remove(oldArgs.size() - 1);
                oldArgs.remove(0);
            }

            for (int i = 0; i < desc.length(); i++) {
                char nextChar = desc.charAt(i);
                if (nextChar == 'L') {
                    String oldsub = desc.substring(i);
                    oldsub = oldsub.substring(0 ,oldsub.indexOf(";") + 1);
                    newArgs.add(oldsub);
                    i += oldsub.length() - 1;
                } else if (nextChar == '[') {
                    String oldsub = "";
                    int y = i;
                    while (desc.charAt(y) == '[') {
                        oldsub += desc.charAt(y);
                        y++;
                    }
                    
                    if (desc.charAt(y) == 'L') {
                        String oldsub2 = desc.substring(y);
                        oldsub2 = oldsub2.substring(0,oldsub2.indexOf(";") + 1);
                        oldsub += oldsub2;
                    } else {
                        oldsub += desc.charAt(y);
                    }
                    newArgs.add(oldsub);
                    i += oldsub.length() - 1;
                } else {
                    newArgs.add(desc.charAt(i) + "");
                }
            }

            if (newArgs.size() >= 3) {
                newArgs.remove(newArgs.size() - 1);
                newArgs.remove(newArgs.size() - 1);
                newArgs.remove(0);
            }

            int minLength = Math.min(oldArgs.size(), newArgs.size());
            boolean sameArgs = true;

            for (int i = 0; i < minLength; i++) {
                if (!(oldArgs.get(i).equals(newArgs.get(i)))) {
                    sameArgs = false;
                    break;
                }
            }
        }

        return super.visitMethod(access, name, desc, signature, exceptions);
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
