package org.pitest.mutationtest.engine.gregor.mutators.augmentation;

public class CheckForNullObject {

    public CheckForNullObject() {
        /* TODO M1 mutation, check for null before dereferencing.
         * 
         * field dereferencing are called using GETFIELD in java bytecode
         * Invoking object method are called using INVOKEVIRTUAL, INVOKESPECIAL, INVOKEINTERFACE, INVOKESTATIC.
         *  
         */
    }

}
