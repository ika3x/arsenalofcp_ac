import java.util.*;
import java.util.stream.*;

public class MoreArraysInteger {
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
}