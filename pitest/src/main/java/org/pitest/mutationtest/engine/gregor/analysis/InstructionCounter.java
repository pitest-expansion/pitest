package org.pitest.mutationtest.engine.gregor.analysis;

/**
 * Count the instruction for each visit method.
 *
 * 
 */
public interface InstructionCounter {

    void increment();

    int currentInstructionCount();

}
