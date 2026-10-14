static class FenwickTree {
    private int size;
    private long[] data;

    public FenwickTree(int n){
        this.size = n;
        data = new long[n];
    }
    
    public FenwickTree(long[] data) {
        this(data.length);
        build(data);
    }

    public void set(int p, long x){
        add(p, x - get(p));
    }

    public void add(int p, long x){
        assert(0<=p && p<size);
        p++;
        while(p<=size){
            data[p-1] += x;
            p += p&-p;
        }
    }
    public long sum(int l, int r){
        assert(0<=l && l<=r && r<=size);
        return sum(r)-sum(l);
    }

    public long get(int p){
        return sum(p, p+1);
    }
    
    // Returns the maximum r such that f(sum(0, r)) is true.
    // Equivalently, the first r for which f(sum(0, r)) is false.
    // Requires f(0) == true, all values non-negative,
    // and f to be monotone on prefix sums.
    public int maxRight(LongPredicate f) {
        assert f.test(0L);

        long sm = 0;
        int pos = 0;

        for (int k = Integer.highestOneBit(size); k != 0; k >>= 1) {
            int nxt = pos + k;
            if (nxt <= size) {
                long nsm = sm + data[nxt - 1];

                if (f.test(nsm)) {
                    pos = nxt;
                    sm = nsm;
                }
            }
        }

        return pos;
    }

    private long sum(int r){
        long s = 0;
        while(r>0){
            s += data[r-1];
            r -= r&-r;
        }
        return s;
    }

    private void build(long[] dat) {
        System.arraycopy(dat, 0, data, 0, size);
        for (int i=1; i<=size; i++) {
            int p = i+(i&-i);
            if(p<=size){
                data[p-1] += data[i-1];
            }
        }
    }
}
