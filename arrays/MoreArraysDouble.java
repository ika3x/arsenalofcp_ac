import java.util.*;
import java.util.stream.*;

public class MoreArraysDouble {
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
}