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
    @FunctionalInterface
    public interface MergeOpi64 {
        long apply(long l, long r);
    }

    private int nodeCount;
    private final int[] left, right;
    private final long[] val;

    private int versionCount;
    private final int[] roots;

    private final int MAX;
    private final int N;
    private final MergeOpi64 op;
    private final long E;

    private static final int NULL = 0;

    // set(): ~ maxVersions times
    @SuppressWarnings("unchecked")
    public PersistentSegTreei64(int n,
                             MergeOpi64 op,
                             long e,
                             int maxVersions) {
        this.MAX = n;
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

        roots[0] = buildEmpty(1, N);
        versionCount = 1;
    }

    @SuppressWarnings("unchecked")
    public PersistentSegTreei64(long[] dat,
                             MergeOpi64 op,
                             long e,
                             int maxVersions) {
        this.MAX = dat.length;
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

        roots[0] = build(dat, 1, N);
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
        return newNode(lc, rc, op.apply(val[lc], val[rc]));
    }

    private int build(long[] dat, int nodeL, int nodeR) {
        if (nodeR - nodeL <= 1) {
            long v = (nodeL < dat.length) ? dat[nodeL] : E;
            return newNode(NULL, NULL, v);
        }
        int mid = (nodeL + nodeR) >> 1;
        int lc  = build(dat, nodeL, mid);
        int rc  = build(dat, mid, nodeR);
        return newNode(lc, rc, op.apply(val[lc], val[rc]));
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
        return newNode(lc, rc, op.apply(val[lc], val[rc]));
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
        return op.apply(
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