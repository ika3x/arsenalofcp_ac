class SegTreei64 {
    final int MAX;
    final int N;
    final LongBinaryOperator op;
    final long E;
    final long[] data;

    public SegTreei64(int n, LongBinaryOperator op, long e) {
        this.MAX = n;
        int k = 1;
        while (k < n) k <<= 1;
        this.N = k;
        this.E = e;
        this.op = op;
        this.data = new long[N << 1];
        Arrays.fill(data, E);
    }

    public SegTreei64(long[] dat, LongBinaryOperator op, long e) {
        this(dat.length, op, e);
        System.arraycopy(dat, 0, data, N, dat.length);
        for (int i = N - 1; i > 0; i--) {
            data[i] = op.applyAsLong(data[i << 1], data[i << 1 | 1]);
        }
    }

    public void set(int p, long x) {
        exclusiveRangeCheck(p);
        data[p += N] = x;
        while (p > 1) {
            p >>= 1;
            data[p] = op.applyAsLong(data[p << 1], data[p << 1 | 1]);
        }
    }

    public long get(int p) {
        if (p < 0 || p >= MAX) throw new IndexOutOfBoundsException();
        return data[p + N];
    }

    public long prod(int l, int r) {
        if (l > r) {
            throw new IllegalArgumentException(
                String.format("Invalid range: [%d, %d)", l, r)
            );
        }
        inclusiveRangeCheck(l);
        inclusiveRangeCheck(r);
        long sumLeft = E, sumRight = E;
        for (l += N, r += N; l < r; l >>= 1, r >>= 1) {
            if ((l & 1) == 1) sumLeft = op.applyAsLong(sumLeft, data[l++]);
            if ((r & 1) == 1) sumRight = op.applyAsLong(data[--r], sumRight);
        }
        return op.applyAsLong(sumLeft, sumRight);
    }

    public long allProd() {
        return data[1];
    }

    public int maxRight(int l, LongPredicate f) {
        inclusiveRangeCheck(l);
        if (!f.test(E)) {
            throw new IllegalArgumentException("Identity element must satisfy the condition.");
        }
        if (l == MAX) return MAX;
        l += N;
        long sum = E;
        do {
            l >>= Integer.numberOfTrailingZeros(l);
            if (!f.test(op.applyAsLong(sum, data[l]))) {
                while (l < N) {
                    l <<= 1;
                    if (f.test(op.applyAsLong(sum, data[l]))) {
                        sum = op.applyAsLong(sum, data[l]);
                        l++;
                    }
                }
                return l - N;
            }
            sum = op.applyAsLong(sum, data[l]);
            l++;
        } while ((l & -l) != l);
        return MAX;
    }

    public int minLeft(int r, LongPredicate f) {
        inclusiveRangeCheck(r);
        if (!f.test(E)) {
            throw new IllegalArgumentException("Identity element must satisfy the condition.");
        }
        if (r == 0) return 0;
        r += N;
        long sum = E;
        do {
            r--;
            while (r > 1 && (r & 1) == 1) r >>= 1;
            if (!f.test(op.applyAsLong(data[r], sum))) {
                while (r < N) {
                    r = (r << 1) | 1;
                    if (f.test(op.applyAsLong(data[r], sum))) {
                        sum = op.applyAsLong(data[r], sum);
                        r--;
                    }
                }
                return r + 1 - N;
            }
            sum = op.applyAsLong(data[r], sum);
        } while ((r & -r) != r);
        return 0;
    }

    private void exclusiveRangeCheck(int p) {
        if (p < 0 || p >= MAX) {
            throw new IndexOutOfBoundsException(
                String.format("Index %d out of bounds for the range [%d, %d).", p, 0, MAX)
            );
        }
    }

    private void inclusiveRangeCheck(int p) {
        if (p < 0 || p > MAX) {
            throw new IndexOutOfBoundsException(
                String.format("Index %d out of bounds for the range [%d, %d].", p, 0, MAX)
            );
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SegTreei64([");
        for (int i = 0; i < N; i++) {
            sb.append(data[i + N]);
            if (i < N - 1) sb.append(',').append(' ');
        }
        sb.append("])");
        return sb.toString();
    }
}