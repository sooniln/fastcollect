package io.github.sooniln.fastcollect;

import io.github.sooniln.fastcollect.ints.Int2IntHashMap;
import java.util.Random;

public final class Int2IntMapAsmProbe {
    private static final int N_KEYS = 1 << 14;

    public static void main(String[] args) {
        Random rnd = new Random();
        Int2IntHashMap m = new Int2IntHashMap(N_KEYS, 0.75f, Integer.MIN_VALUE);
        int[] inElements = new int[N_KEYS];
        int[] outElements = new int[N_KEYS];

        var i = 0;
        while (i < N_KEYS) {
            int r = rnd.nextInt(N_KEYS);
            if (!m.containsKey(r)) {
                m.set(r, r);
                inElements[i++] = r;
            }
        }

        i = 0;
        while (i < N_KEYS) {
            int r = rnd.nextInt(N_KEYS * 10);
            if (!m.containsKey(r)) {
                outElements[i++] = r;
            }
        }

        // Warmup + measurement-ish loop. We want a very hot call-site so C2 compiles it.
        long insum = 0;
        long outsum = 0;
        for (i = 0; i < inElements.length; i++) {
            insum += m.lookup(inElements[i]);
            outsum += m.lookup(outElements[i]);
        }
        System.out.println(insum + outsum);
    }
}
