import java.util.Arrays;
import java.util.function.LongBinaryOperator;
import java.util.stream.LongStream;

/**
 * Li Chao Tree.
 * Supports both min and max queries by passing an appropriate comparator/operator.
 * Coordinate compression is done via the xs[] array passed to the constructor.
 * If you don't want compression, pass LongStream.range(l, r).toArray() as xs.
 *
 * Line representation: f(x) = a * x + b
 */
public class LiChaoTree {
    // xs[i] is the actual x-coordinate corresponding to compressed index i
    private final long[] xs;
    private final int n;

    // a[id], b[id] : line stored at node id (f(x) = a*x + b)
    private final long[] a;
    private final long[] b;
    private final boolean[] used;

    // isMin = true -> min query (keep line with smaller value)
    // isMin = false -> max query (keep line with larger value)
    private final boolean isMin;

    // sentinel value used for "no line" comparisons
    private final long INF;

    public LiChaoTree(long[] xs, boolean isMin) {
        this.xs = xs.clone();
        this.n = xs.length;
        this.isMin = isMin;
        this.INF = isMin ? Long.MAX_VALUE : Long.MIN_VALUE;

        int size = Math.max(1, n);
        this.a = new long[4 * size];
        this.b = new long[4 * size];
        this.used = new boolean[4 * size];
    }

    // value of line (a, b) at compressed index idx (uses xs[idx] as real x)
    private long eval(long a, long b, int idx) {
        return a * xs[idx] + b;
    }

    // returns true if val1 is "better" than val2 given isMin/isMax mode
    private boolean better(long val1, long val2) {
        return isMin ? (val1 < val2) : (val1 > val2);
    }

    /**
     * Add a line f(x) = a*x + b to the segment [l, r) in compressed index space.
     */
    public void addLine(long a, long b) {
        if (n == 0) return;
        addLine(a, b, 1, 0, n - 1);
    }

    private void addLine(long a, long b, int node, int l, int r) {
        int mid = (l + r) / 2;

        if (!used[node]) {
            // no line at this node yet, just place it
            this.a[node] = a;
            this.b[node] = b;
            used[node] = true;
            return;
        }

        long curLeft = eval(this.a[node], this.b[node], l);
        long newLeft = eval(a, b, l);
        long curMid = eval(this.a[node], this.b[node], mid);
        long newMid = eval(a, b, mid);

        // if new line is better at mid, swap so 'node' always holds the line
        // that wins at mid, and (a, b) becomes the "losing" candidate to push down
        boolean leftBetter = better(newLeft, curLeft);
        boolean midBetter = better(newMid, curMid);

        if (midBetter) {
            // swap: node's line becomes the new one, push old down
            long ta = this.a[node], tb = this.b[node];
            this.a[node] = a;
            this.b[node] = b;
            a = ta;
            b = tb;
            leftBetter = !leftBetter; // recompute relative winner at l after swap
            // recompute leftBetter properly instead of flipping blindly
            leftBetter = better(eval(a, b, l), eval(this.a[node], this.b[node], l));
        }

        if (l == r) return;

        if (leftBetter) {
            // losing line (a, b) is better on the left half -> recurse left
            addLine(a, b, node * 2, l, mid);
        } else {
            // losing line (a, b) is better on the right half -> recurse right
            addLine(a, b, node * 2 + 1, mid + 1, r);
        }
    }

    /**
     * Query the best value among all added lines at compressed index idx.
     */
    public long get(int idx) {
        if (n == 0) return INF;
        return get(idx, 1, 0, n - 1);
    }
    
    private long get(int idx, int node, int l, int r) {
        if (node >= a.length) return INF;

        long res = used[node] ? eval(a[node], b[node], idx) : INF;

        if (l == r) return res;

        int mid = (l + r) / 2;
        long childRes;
        if (idx <= mid) {
            childRes = get(idx, node * 2, l, mid);
        } else {
            childRes = get(idx, node * 2 + 1, mid + 1, r);
        }

        return better(childRes, res) ? childRes : res;
    }

    /**
     * Query by actual x-coordinate value (binary search into compressed index).
     * xs must be sorted ascending for this to work.
     */
    public long getByValue(long x) {
        int idx = java.util.Arrays.binarySearch(xs, x);
        if (idx < 0) {
            throw new IllegalArgumentException("value not in coordinate array: " + x);
        }
        return get(idx);
    }

    // helper: build an uncompressed xs array [l, r)
    public static long[] identityXs(long l, long r) {
        return LongStream.range(l, r).toArray();
    }
}