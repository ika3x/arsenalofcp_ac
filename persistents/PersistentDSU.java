class PersistentDSU {
    private final int[] parent;
    private final int[] unionTime;
    private final List<List<int[]>> sizeHistory;
    private static final int INF = Integer.MAX_VALUE;
    private int currentTime;

    public PersistentDSU(int n) {
        parent = new int[n];
        unionTime = new int[n];
        sizeHistory = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            unionTime[i] = INF;
            List<int[]> hist = new ArrayList<>();
            hist.add(new int[]{0, 1});
            sizeHistory.add(hist);
        }
        currentTime = 0;
    }

    public int leader(int x, int t) {
        while (unionTime[x] <= t) {
            x = parent[x];
        }
        return x;
    }

    public boolean same(int x, int y, int t) {
        return leader(x, t) == leader(y, t);
    }

    public int size(int x, int t) {
        int r = leader(x, t);
        List<int[]> hist = sizeHistory.get(r);
        int lo = 0, hi = hist.size() - 1, ans = 0;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            if (hist.get(mid)[0] <= t) { ans = mid; lo = mid + 1; }
            else hi = mid - 1;
        }
        return hist.get(ans)[1];
    }

    private int findCurrentRoot(int x) {
        while (parent[x] != x) x = parent[x];
        return x;
    }

    private int currentSize(int root) {
        List<int[]> h = sizeHistory.get(root);
        return h.get(h.size() - 1)[1];
    }

    public int merge(int x, int y) {
        int rx = findCurrentRoot(x);
        int ry = findCurrentRoot(y);
        if (rx == ry) return currentTime;

        currentTime++;
        int sx = currentSize(rx), sy = currentSize(ry);
        if (sx < sy) {
            int t = rx; rx = ry; ry = t;
            int ts = sx; sx = sy; sy = ts;
        }
        parent[ry] = rx;
        unionTime[ry] = currentTime;
        sizeHistory.get(rx).add(new int[]{currentTime, sx + sy});
        return currentTime;
    }

    public int latestTime() {
        return currentTime;
    }
}