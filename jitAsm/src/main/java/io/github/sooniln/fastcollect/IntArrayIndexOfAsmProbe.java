package io.github.sooniln.fastcollect;

import kotlin.collections.ArraysKt;
import java.util.Random;

public final class IntArrayIndexOfAsmProbe {
    private static final int SIZE = 1024;
    private static final int ITERS = 10_000;

    // Thin wrappers give CompileCommand a stable target name. C2 will inline
    // through them, so the printed assembly reflects the full implementation
    // of each function including any allocations or inlined callees.

    private static int fastSearch(int[] array, int element, int size) {
        return ArrayUtils_jvmKt.fastIndexOf(array, element, 0, size);
    }

    private static int stdSearch(int[] array, int element) {
        return ArraysKt.indexOf(array, element);
    }

    public static void main(String[] args) {
        int[] array = new int[SIZE];
        for (int i = 0; i < SIZE; i++) array[i] = i + 2;

        // Fixed-seed Fisher-Yates shuffle for reproducibility.
        Random rnd = new Random(42);
        for (int i = SIZE - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = array[i]; array[i] = array[j]; array[j] = tmp;
        }

        // hitElement lives only at the last index — forces a full traversal.
        array[SIZE - 1] = 0;
        int hitElement = 0;
        int missElement = 1; // not present anywhere

        long sum = 0;
        for (int i = 0; i < ITERS; i++) {
            sum += fastSearch(array, hitElement, SIZE);
            sum += fastSearch(array, missElement, SIZE);
            sum += stdSearch(array, hitElement);
            sum += stdSearch(array, missElement);
        }
        System.out.println(sum);
    }
}
