/**
 * PersistentSegTreei64 - all primitive!!
 * ---- Methods ----
 *   build / new            : O(N)
 *   set                    : O(log N)
 *   get / prod / allProd   : O(log N)
 *
 *   RAM                    : O(N + Q log N)
 */
class PersistentSegTreei64 {
    private int nodeCount;
    private final int[] left, right;
    private final long[] val;

    private int versionCount;
    private final int[] roots;

    private final int MAX;
    private final int N;
    private final java.util.function.LongBinaryOperator op;
    private final long E;

    private static final int NULL = 0;

    // set(): ~ maxVersions times
    @SuppressWarnings("unchecked")
    public PersistentSegTreei64(int n,
                             java.util.function.LongBinaryOperator op,
                             long e,
                             int maxVersions) {
        this.MAX = n;
        if (n == 1) n = 2;
        
        int k = 1;
        while (k < n) k <<= 1;
        this.N = k;
        this.op = op;
        this.E = e;

        int logN = 32 - Integer.numberOfLeadingZeros(N);
        int maxNodes = (N << 1) + (maxVersions + 1) * (logN + 2) + 8;

        this.left  = new int[maxNodes];
        this.right = new int[maxNodes];
        this.val   = new long[maxNodes];
        this.roots = new int[maxVersions + 2];

        nodeCount = 1;
        val[NULL] = E;

        roots[0] = buildEmpty(0, N);
        versionCount = 1;
    }

    @SuppressWarnings("unchecked")
    public PersistentSegTreei64(long[] dat,
                             java.util.function.LongBinaryOperator op,
                             long e,
                             int maxVersions) {
        this.MAX = dat.length;
        if (dat.length == 1) { dat = Arrays.copyOf(dat, 2); dat[1] = e; }

        int k = 1;
        while (k < dat.length) k <<= 1;
        this.N = k;
        this.op = op;
        this.E = e;

        int logN = 32 - Integer.numberOfLeadingZeros(N);
        int maxNodes = (N << 1) + (maxVersions + 1) * (logN + 2) + 8;

        this.left  = new int[maxNodes];
        this.right = new int[maxNodes];
        this.val   = new long[maxNodes];
        this.roots = new int[maxVersions + 2];

        nodeCount = 1;
        val[NULL] = E;

        roots[0] = build(dat, 0, N);
        versionCount = 1;
    }

    // -------- Version APIs --------

    public int getVersionCount() { return versionCount; }
    public int latestVersion() { return versionCount - 1; }

    // -------- Main APIs --------

    public int set(int version, int p, long x) {
        checkVersion(version);
        exclusiveRangeCheck(p);
        roots[versionCount] = set(roots[version], p, x, 0, N);
        return versionCount++;
    }

    public long get(int version, int p) {
        checkVersion(version);
        exclusiveRangeCheck(p);
        return get(roots[version], p, 0, N);
    }

    public long prod(int version, int l, int r) {
        if (l > r) throw new IllegalArgumentException(
            String.format("Invalid range: [%d, %d)", l, r));
        inclusiveRangeCheck(l);
        inclusiveRangeCheck(r);
        checkVersion(version);
        return prod(roots[version], l, r, 0, N);
    }

    public long allProd(int version) {
        checkVersion(version);
        return val[roots[version]];
    }

    // -------- Boundary Search APIs --------

    public int maxRight(int version,
                        int l,
                        java.util.function.LongPredicate f) {
        checkVersion(version);
        inclusiveRangeCheck(l);

        if (!f.test(E)) {
            throw new IllegalArgumentException(
                "Identity element must satisfy the condition.");
        }
        if (l == MAX) return MAX;

        @SuppressWarnings("unchecked")
        long[] sum = new long[1];
        sum[0] = E;

        int result = maxRight(
            roots[version],
            l,
            0,
            N,
            sum,
            f
        );

        return Math.min(result, MAX);
    }

    public int minLeft(int version,
                       int r,
                       java.util.function.LongPredicate f) {
        checkVersion(version);
        inclusiveRangeCheck(r);

        if (!f.test(E)) {
            throw new IllegalArgumentException(
                "Identity element must satisfy the condition.");
        }
        if (r == 0) return 0;

        @SuppressWarnings("unchecked")
        long[] sum = new long[1];
        sum[0] = E;

        int result = minLeft(
            roots[version],
            r,
            0,
            N,
            sum,
            f
        );

        return Math.max(0, Math.min(result, MAX));
    }

    // -------- Internal: Boundary Search --------

    private int maxRight(int node,
                         int l,
                         int nodeL,
                         int nodeR,
                         long[] sum,
                         java.util.function.LongPredicate f) {
        if (nodeR <= l) {
            return nodeR;
        }

        if (l <= nodeL) {
            long next = op.applyAsLong(sum[0], val[node]);

            if (f.test(next)) {
                sum[0] = next;
                return nodeR;
            }

            return maxRightFail(
                node,
                nodeL,
                nodeR,
                sum,
                f
            );
        }

        int mid = (nodeL + nodeR) >> 1;

        int result = maxRight(
            left[node],
            l,
            nodeL,
            mid,
            sum,
            f
        );

        if (result != mid) {
            return result;
        }

        return maxRight(
            right[node],
            l,
            mid,
            nodeR,
            sum,
            f
        );
    }

    private int maxRightFail(int node,
                             int nodeL,
                             int nodeR,
                             long[] sum,
                             java.util.function.LongPredicate f) {
        if (nodeR - nodeL <= 1) {
            return nodeL;
        }

        int mid = (nodeL + nodeR) >> 1;
        int lc = left[node];
        int rc = right[node];

        long next = op.applyAsLong(sum[0], val[lc]);

        if (!f.test(next)) {
            return maxRightFail(
                lc,
                nodeL,
                mid,
                sum,
                f
            );
        }

        sum[0] = next;

        next = op.applyAsLong(sum[0], val[rc]);

        if (!f.test(next)) {
            return maxRightFail(
                rc,
                mid,
                nodeR,
                sum,
                f
            );
        }

        sum[0] = next;
        return nodeR;
    }

    private int minLeft(int node,
                        int r,
                        int nodeL,
                        int nodeR,
                        long[] sum,
                        java.util.function.LongPredicate f) {
        if (r <= nodeL) {
            return nodeL;
        }

        if (nodeR <= r) {
            long next = op.applyAsLong(val[node], sum[0]);

            if (f.test(next)) {
                sum[0] = next;
                return nodeL;
            }

            return minLeftFail(
                node,
                nodeL,
                nodeR,
                sum,
                f
            );
        }

        int mid = (nodeL + nodeR) >> 1;

        int result = minLeft(
            right[node],
            r,
            mid,
            nodeR,
            sum,
            f
        );

        if (result != mid) {
            return result;
        }

        return minLeft(
            left[node],
            r,
            nodeL,
            mid,
            sum,
            f
        );
    }

    private int minLeftFail(int node,
                            int nodeL,
                            int nodeR,
                            long[] sum,
                            java.util.function.LongPredicate f) {
        if (nodeR - nodeL <= 1) {
            return nodeR;
        }

        int mid = (nodeL + nodeR) >> 1;
        int lc = left[node];
        int rc = right[node];

        long next = op.applyAsLong(val[rc], sum[0]);

        if (!f.test(next)) {
            return minLeftFail(
                rc,
                mid,
                nodeR,
                sum,
                f
            );
        }

        sum[0] = next;

        next = op.applyAsLong(val[lc], sum[0]);

        if (!f.test(next)) {
            return minLeftFail(
                lc,
                nodeL,
                mid,
                sum,
                f
            );
        }

        sum[0] = next;
        return nodeL;
    }

    // -------- Latest APIs --------

    public int setL(int p, long x) {
        return set(latestVersion(), p, x);
    }

    public long getL(int p) {
        return get(latestVersion(), p);
    }

    public long prodL(int l, int r) {
        return prod(latestVersion(), l, r);
    }

    public long allProdL() {
        return allProd(latestVersion());
    }

    public int maxRightL(int l,
                        java.util.function.LongPredicate f) {
        return maxRight(latestVersion(), l, f);
    }

    public int minLeftL(int r,
                       java.util.function.LongPredicate f) {
        return minLeft(latestVersion(), r, f);
    }


    // -------- Internal: Node Util --------

    private int newNode(int l, int r, long v) {
        int id = nodeCount++;
        left[id]  = l;
        right[id] = r;
        val[id]   = v;
        return id;
    }

    private int buildEmpty(int nodeL, int nodeR) {
        if (nodeR - nodeL <= 1) {
            return newNode(NULL, NULL, E);
        }
        int mid = (nodeL + nodeR) >> 1;
        int lc  = buildEmpty(nodeL, mid);
        int rc  = buildEmpty(mid, nodeR);
        return newNode(lc, rc, op.applyAsLong(val[lc], val[rc]));
    }

    private int build(long[] dat, int nodeL, int nodeR) {
        if (nodeR - nodeL <= 1) {
            long v = (nodeL < dat.length) ? dat[nodeL] : E;
            return newNode(NULL, NULL, v);
        }
        int mid = (nodeL + nodeR) >> 1;
        int lc  = build(dat, nodeL, mid);
        int rc  = build(dat, mid, nodeR);
        return newNode(lc, rc, op.applyAsLong(val[lc], val[rc]));
    }

    private int set(int node, int p, long x, int nodeL, int nodeR) {
        if (nodeR - nodeL <= 1) {
            return newNode(NULL, NULL, x);
        }
        int mid = (nodeL + nodeR) >> 1;
        int lc, rc;
        if (p < mid) {
            lc = set(left[node],  p, x, nodeL, mid);
            rc = right[node];
        } else {
            lc = left[node];
            rc = set(right[node], p, x, mid, nodeR);
        }
        return newNode(lc, rc, op.applyAsLong(val[lc], val[rc]));
    }

    private long get(int node, int p, int nodeL, int nodeR) {
        if (nodeR - nodeL <= 1) return val[node];
        int mid = (nodeL + nodeR) >> 1;
        if (p < mid) return get(left[node],  p, nodeL, mid);
        else         return get(right[node], p, mid, nodeR);
    }

    private long prod(int node, int l, int r, int nodeL, int nodeR) {
        if (l <= nodeL && nodeR <= r) return val[node];
        if (r <= nodeL || nodeR <= l) return E;
        int mid = (nodeL + nodeR) >> 1;
        return op.applyAsLong(
            prod(left[node],  l, r, nodeL, mid),
            prod(right[node], l, r, mid, nodeR)
        );
    }

    // -------- Range Checks --------

    private void exclusiveRangeCheck(int p) {
        if (p < 0 || p >= MAX) throw new IndexOutOfBoundsException(
            String.format("Index %d out of bounds for the range [%d, %d).", p, 0, MAX));
    }

    private void inclusiveRangeCheck(int p) {
        if (p < 0 || p > MAX) throw new IndexOutOfBoundsException(
            String.format("Index %d out of bounds for the range [%d, %d].", p, 0, MAX));
    }

    private void checkVersion(int v) {
        if (v < 0 || v >= versionCount) throw new IndexOutOfBoundsException(
            String.format("Version %d out of bounds [0, %d).", v, versionCount));
    }

    // -------- DEBUG --------

    @Override
    public String toString() {
        return toSimpleString(latestVersion());
    }

    public String toSimpleString(int version) {
        checkVersion(version);
        StringBuilder sb = new StringBuilder();
        sb.append("v").append(version).append(": [");
        for (int i = 0; i < MAX; i++) {
            sb.append(get(version, i));
            if (i < MAX - 1) sb.append(", ");
        }
        sb.append(']');
        return sb.toString();
    }

    public String toAllVersionString() {
        StringBuilder sb = new StringBuilder();
        for (int v = 0; v < versionCount; v++) {
            sb.append(toSimpleString(v)).append('\n');
        }
        return sb.toString();
    }
}