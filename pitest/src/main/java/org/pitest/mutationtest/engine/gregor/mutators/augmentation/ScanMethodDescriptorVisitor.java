package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Implements the scan and replacement here
 * 
 * @author khoa
 *
 */
public class ScanMethodDescriptorVisitor extends MethodVisitor {

	private final MethodMutatorFactory factory;
	private final MutationContext context;
	private final MethodInfo methodInfo;
	private static final List<String> descriptorList = new Vector<String>();
	private static String methodNameToScan;

	public static String getMethodName() {
		return methodNameToScan;
	}

	public static void setMethodName(String methodName) {
		ScanMethodDescriptorVisitor.methodNameToScan = methodName;
	}

	public ScanMethodDescriptorVisitor(final MethodMutatorFactory factory, final MethodInfo methodInfo,
			final MutationContext context, final MethodVisitor delegateMethodVisitor) {
		super(Opcodes.ASM6, delegateMethodVisitor);
		this.factory = factory;
		this.methodInfo = methodInfo;
		this.context = context;
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		// scan for the same name, add it to hashmap

		super.visitFieldInsn(opcode, owner, name, desc);
	}

}
