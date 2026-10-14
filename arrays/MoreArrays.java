import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class MoreArrays {
    // Welcome to MoreArrays Library.
    // You can use me to do something i don't know. anyway, good luck.

    /* double territory -> */

    // tip: you must code in java if you want to use this lib, btw.
    
    public static DoubleStream stream(double[] a) { return Arrays.stream(a); }
    public static void fill(double[] a, double x) { Arrays.fill(a, x); }
    
    // 1-indexed
    public static double[] prefixSum(double[] a) {
        double[] p = new double[a.length+1];
        for (int i = 0; i < a.length; i++) {
            p[i+1] = p[i] + a[i];
        }
        return p;
    }

    public static void reverse(double[] a) {
        for (int i = 0; i < a.length/2; i++) {
            double tmp = a[i];
            a[i] = a[a.length-1-i];
            a[a.length-1-i] = tmp; 
        }
    }

    public static double[] reversedArray(double[] a) {
        double[] r = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            r[a.length - i - 1] = a[i];
        }
        return r;
    }

    // tip: in competitive programming, you must compete.

    public static double[] slice(double[] a, int fromIndex, int toIndex) {
        double[] res = new double[toIndex - fromIndex];
        System.arraycopy(a, fromIndex, res, 0, toIndex - fromIndex);
        return res;
    }

    public static double pyget(double[] a, int i) {
        if (i < 0) { i = a.length + i; }
        return a[i];
    }

    public static double[] pyslice(double[] a, int fromIndex, int toIndex) {
        if (fromIndex < 0) { fromIndex = a.length + fromIndex; }
        if (toIndex < 0) { toIndex = a.length + toIndex; }

        double[] res = new double[toIndex - fromIndex];
        System.arraycopy(a, fromIndex, res, 0, toIndex - fromIndex);
        return res;
    }

    public static HashMap<Double, Integer> countToHashMap(double[] a) {
        HashMap<Double, Integer> chm = new HashMap<>();
        for (double v : a) { chm.merge(v, 1, Integer::sum); }
        return chm;
    }

    public static void sort(double[] a) { Arrays.sort(a); }
    public static void sort(double[] a, int fromIndex, int toIndex) { Arrays.sort(a, fromIndex, toIndex); }

    public static void sort(double[] a, Comparator<? super Double> comp) {
        if (comp.equals(Comparator.naturalOrder())) { sort(a); return; }
        else if (comp.equals(Comparator.reverseOrder())) { sort(a); reverse(a); return; }
        double[] b = stream(a).boxed().sorted(comp).mapToDouble(Double::doubleValue).toArray();
        System.arraycopy(b, 0, a, 0, a.length);
    }

    public static Compressed_f64<HashMap<Double, Integer>> compress(double[] a) {
        return compress(a, new HashMap<Double, Integer>());
    }

    // map should be empty
    public static <M extends Map<Double, Integer>> Compressed_f64<M> compress(double[] a, M compress) {
        if (a.length == 0) {
            return new Compressed_f64<>(compress, new int[0], new double[0]);
        }
        
        double[] sorted = a.clone();
        sort(sorted);
        double last = sorted[0];

        compress.put(last, 0);
        int idx = 1;
        for (double val : sorted) {
            if (val != last) {
                compress.put(val, idx++);
                last = val;
            }
        }

        double[] v = new double[compress.size()];
        for (var en : compress.entrySet()) {
            v[en.getValue()] = en.getKey();
        }
        int[] r = Arrays.stream(a).mapToInt(compress::get).toArray();
        
        return new Compressed_f64<>(compress, r, v);
    }
    public record Compressed_f64<M extends Map<Double, Integer>>(
        M valueToIndex, int[] compressed, double[] values
    ) {}

    public static int normalBinarySearch(double[] a, double x) {
        return Arrays.binarySearch(a, x);
    }

    public static boolean containsSorted(double[] a, double x) {
        return normalBinarySearch(a, x) >= 0;
    }

    public static int lowerBound(double[] a, double x) {
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

    public static int upperBound(double[] a, double x) {
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

    public static int countSorted(double[] a, double x) {
        return upperBound(a, x) - lowerBound(a, x);
    }

    // tip: you should submit code when you solve a problem

    public static double[] deepClone(double[] a) { return a.clone(); }
    public static double[][] deepClone(double[][] a) {
        var b = new double[a.length][];
        for (int i = 0; i < a.length; i++) { b[i] = deepClone(a[i]); }
        return b;
    }
    public static double[][][] deepClone(double[][][] a) { var b = new double[a.length][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static double[][][][] deepClone(double[][][][] a) { var b = new double[a.length][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static double[][][][][] deepClone(double[][][][][] a) { var b = new double[a.length][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static double[][][][][][] deepClone(double[][][][][][] a) { var b = new double[a.length][][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static double[][][][][][][] deepClone(double[][][][][][][] a) { var b = new double[a.length][][][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static double[][][][][][][][] deepClone(double[][][][][][][][] a) { var b = new double[a.length][][][][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static double[][][][][][][][][] deepClone(double[][][][][][][][][] a) { var b = new double[a.length][][][][][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static double[][][][][][][][][][] deepClone(double[][][][][][][][][][] a) { var b = new double[a.length][][][][][][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }

    public static void deepFill(double[] a, double x) { Arrays.fill(a, x); }
    public static void deepFill(double[][] a, double x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(double[][][] a, double x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(double[][][][] a, double x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(double[][][][][] a, double x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(double[][][][][][] a, double x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(double[][][][][][][] a, double x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(double[][][][][][][][] a, double x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(double[][][][][][][][][] a, double x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(double[][][][][][][][][][] a, double x) { for (var b : a) deepFill(b, x);}

    // ClockWise, Right?
    public static double[][] rotate90cw(double[][] a) {
        int H = a.length, W = a[0].length;
        double[][] b = new double[W][H];
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                b[j][H-1-i] = a[i][j];
            }
        }

        return b;
    }

    // CounterClockWise, Left?
    public static double[][] rotate90ccw(double[][] a) {
        int H = a.length, W = a[0].length;
        double[][] b = new double[W][H];
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                b[W-1-j][i] = a[i][j];
            }
        }

        return b;
    }

    // 3.14159265358979Wise
    public static double[][] rotate180(double[][] a) {
        int H = a.length, W = a[0].length;
        double[][] b = new double[H][W];
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                b[H-1-i][W-1-j] = a[i][j];
            }
        }

        return b;
    }

    // tip: editors are good for editing.

    /* <- double territory | int territory -> */

    public static IntStream stream(int[] a) { return Arrays.stream(a); }
    public static void fill(int[] a, int x) { Arrays.fill(a, x); }
    
    // 1-indexed
    public static int[] prefixSum(int[] a) {
        int[] p = new int[a.length+1];
        for (int i = 0; i < a.length; i++) {
            p[i+1] = p[i] + a[i];
        }
        return p;
    }

    public static void reverse(int[] a) {
        for (int i = 0; i < a.length/2; i++) {
            int tmp = a[i];
            a[i] = a[a.length-1-i];
            a[a.length-1-i] = tmp; 
        }
    }

    public static int[] reversedArray(int[] a) {
        int[] r = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            r[a.length - i - 1] = a[i];
        }
        return r;
    }

    public static int[] slice(int[] a, int fromIndex, int toIndex) {
        int[] res = new int[toIndex - fromIndex];
        System.arraycopy(a, fromIndex, res, 0, toIndex - fromIndex);
        return res;
    }

    // Index exception? That's not my fault, is it?
    public static int pyget(int[] a, int i) {
        if (i < 0) { i = a.length + i; }
        return a[i];
    }

    // Negative array length? That's your fault, isn't it?
    public static int[] pyslice(int[] a, int fromIndex, int toIndex) {
        if (fromIndex < 0) { fromIndex = a.length + fromIndex; }
        if (toIndex < 0) { toIndex = a.length + toIndex; }

        int[] res = new int[toIndex - fromIndex];
        System.arraycopy(a, fromIndex, res, 0, toIndex - fromIndex);
        return res;
    }

    // people who want to abstract(e.g. for TreeMap) this method must use brainf*ck to study what is coding, ++++...
    public static HashMap<Integer, Integer> countToHashMap(int[] a) {
        HashMap<Integer, Integer> chm = new HashMap<>();
        for (int v : a) { chm.merge(v, 1, Integer::sum); }
        return chm;
    }

    // non-negative only
    public static int[] countToArray(int[] a) {
        int max = stream(a).max().orElse(0);
        if (max >= 260_000_000) throw new OutOfMemoryError("[MLE] Over 1024 MiB.");
        else if (max >= 50_000_000) System.err.println("MLE Warning: allocating about " + (max >> 18) + " MiB.");
        int[] arr = new int[max + 1];
        for (int v : a) arr[v]++;
        return arr;
    }

    // non-negative only, cnt.length > max(a)
    public static int[] countToArrayInPlace(int[] a, int[] cnt) {
        for (int v : a) cnt[v]++;
        return cnt;
    }

    public static int[] iota0(int n) {
        return IntStream.range(0, n).toArray();
    }

    public static int[] iota1(int n) {
        return IntStream.rangeClosed(1, n).toArray();
    }

    // minimuM EXcluded value
    public static int mex(int[] a) {
        boolean[] flag = new boolean[a.length];
        for (int v : a) {
            if (0 <= v && v < a.length) {
                flag[v] = true;
            } else if (v < 0) {
                System.err.println("JVM said that: Mex but negative integer " + v + "? hmm...");
            }
        }
        for (int i = 0; i < flag.length; i++) {
            if (!flag[i]) return i;
        }

        return a.length;
    }

    public static void sort(int[] a) { Arrays.sort(a); }
    public static void sort(int[] a, int fromIndex, int toIndex) { Arrays.sort(a, fromIndex, toIndex); }

    public static void sort(int[] a, Comparator<? super Integer> comp) {
        if (comp.equals(Comparator.naturalOrder())) { sort(a); return; }
        else if (comp.equals(Comparator.reverseOrder())) { sort(a); reverse(a); return; }
        int[] b = stream(a).boxed().sorted(comp).mapToInt(Integer::intValue).toArray();
        System.arraycopy(b, 0, a, 0, a.length);
    }

    public static Compressed_i32<HashMap<Integer, Integer>> compress(int[] a) {
        return compress(a, new HashMap<Integer, Integer>());
    }

    // map should be empty
    public static <M extends Map<Integer, Integer>> Compressed_i32<M> compress(int[] a, M compress) {
        if (a.length == 0) {
            return new Compressed_i32<>(compress, new int[0], new int[0]);
        }
        
        int[] sorted = a.clone();
        sort(sorted);
        int last = sorted[0];

        compress.put(last, 0);
        int idx = 1;
        for (int val : sorted) {
            if (val != last) {
                compress.put(val, idx++);
                last = val;
            }
        }

        int[] v = new int[compress.size()];
        for (var en : compress.entrySet()) {
            v[en.getValue()] = en.getKey();
        }
        int[] r = Arrays.stream(a).map(compress::get).toArray();
        
        return new Compressed_i32<>(compress, r, v);
    }
    public record Compressed_i32<M extends Map<Integer, Integer>>(
        M valueToIndex, int[] compressed, int[] values
    ) {}

    // very useful ---
    public static int normalBinarySearch(int[] a, int x) {
        return Arrays.binarySearch(a, x);
    }
    // --- for contiansSorted.

    public static boolean containsSorted(int[] a, int x) {
        return normalBinarySearch(a, x) >= 0;
    }

    // Your choice of Java is the biggest mistake of your life.
    // Because java hasn't java.util.Arrays.lowerBound yet.
    // I think lowerBound is much easier than Treeify, isn't it?
    public static int lowerBound(int[] a, int x) {
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

    public static int upperBound(int[] a, int x) {
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

    // tip: do you know about 998244353? it's a kind of integer.

    public static int countSorted(int[] a, int x) {
        return upperBound(a, x) - lowerBound(a, x);
    }

    // very easy to understand, ya.
    public static int[] deepClone(int[] a) { return a.clone(); }
    public static int[][] deepClone(int[][] a) {
        var b = new int[a.length][];
        for (int i = 0; i < a.length; i++) { b[i] = deepClone(a[i]); }
        return b;
    }
    public static int[][][] deepClone(int[][][] a) { var b = new int[a.length][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static int[][][][] deepClone(int[][][][] a) { var b = new int[a.length][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static int[][][][][] deepClone(int[][][][][] a) { var b = new int[a.length][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static int[][][][][][] deepClone(int[][][][][][] a) { var b = new int[a.length][][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static int[][][][][][][] deepClone(int[][][][][][][] a) { var b = new int[a.length][][][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static int[][][][][][][][] deepClone(int[][][][][][][][] a) { var b = new int[a.length][][][][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static int[][][][][][][][][] deepClone(int[][][][][][][][][] a) { var b = new int[a.length][][][][][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }
    public static int[][][][][][][][][][] deepClone(int[][][][][][][][][][] a) { var b = new int[a.length][][][][][][][][][]; for (int i = 0; i < a.length; i++) b[i] = deepClone(a[i]); return b; }

    public static void deepFill(int[] a, int x) { Arrays.fill(a, x); }
    public static void deepFill(int[][] a, int x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(int[][][] a, int x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(int[][][][] a, int x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(int[][][][][] a, int x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(int[][][][][][] a, int x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(int[][][][][][][] a, int x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(int[][][][][][][][] a, int x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(int[][][][][][][][][] a, int x) { for (var b : a) deepFill(b, x);}
    public static void deepFill(int[][][][][][][][][][] a, int x) { for (var b : a) deepFill(b, x);}

    // ClockWise, Right?
    public static int[][] rotate90cw(int[][] a) {
        int H = a.length, W = a[0].length;
        int[][] b = new int[W][H];
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                b[j][H-1-i] = a[i][j];
            }
        }

        return b;
    }

    // CounterClockWise, Left?
    public static int[][] rotate90ccw(int[][] a) {
        int H = a.length, W = a[0].length;
        int[][] b = new int[W][H];
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                b[W-1-j][i] = a[i][j];
            }
        }

        return b;
    }

    // 3.14159265358979Wise
    public static int[][] rotate180(int[][] a) {
        int H = a.length, W = a[0].length;
        int[][] b = new int[H][W];
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j++) {
                b[H-1-i][W-1-j] = a[i][j];
            }
        }

        return b;
    }

    /* <- int territory | long territory -> */

    // tip: long is longer than int

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

    /* <- long territory */

    /* The final Code -> */ private MoreArrays() {} /* <- The Final Code */

    // tip: MoreArrays() is private, but MoreArrays is public library.
    // tip: here's the end of the library
    // tip: type jshell on your terminal
    // tip: try atcoder if you are interested in competitive programming.
    // tip: you can use keyboard to type something
    // tip: i use i32/i64/f64 bcuz LongSegTree looks like LazySegTree.
    //
    // final tip: final in java means final in english

    // thanks for reading me. i am glad if you enjoyed! anyway, good luck.
}
