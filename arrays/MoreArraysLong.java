import java.util.*;
import java.util.stream.*;

public class MoreArraysLong {
    // tip: long is longer than int

    public static LongStream stream(long[] a) { return Arrays.stream(a); }
    public static void fill(long[] a, long x) { Arrays.fill(a, x); }
    
    // 1-indexed
    public static long[] prefixSum(long[] a) {
        long[] p = new long[a.length+1];
        for (int i = 0; i < a.length; i++) {
            p[i+1] = p[i] + a[i];
        }
        return p;
    }

    public static void reverse(long[] a) {
        for (int i = 0; i < a.length/2; i++) {
            long tmp = a[i];
            a[i] = a[a.length-1-i];
            a[a.length-1-i] = tmp; 
        }
    }

    public static long[] reversedArray(long[] a) {
        long[] r = new long[a.length];
        for (int i = 0; i < a.length; i++) {
            r[a.length - i - 1] = a[i];
        }
        return r;
    }

    public static long[] slice(long[] a, int fromIndex, int toIndex) {
        long[] res = new long[toIndex - fromIndex];
        System.arraycopy(a, fromIndex, res, 0, toIndex - fromIndex);
        return res;
    }

    public static long pyget(long[] a, int i) {
        if (i < 0) { i = a.length + i; }
        return a[i];
    }

    public static long[] pyslice(long[] a, int fromIndex, int toIndex) {
        if (fromIndex < 0) { fromIndex = a.length + fromIndex; }
        if (toIndex < 0) { toIndex = a.length + toIndex; }

        long[] res = new long[toIndex - fromIndex];
        System.arraycopy(a, fromIndex, res, 0, toIndex - fromIndex);
        return res;
    }

    // tip: this comment was written in english

    public static HashMap<Long, Integer> countToHashMap(long[] a) {
        HashMap<Long, Integer> chm = new HashMap<>();
        for (long v : a) { chm.merge(v, 1, Integer::sum); }
        return chm;
    }

    public static void sort(long[] a) { Arrays.sort(a); }
    public static void sort(long[] a, int fromIndex, int toIndex) { Arrays.sort(a, fromIndex, toIndex); }

    public static void sort(long[] a, Comparator<? super Long> comp) {
        if (comp.equals(Comparator.naturalOrder())) { sort(a); return; }
        else if (comp.equals(Comparator.reverseOrder())) { sort(a); reverse(a); return; }
        long[] b = stream(a).boxed().sorted(comp).mapToLong(Long::longValue).toArray();
        System.arraycopy(b, 0, a, 0, a.length);
    }

    public static Compressed_i64<HashMap<Long, Integer>> compress(long[] a) {
        return compress(a, new HashMap<Long, Integer>());
    }

    // map should be empty
    public static <M extends Map<Long, Integer>> Compressed_i64<M> compress(long[] a, M compress) {
        if (a.length == 0) {
            return new Compressed_i64<>(compress, new int[0], new long[0]);
        }
        
        long[] sorted = a.clone();
        sort(sorted);
        long last = sorted[0];

        compress.put(last, 0);
        int idx = 1;
        for (long val : sorted) {
            if (val != last) {
                compress.put(val, idx++);
                last = val;
            }
        }

        long[] v = new long[compress.size()];
        for (var en : compress.entrySet()) {
            v[en.getValue()] = en.getKey();
        }
        int[] r = Arrays.stream(a).mapToInt(compress::get).toArray();
        
        return new Compressed_i64<>(compress, r, v);
    }
    public record Compressed_i64<M extends Map<Long, Integer>>(
        M valueToIndex, int[] compressed, long[] values
    ) {}

    // tip: this is made by a coder who made this.

    public static int normalBinarySearch(long[] a, long x) {
        return Arrays.binarySearch(a, x);
    }

    public static boolean containsSorted(long[] a, long x) {
        return normalBinarySearch(a, x) >= 0;
    }

    public static int lowerBound(long[] a, long x) {
        int lf = 0, rg = a.length-1;
        int ok = -1;
        while (lf <= rg) {
            int md = lf + ((rg - lf) >> 1);
            if (a[md] < x) {
                ok = md;
                lf = md + 1;
            } else {
                rg = md - 1;
            }
        }
        return ok + 1;
    }

    public static int upperBound(long[] a, long x) {
        int lf = 0, rg = a.length-1;
        int ok = -1;
        while (lf <= rg) {
            int md = lf + ((rg - lf) >> 1);
            if (a[md] <= x) {
                ok = md;
                lf = md + 1;
            } else {
                rg = md - 1;
            }
        }
        return ok + 1;
    }

    public static int countSorted(long[] a, long x) {
        return upperBound(a, x) - lowerBound(a, x);
    }

    // tip: '\n' means the letter \n.

    public static long[] deepClone(long[] a) { return a.clone(); }
    public static long[][] deepClone(long[][] a) {
        var b = new long[a.length][];
        for (int i = 0; i < a.length; i++) { b[i] = deepClone(a[i]); }
        return b;
    }
    public static long[][][] deepClone(long[][][] a) { var b = new long[a.length][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static long[][][][] deepClone(long[][][][] a) { var b = new long[a.length][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static long[][][][][] deepClone(long[][][][][] a) { var b = new long[a.length][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static long[][][][][][] deepClone(long[][][][][][] a) { var b = new long[a.length][][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static long[][][][][][][] deepClone(long[][][][][][][] a) { var b = new long[a.length][][][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static long[][][][][][][][] deepClone(long[][][][][][][][] a) { var b = new long[a.length][][][][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static long[][][][][][][][][] deepClone(long[][][][][][][][][] a) { var b = new long[a.length][][][][][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static long[][][][][][][][][][] deepClone(long[][][][][][][][][][] a) { var b = new long[a.length][][][][][][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    
    public static void deepFill(long[] a, long x) { Arrays.fill(a, x); }
    public static void deepFill(long[][] a, long x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(long[][][] a, long x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(long[][][][] a, long x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(long[][][][][] a, long x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(long[][][][][][] a, long x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(long[][][][][][][] a, long x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(long[][][][][][][][] a, long x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(long[][][][][][][][][] a, long x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(long[][][][][][][][][][] a, long x) { for (var b : a) deepFill(b, x);}

    // ClockWise, Right?
    public static long[][] rotate90cw(long[][] a) {
        int H = a.length, W = a[0].length;
        long[][] b = new long[W][H];
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                b[j][H-1-i] = a[i][j];
            }
        }

        return b;
    }

    // CounterClockWise, Left?
    public static long[][] rotate90ccw(long[][] a) {
        int H = a.length, W = a[0].length;
        long[][] b = new long[W][H];
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                b[W-1-j][i] = a[i][j];
            }
        }

        return b;
    }

    // 3.14159265358979Wise
    public static long[][] rotate180(long[][] a) {
        int H = a.length, W = a[0].length;
        long[][] b = new long[H][W];
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                b[H-1-i][W-1-j] = a[i][j];
            }
        }

        return b;
    }
}