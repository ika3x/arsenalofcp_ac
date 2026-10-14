class XorWeightedDSU {
    private final int n;
    private final int[] parentOrSize;

    private final long[] weight;

    public XorWeightedDSU(int n) {
        if (n < 0) throw new IllegalArgumentException("n=" + n);

        this.n = n;
        this.parentOrSize = new int[n];
        this.weight = new long[n];

        java.util.Arrays.fill(parentOrSize, -1);
    }

    private void checkIndex(int a) {
        if (!(0 <= a && a < n)) {
            throw new IndexOutOfBoundsException("a=" + a);
        }
    }

    public boolean addEdge(int x, int y, long diff) {
        checkIndex(x);
        checkIndex(y);

        int rx = leader(x);
        int ry = leader(y);

        long wx = weight[x];
        long wy = weight[y];

        if (rx == ry) {
            return (wy ^ wx) == diff;
        }

        if (-parentOrSize[rx] >= -parentOrSize[ry]) {
            parentOrSize[rx] += parentOrSize[ry];
            parentOrSize[ry] = rx;

            weight[ry] = wy ^ wx ^ diff;
        } else {
            parentOrSize[ry] += parentOrSize[rx];
            parentOrSize[rx] = ry;

            weight[rx] = diff ^ wx ^ wy;
        }
        
        return true;
    }

    public boolean same(int a, int b) {
        checkIndex(a);
        checkIndex(b);
        return leader(a) == leader(b);
    }

    public int leader(int a) {
        checkIndex(a);

        if (parentOrSize[a] < 0) {
            return a;
        }

        int p = parentOrSize[a];
        parentOrSize[a] = leader(p);

        weight[a] ^= weight[p];

        return parentOrSize[a];
    }

    public long potential(int a) {
        checkIndex(a);
        leader(a);
        return weight[a];
    }

    public long diff(int x, int y) {
        checkIndex(x);
        checkIndex(y);

        if (leader(x) != leader(y)) {
            throw new IllegalArgumentException(
                "x and y are not connected: x=" + x + ", y=" + y
            );
        }

        return weight[y] ^ weight[x];
    }

    public int size(int a) {
        checkIndex(a);
        return -parentOrSize[leader(a)];
    }

    java.util.ArrayList<java.util.ArrayList<Integer>> groups() {
        int[] leaderBuf = new int[n];
        int[] groupSize = new int[n];

        for (int i = 0; i < n; i++) {
            leaderBuf[i] = leader(i);
            groupSize[leaderBuf[i]]++;
        }

        java.util.ArrayList<java.util.ArrayList<Integer>> result =
            new java.util.ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            result.add(new java.util.ArrayList<>(groupSize[i]));
        }

        for (int i = 0; i < n; i++) {
            result.get(leaderBuf[i]).add(i);
        }

        result.removeIf(java.util.ArrayList::isEmpty);
        return result;
    }
}