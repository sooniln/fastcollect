package io.github.sooniln.fastcollect;

import io.github.sooniln.fastcollect.ints.Int2IntHashMap;

public final class Int2IntMapAsmProbe {
    private static final int N_KEYS = 1 << 14;
    private static final int OPS = 5_000_000;

    public static void main(String[] args) {
        Int2IntHashMap m = new Int2IntHashMap(N_KEYS, 0.75f, Integer.MIN_VALUE);

        // Precompute keys + smeared hashes so the hot loop does not allocate or re-hash.
        int[] keys = new int[N_KEYS];
        for (int i = 0; i < N_KEYS; i++) {
            keys[i] = i;
        }

        // Pre-fill so lookups don't early-out on size == 0 and we hit the probe loop.
        for (int i = 0; i < N_KEYS; i++) {
            m.putValue(keys[i], i);
        }

        // Warmup + measurement-ish loop. We want a very hot call-site so C2 compiles it.
        System.out.println("sum=" + iterateFast(m));
        System.out.println("sum=" + iterate(m));
    }

    public static long iterateFast(Int2IntHashMap m) {
        long sum = 0;
        var it = m.fastIterator();
        while (it.hasNext()) {
            sum += it.next().key();
        }
        return sum;
    }

    public static long iterate(Int2IntHashMap m) {
        long sum = 0;
        var it = m.iterator();
        while (it.hasNext()) {
            sum += it.next().key();
        }
        return sum;
    }
}
