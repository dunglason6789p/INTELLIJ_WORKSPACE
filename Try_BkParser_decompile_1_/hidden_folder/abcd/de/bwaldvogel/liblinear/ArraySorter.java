/*
 * Decompiled with CFR 0.146.
 */
package de.bwaldvogel.liblinear;

final class ArraySorter {
    ArraySorter() {
    }

    public static void reversedMergesort(double[] a) {
        ArraySorter.reversedMergesort(a, 0, a.length);
    }

    private static void reversedMergesort(double[] x, int off, int len) {
        int a;
        int c;
        if (len < 7) {
            for (int i = off; i < len + off; ++i) {
                for (int j = i; j > off && x[j - 1] < x[j]; --j) {
                    ArraySorter.swap(x, j, j - 1);
                }
            }
            return;
        }
        int m = off + (len >> 1);
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {
                int s = len / 8;
                l = ArraySorter.med3(x, l, l + s, l + 2 * s);
                m = ArraySorter.med3(x, m - s, m, m + s);
                n = ArraySorter.med3(x, n - 2 * s, n - s, n);
            }
            m = ArraySorter.med3(x, l, m, n);
        }
        double v = x[m];
        int b = a = off;
        int d = c = off + len - 1;
        do {
            if (b <= c && x[b] >= v) {
                if (x[b] == v) {
                    ArraySorter.swap(x, a++, b);
                }
                ++b;
                continue;
            }
            while (c >= b && x[c] <= v) {
                if (x[c] == v) {
                    ArraySorter.swap(x, c, d--);
                }
                --c;
            }
            if (b > c) break;
            ArraySorter.swap(x, b++, c--);
        } while (true);
        int n = off + len;
        int s = Math.min(a - off, b - a);
        ArraySorter.vecswap(x, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        ArraySorter.vecswap(x, b, n - s, s);
        s = b - a;
        if (s > 1) {
            ArraySorter.reversedMergesort(x, off, s);
        }
        if ((s = d - c) > 1) {
            ArraySorter.reversedMergesort(x, n - s, s);
        }
    }

    private static void swap(double[] x, int a, int b) {
        double t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    private static void vecswap(double[] x, int a, int b, int n) {
        int i = 0;
        while (i < n) {
            ArraySorter.swap(x, a, b);
            ++i;
            ++a;
            ++b;
        }
    }

    private static int med3(double[] x, int a, int b, int c) {
        return x[a] < x[b] ? (x[b] < x[c] ? b : (x[a] < x[c] ? c : a)) : (x[b] > x[c] ? b : (x[a] > x[c] ? c : a));
    }
}

