package io.github.sooniln.fastcollect;

import io.github.sooniln.fastcollect.ints.IntArrayDeque;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Random;

/**
 * A harness which runs [IntHashSet.contains] in a tight loop to force C2 compilation and allows for JIT ASM output of
 * the resulting compilations.
 */
public final class IntArrayDequeIterateAsmProbe {
    private static final int N_KEYS = 1 << 14;

    public static void main(String[] args) {
        Random rnd = new Random();
        IntArrayDeque m = new IntArrayDeque(N_KEYS);
        IntArrayList n = new IntArrayList(N_KEYS);

        var i = 0;
        while (i < N_KEYS) {
            m.add(rnd.nextInt(N_KEYS));
            n.add(rnd.nextInt(N_KEYS));
            ++i;
        }

        System.out.println(iterateFastCollect(m));
        System.out.println(iterateFastutil(n));
    }

    // Warmup + measurement-ish loop. We want a very hot call-site so C2 compiles it.
    public static int iterateFastCollect(IntArrayDeque m) {
        var sum = 0;
        var it = m.iterator();
        while (it.hasNext()) {
            sum += it.nextInt();
        }
        return sum;
    }


    public static int iterateFastutil(IntArrayList n) {
        var sum = 0;
        var it = n.iterator();
        while (it.hasNext()) {
            sum += it.nextInt();
        }
        return sum;
    }
}
