public class CommutativeFenwickTree {
    private final int size;
    private final long[] data;
    private final LongBinaryOperator op;
    private final long e;

    public CommutativeFenwickTree(
            int n,
            LongBinaryOperator op,
            long e
    ) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative: " + n);
        }

        this.size = n;
        this.data = new long[n];
        this.op = Objects.requireNonNull(op);
        this.e = e;

        java.util.Arrays.fill(this.data, e);
    }

    public CommutativeFenwickTree(
            long[] data,
            LongBinaryOperator op,
            long e
    ) {
        Objects.requireNonNull(data);

        this.size = data.length;
        this.data = data.clone();
        this.op = Objects.requireNonNull(op);
        this.e = e;

        build();
    }

    public long prod(int r) {
        checkRange(0, r);

        long result = e;

        while (r > 0) {
            result = op.applyAsLong(data[r - 1], result);
            r -= r & -r;
        }

        return result;
    }

    public void apply(int p, long x) {
        checkIndex(p);

        for (int i = p + 1; i <= size; i += i & -i) {
            data[i - 1] = op.applyAsLong(x, data[i - 1]);
        }
    }

    public int maxRight(LongPredicate f) {
        Objects.requireNonNull(f);

        if (!f.test(e)) {
            throw new IllegalArgumentException("f(e) must be true");
        }

        long prefix = e;
        int position = 0;

        for (int step = Integer.highestOneBit(size);
             step != 0;
             step >>= 1) {
            int next = position + step;

            if (next <= size) {
                long candidate =
                        op.applyAsLong(prefix, data[next - 1]);

                if (f.test(candidate)) {
                    position = next;
                    prefix = candidate;
                }
            }
        }

        return position;
    }

    public int size() {
        return size;
    }

    private void build() {
        for (int i = 1; i <= size; i++) {
            int parent = i + (i & -i);

            if (parent <= size) {
                data[parent - 1] =
                        op.applyAsLong(data[parent - 1], data[i - 1]);
            }
        }
    }

    private void checkIndex(int p) {
        if (p < 0 || p >= size) {
            throw new IndexOutOfBoundsException(
                    "index: " + p + ", size: " + size
            );
        }
    }

    private void checkRange(int l, int r) {
        if (l < 0 || l > r || r > size) {
            throw new IndexOutOfBoundsException(
                    "range: [" + l + ", " + r + "), size: " + size
            );
        }
    }
}