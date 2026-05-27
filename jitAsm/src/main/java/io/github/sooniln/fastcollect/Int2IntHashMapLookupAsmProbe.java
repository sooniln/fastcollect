package io.github.sooniln.fastcollect;

import io.github.sooniln.fastcollect.ints.Int2IntHashMap;

import java.util.Random;

/**
 * A harness which runs [IntHashSet.contains] in a tight loop to force C2 compilation and allows for JIT ASM output of
 * the resulting compilations.
 */
public final class Int2IntHashMapLookupAsmProbe {
    private static final int N_KEYS = 1 << 14;

    public static void main(String[] args) {
        Random rnd = new Random();
        Int2IntHashMap m = new Int2IntHashMap(N_KEYS);
        int[] inElements = new int[N_KEYS];
        int[] outElements = new int[N_KEYS];

        var i = 0;
        while (i < N_KEYS) {
            int r = rnd.nextInt();
            if (!m.containsKey(r)) {
                m.set(r, r);
                inElements[i++] = r;
            }
        }

        i = 0;
        while (i < N_KEYS) {
            int r = rnd.nextInt();
            if (!m.containsKey(r)) {
                outElements[i++] = r;
            }
        }
/*
        i = 0;
        while (i < N_KEYS) {
            int r = rnd.nextInt(N_KEYS * 10);
            if (!m.contains(r)) {
                outElements[i++] = r;
            }
        }*/

        // Warmup + measurement-ish loop. We want a very hot call-site so C2 compiles it.
        long insum = 0;
        for (i = 0; i < outElements.length; i++) {
            insum += m.get(outElements[i]);
        }
        System.out.println(insum);
    }
}
