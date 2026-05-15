package io.github.sooniln.fastcollect;

import io.github.sooniln.fastcollect.ints.IntHashSet;

import java.util.Random;

/**
 * A harness which runs [IntHashSet.contains] in a tight loop to force C2 compilation and allows for JIT ASM output of
 * the resulting compilations.
 */
public final class IntHashSetContainsAsmProbe {
    private static final int N_KEYS = 1 << 14;

    public static void main(String[] args) {
        Random rnd = new Random();
        IntHashSet m = new IntHashSet(N_KEYS);
        org.eclipse.collections.impl.set.mutable.primitive.IntHashSet n = new org.eclipse.collections.impl.set.mutable.primitive.IntHashSet(N_KEYS);
        int[] inElements = new int[N_KEYS];
        int[] outElements = new int[N_KEYS];

        var i = 0;
        while (i < N_KEYS) {
            int r = rnd.nextInt(N_KEYS);
            if (!m.contains(r)) {
                m.add(r);
                n.add(r);
                inElements[i++] = r;
            }
        }

        i = 0;
        while (i < N_KEYS) {
            int r = rnd.nextInt(N_KEYS * 10);
            if (!m.contains(r)) {
                outElements[i++] = r;
            }
        }

        // Warmup + measurement-ish loop. We want a very hot call-site so C2 compiles it.
        long insum = 0;
        long outsum = 0;
        for (i = 0; i < outElements.length; i++) {
            if (m.contains(outElements[i])) ++insum;
            if (n.contains(outElements[i])) ++outsum;
        }
        System.out.println(insum + outsum);
    }
}
