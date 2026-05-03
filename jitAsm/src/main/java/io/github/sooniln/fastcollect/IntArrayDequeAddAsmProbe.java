package io.github.sooniln.fastcollect;

import io.github.sooniln.fastcollect.ints.IntArrayDeque;
import io.github.sooniln.fastcollect.ints.IntHashSet;
import io.github.sooniln.fastcollect.ints.MutableIntCollection;
import korlibs.datastructure.IntArrayList;
import korlibs.datastructure.IntList;

import java.util.Collection;
import java.util.Random;

/**
 * A harness which runs [IntHashSet.contains] in a tight loop to force C2 compilation and allows for JIT ASM output of
 * the resulting compilations.
 */
public final class IntArrayDequeAddAsmProbe {
    private static final int N_KEYS = 1 << 14;

    public static void main(String[] args) {
        // Warmup + measurement-ish loop. We want a very hot call-site so C2 compiles it.
        System.out.println(add().getSize());
    }

    public static IntArrayDeque add() {
        var fastcollect = new IntArrayDeque(0);
        for (var i = 0; i < N_KEYS; ++i) {
            fastcollect.add(2);
        }
        return fastcollect;
    }
}
