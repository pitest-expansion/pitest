package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

import org.objectweb.asm.MethodVisitor;
import org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.engine.MutationIdentifier;
//import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
//import org.pitest.mutationtest.engine.gregor.InsnSubstitution;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
/*
 * M1 mutation
 */

/* M1 mutation, check for null before dereferencing.
 * 
 * field dereferencing are called using GETFIELD in java bytecode
 * Loading objects are loaded using ALOAD and ALOAD_n 
 * This code stops at GETFIELD and PUTFIELD. We skip ALOAD because 
 *  
 */
public enum CheckNullObjectMutator implements MethodMutatorFactory {

	CHECK_NULL_OBJECT_MUTATOR;

	/*
	 * Create a MethodVisitor class to add null check (CheckNullObjectVisitor)
	 * 
	 * @see
	 * org.pitest.mutationtest.engine.gregor.MethodMutatorFactory#create(org.pitest.
	 * mutationtest.engine.gregor.MutationContext,
	 * org.pitest.mutationtest.engine.gregor.MethodInfo,
	 * org.objectweb.asm.MethodVisitor)
	 */
	@Override
	public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo, final MethodVisitor mv,
			ClassByteArraySource byteSource) {
		// return new CheckLDCOneWordOrTwoWord(this, methodInfo, context, mv);
		return new CheckNullObjectVisitor(this, methodInfo, context, mv);
	}

	@Override
	public String getGloballyUniqueId() {
		return this.getClass().getName() + "_" + name();
	}

	@Override
	public String getName() {
		return "Check if object is null - " + name();
	}

}

class CheckLDCOneWordOrTwoWord extends MethodVisitor {

	private final MethodMutatorFactory factory;
	private final MutationContext context;
	final MethodInfo methodInfo;

	CheckLDCOneWordOrTwoWord(final MethodMutatorFactory factory, final MethodInfo methodInfo,
			final MutationContext context, final MethodVisitor mv) {
		super(Opcodes.ASM6, mv);
		this.factory = factory;
		this.methodInfo = methodInfo;
		this.context = context;
	}

	@Override
	public void visitLdcInsn(Object cst) {
		super.visitLdcInsn(cst);
		if (cst instanceof Integer || cst instanceof Float) {
			CheckNullObjectVisitor a = new CheckNullObjectVisitor(factory, methodInfo, context, mv);
			a.setWordLength(1);
		}
		if (cst instanceof Double || cst instanceof Long) {
			CheckNullObjectVisitor a = new CheckNullObjectVisitor(factory, methodInfo, context, mv);
			a.setWordLength(2);
		}
	}
}

/*
 * Two ways to do this. One is using Junit assertNotNull.Two is using IFNULL in
 * ASM.Extends ASM MethodVisitor to manipulate bytecode that goes into
 * MethodVisitor.
 */
class CheckNullObjectVisitor extends MethodVisitor {

	private final MethodMutatorFactory factory;
	private final MutationContext context;
	public int wordLength = 1;

	CheckNullObjectVisitor(final MethodMutatorFactory factory, final MethodInfo methodInfo,
			final MutationContext context, final MethodVisitor mv) {
		super(Opcodes.ASM6, mv);
		this.factory = factory;
		this.context = context;
	}

	/*
	 * Override visitFieldInsn to check for PUTFIELD or GETFIELD If the bytecode is
	 * PUTFIELD or GETFIELD, perform a nullcheck.
	 */
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		if (opcode == Opcodes.GETFIELD) {
			final MutationIdentifier muID = this.context.registerMutation(factory, "Checked for NULL object here.");

			if (this.context.shouldMutate(muID)) {
				if (wordLength == 1) {
					oneWordVariable(opcode, owner, name, desc);
				}
				if (wordLength == 2) {
					twoWordVariable(opcode, owner, name, desc);
				}
			}

		} else if (opcode == Opcodes.PUTFIELD) {
		} else

		{
			super.visitFieldInsn(opcode, owner, name, desc);
		}
	}

	/**
	 * Deals with the case there there's a two-word variable above the object
	 * reference. If instruction is LDC2_W, use this one. The stack will look like
	 * this: variable (can be 1 or 2-word) -> object reference (1 word)
	 * 
	 */
	public void twoWordVariable(int opcode, String owner, String name, String desc) {
		super.visitInsn(Opcodes.DUP2_X1);
		super.visitInsn(Opcodes.POP2);
		super.visitInsn(Opcodes.DUP);

		// create a label to mark where IFNULL jump to
		Label ifNull = new Label();
		Label ifNotNull = new Label();
		Label returnToNormalCode = new Label();

		// visit ifNull if object is null
		super.visitJumpInsn(Opcodes.IFNULL, ifNull);

		// this happens if object is null
		super.visitLabel(ifNull);
		super.visitInsn(Opcodes.POP);
		super.visitInsn(Opcodes.POP2);
		super.visitJumpInsn(Opcodes.GOTO, returnToNormalCode);

		// this happens if object is not null
		super.visitLabel(ifNotNull);
		super.visitInsn(Opcodes.DUP_X2);
		super.visitInsn(Opcodes.POP);
		super.visitFieldInsn(opcode, owner, name, desc);
		super.visitJumpInsn(Opcodes.GOTO, returnToNormalCode);
		super.visitLabel(returnToNormalCode);
	}

	/**
	 * Deals with the case there there's a one-word variable above the object
	 * reference. If instruction is LDC, use this one. The stack will look like
	 * this: variable (can be 1 or 2-word) -> object reference (1 word)
	 * 
	 * 
	 */
	public void oneWordVariable(int opcode, String owner, String name, String desc) {
		super.visitInsn(Opcodes.DUP_X1);
		super.visitInsn(Opcodes.POP);
		super.visitInsn(Opcodes.DUP);

		// create a label to mark where IFNULL jump to
		Label ifNull = new Label();
		Label ifNotNull = new Label();
		Label returnToNormalCode = new Label();

		// visit ifNull if object is null
		super.visitJumpInsn(Opcodes.IFNULL, ifNull);

		// this happens if object is null
		super.visitLabel(ifNull);
		super.visitInsn(Opcodes.POP);
		super.visitInsn(Opcodes.POP);
		super.visitJumpInsn(Opcodes.GOTO, returnToNormalCode);

		// this happens if object is not null
		super.visitLabel(ifNotNull);
		super.visitInsn(Opcodes.DUP_X2);
		super.visitInsn(Opcodes.POP);
		super.visitFieldInsn(opcode, owner, name, desc);
		super.visitJumpInsn(Opcodes.GOTO, returnToNormalCode);
		super.visitLabel(returnToNormalCode);
	}

	/**
	 * We don't introduce any new variable, just one computation, so the maxStack
	 * should not change. In FrameOptions.java, the ClassWriter already has
	 * COMPUTE_FRAME so maybe this stack size will be automatically calculated.
	 * 
	 * @see org.objectweb.asm.MethodVisitor#visitMaxs(int, int)
	 */
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		super.visitMaxs(maxStack, maxLocals);
	}

	public void setWordLength(int length) {
		this.wordLength = length;
	}

}