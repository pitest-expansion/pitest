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
package org.pitest.mutationtest.engine.gregor;

import static org.pitest.functional.prelude.Prelude.and;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.pitest.bytecode.FrameOptions;
import org.pitest.bytecode.NullVisitor;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.ComputeClassWriter;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

/**
 * Use ClassByteArraySource byteSource interface to replace methods name and
 * method descriptor in code. ByteSource is an interface and therefore can
 * accept CatchingByteArraySource, ClassloaderByteArraySource,
 * ClassPathByteArraySource, ResourceFolderByteArraySource
 */
public class GregorMutater implements Mutater {

    private final Map<String, String> computeCache = new HashMap<>();
    private final Predicate<MethodInfo> filter;
    private final ClassByteArraySource byteSource;

    private final Set<MethodMutatorFactory> mutators = new HashSet<>();

    public GregorMutater(final ClassByteArraySource byteSource, final Predicate<MethodInfo> filter,
            final Collection<MethodMutatorFactory> mutators) {
        this.filter = filter;
        this.mutators.addAll(mutators);
        this.byteSource = byteSource;
    }

    /**
     * Implements the interface and use Optional object to find the mutation.
     * 
     * @see org.pitest.mutationtest.engine.Mutater#findMutations(org.pitest.classinfo.
     *      ClassName)
     */
    @Override
    public List<MutationDetails> findMutations(final ClassName classToMutate) {

        final ClassContext context = new ClassContext();
        context.setTargetMutation(Optional.<MutationIdentifier>empty());
        Optional<byte[]> bytes = GregorMutater.this.byteSource.getBytes(classToMutate.asInternalName());

        return bytes.map(findMutations(context)).orElse(Collections.<MutationDetails>emptyList());

    }

    /**
     * Function interface, return a list of input and a list of output
     * 
     * @param context
     *            A ClassContext input to be used in the function
     *            findMutationForBytes
     * @return Function<byte[], List<MutationDetails>> A function interface with
     *         input as an array of byte[], output to List<MutationDetails> ??? not
     *         sure about this
     */
    private Function<byte[], List<MutationDetails>> findMutations(final ClassContext context) {
        return bytes -> findMutationsForBytes(context, bytes);
    }

    /**
     * Used as a lambda function for findMutations. Read the stream of byte[] from a
     * class then save the mutations in an ArrayList in ClassContext object. Then
     * return the ArrayList.
     * 
     * @param context
     *            ClassContext to save the mutations.
     * @param classToMutat
     *            An array of byte to scan for possible mutations.
     * @return List<MutationDetails> A list of mutations saved in the "context"
     *         parameter
     */
    private List<MutationDetails> findMutationsForBytes(final ClassContext context, final byte[] classToMutate) {

        final ClassReader first = new ClassReader(classToMutate);
        final NullVisitor nv = new NullVisitor();
        final MutatingClassVisitor mca = new MutatingClassVisitor(nv, context, filterMethods(), this.mutators,
                this.byteSource);

        first.accept(mca, ClassReader.EXPAND_FRAMES);

        return new ArrayList<>(context.getCollectedMutations());
    }

    /**
     * Use the optimization pattern similar to page 21 of ASM user guide.
     */
    @Override
    public Mutant getMutation(final MutationIdentifier id) {
        final ClassContext context = new ClassContext();
        context.setTargetMutation(Optional.ofNullable(id));

        final Optional<byte[]> bytes = this.byteSource.getBytes(id.getClassName().asJavaName());

        final ClassReader reader = new ClassReader(bytes.get());
        final ClassWriter w = new ComputeClassWriter(this.byteSource, this.computeCache,
                FrameOptions.pickFlags(bytes.get()));
        final MutatingClassVisitor mca = new MutatingClassVisitor(w, context, filterMethods(),
                FCollection.filter(this.mutators, isMutatorFor(id)), this.byteSource);
        reader.accept(mca, ClassReader.EXPAND_FRAMES);
        final List<MutationDetails> details = context.getMutationDetails(context.getTargetMutation().get());
        return new Mutant(details.get(0), w.toByteArray());

    }

    /**
     * @return Return the byteSource object. Useful for extracting data about the
     *         current class and potentially manipulate it (methods, overloading
     *         methods/constructors)
     */
    public ClassByteArraySource getByteSource() {
        return this.byteSource;
    }

    private static Predicate<MethodMutatorFactory> isMutatorFor(final MutationIdentifier id) {
        return a -> id.getMutator().equals(a.getGloballyUniqueId());
    }

    private Predicate<MethodInfo> filterMethods() {
        return and(this.filter, filterSyntheticMethods(), isGeneratedEnumMethod().negate(), isGroovyClass().negate());
    }

    private static Predicate<MethodInfo> isGroovyClass() {
        return a -> a.isInGroovyClass();
    }

    private static Predicate<MethodInfo> filterSyntheticMethods() {
        return a -> !a.isSynthetic() || a.getName().startsWith("lambda$");
    }

    private static Predicate<MethodInfo> isGeneratedEnumMethod() {
        return a -> a.isGeneratedEnumMethod();
    }

}
