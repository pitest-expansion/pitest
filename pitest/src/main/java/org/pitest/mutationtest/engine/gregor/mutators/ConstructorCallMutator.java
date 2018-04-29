/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.pitest.mutationtest.engine.gregor.mutators;

import java.util.function.BiFunction;

import org.objectweb.asm.MethodVisitor;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Replaces constructor calls with null. Uses MethodInfo to check for constructor and MethodCallMethodVisitor to return
 * a null value (which applies to normal method as well)
 *
 * 
 */
public enum ConstructorCallMutator implements MethodMutatorFactory {

    CONSTRUCTOR_CALL_MUTATOR;

    @Override
    public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo,
            final MethodVisitor methodVisitor, ClassByteArraySource byteSource) {
        return new MethodCallMethodVisitor(methodInfo, context, methodVisitor, this, constructors(), byteSource);
    }

    @Override
    public String getGloballyUniqueId() {
        return this.getClass().getName();
    }

    /**
     * Check if this is a constructor and return a BiFunction object
     */
    private static BiFunction<String, String, Boolean> constructors() {
        return (name, desc) -> MethodInfo.isConstructor(name);
    }

    @Override
    public String getName() {
        return name();
    }

}
