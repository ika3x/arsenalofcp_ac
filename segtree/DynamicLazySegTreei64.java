import java.util.Arrays;
import java.util.function.LongBinaryOperator;
import java.util.function.LongPredicate;

public class DynamicLazySegTreei64 {
    private static final int NULLPTR = -1;
    private static final int ROOT = 0;

    @FunctionalInterface
    public interface LongMapping {
        long apply(long f, long s, long size);
    }

    private final int B;
    private final long DOMAIN_SIZE;

    private final LongBinaryOperator Op;
    private final long E;

    private final LongMapping Mapping;
    private final LongBinaryOperator Composition;
    private final long Id;

    private int[] lp;
    private int[] rp;
    private long[] intervalMin;
    private int[] intervalSize;

    private long[] value;
    private long[] lazy;

    private int nodeCount;

    public DynamicLazySegTreei64(
        int b,
        LongBinaryOperator op,
        long e,
        LongMapping mapping,
        LongBinaryOperator composition,
        long id
    ) {
        if (b < 0 || b >= 63) {
            throw new IllegalArgumentException(
                "b must satisfy 0 <= b < 63."
            );
        }

        this.B = b;
        this.DOMAIN_SIZE = 1L << b;

        this.Op = op;
        this.E = e;
        this.Mapping = mapping;
        this.Composition = composition;
        this.Id = id;

        int capacity = 8;

        this.lp = new int[capacity];
        this.rp = new int[capacity];
        this.intervalMin = new long[capacity];
        this.intervalSize = new int[capacity];
        this.value = new long[capacity];
        this.lazy = new long[capacity];

        this.nodeCount = 1;

        lp[ROOT] = NULLPTR;
        rp[ROOT] = NULLPTR;
        intervalMin[ROOT] = 0L;
        intervalSize[ROOT] = B;
        value[ROOT] = E;
        lazy[ROOT] = Id;
    }

    public long size() {
        return DOMAIN_SIZE;
    }

    /*
     * a[p] = x
     */
    public void set(long p, long x) {
        exclusiveRangeCheck(p);
        setRec(ROOT, p, x);
    }

    private void setRec(int node, long p, long x) {
        int sz = intervalSize[node];

        if (sz == 0) {
            value[node] = x;
            lazy[node] = Id;
            return;
        }

        push(node);

        long mid = intervalMin[node] | (1L << (sz - 1));

        if (p < mid) {
            setRec(ensureLeft(node), p, x);
        } else {
            setRec(ensureRight(node), p, x);
        }

        pull(node);
    }

    /*
     * a[p]
     */
    public long get(long p) {
        exclusiveRangeCheck(p);
        return getRec(ROOT, p);
    }

    private long getRec(int node, long p) {
        if (node == NULLPTR) {
            return E;
        }

        int sz = intervalSize[node];

        if (sz == 0) {
            return value[node];
        }

        push(node);

        long mid = intervalMin[node] | (1L << (sz - 1));

        if (p < mid) {
            return getRec(lp[node], p);
        } else {
            return getRec(rp[node], p);
        }
    }

    /*
     * prod([l, r))
     */
    public long prod(long l, long r) {
        if (l > r) {
            throw new IllegalArgumentException(
                String.format("Invalid range: [%d, %d)", l, r)
            );
        }

        inclusiveRangeCheck(l);
        inclusiveRangeCheck(r);

        if (l == r) {
            return E;
        }

        return prodRec(ROOT, l, r);
    }

    private long prodRec(int node, long l, long r) {
        if (node == NULLPTR) {
            return E;
        }

        long lo = intervalMin[node];
        long hi = lo + (1L << intervalSize[node]);

        if (r <= lo || hi <= l) {
            return E;
        }

        if (l <= lo && hi <= r) {
            return value[node];
        }

        push(node);

        return Op.applyAsLong(
            prodRec(lp[node], l, r),
            prodRec(rp[node], l, r)
        );
    }

    public long allProd() {
        return value[ROOT];
    }

    public void apply(long p, long f) {
        exclusiveRangeCheck(p);
        applyPointRec(ROOT, p, f);
    }

    private void applyPointRec(int node, long p, long f) {
        int sz = intervalSize[node];

        if (sz == 0) {
            applyTo(node, f);
            return;
        }

        push(node);

        long mid = intervalMin[node] | (1L << (sz - 1));

        if (p < mid) {
            applyPointRec(ensureLeft(node), p, f);
        } else {
            applyPointRec(ensureRight(node), p, f);
        }

        pull(node);
    }

    public void apply(long l, long r, long f) {
        if (l > r) {
            throw new IllegalArgumentException(
                String.format("Invalid range: [%d, %d)", l, r)
            );
        }

        inclusiveRangeCheck(l);
        inclusiveRangeCheck(r);

        if (l == r) {
            return;
        }

        applyRec(ROOT, l, r, f);
    }

    private void applyRec(int node, long l, long r, long f) {
        long lo = intervalMin[node];
        long hi = lo + (1L << intervalSize[node]);

        if (r <= lo || hi <= l) {
            return;
        }

        if (l <= lo && hi <= r) {
            applyTo(node, f);
            return;
        }

        if (intervalSize[node] == 0) {
            applyTo(node, f);
            return;
        }

        push(node);

        long mid = lo | (1L << (intervalSize[node] - 1));

        if (l < mid) {
            applyRec(ensureLeft(node), l, r, f);
        }

        if (mid < r) {
            applyRec(ensureRight(node), l, r, f);
        }

        pull(node);
    }

    public long maxRight(long l, LongPredicate f) {
        inclusiveRangeCheck(l);

        if (!f.test(E)) {
            throw new IllegalArgumentException(
                "Identity element must satisfy the condition."
            );
        }

        if (l == DOMAIN_SIZE) {
            return l;
        }

        long[] state = new long[]{E};

        long result = maxRightRec(
            ROOT,
            l,
            0L,
            DOMAIN_SIZE,
            state,
            f
        );

        return result == -1 ? DOMAIN_SIZE : result;
    }

    private long maxRightRec(
        int node,
        long l,
        long lo,
        long hi,
        long[] state,
        LongPredicate f
    ) {
        if (node == NULLPTR || hi <= l) {
            return -1;
        }

        if (l <= lo) {
            long next = Op.applyAsLong(state[0], value[node]);

            if (f.test(next)) {
                state[0] = next;
                return -1;
            }

            if (hi - lo == 1) {
                return lo;
            }
        }

        if (hi - lo == 1) {
            return lo;
        }

        push(node);

        long mid = lo + ((hi - lo) >>> 1);

        long result = maxRightRec(
            lp[node],
            l,
            lo,
            mid,
            state,
            f
        );

        if (result != -1) {
            return result;
        }

        return maxRightRec(
            rp[node],
            l,
            mid,
            hi,
            state,
            f
        );
    }

    public long minLeft(long r, LongPredicate f) {
        inclusiveRangeCheck(r);

        if (!f.test(E)) {
            throw new IllegalArgumentException(
                "Identity element must satisfy the condition."
            );
        }

        if (r == 0) {
            return 0;
        }

        long[] state = new long[]{E};

        long result = minLeftRec(
            ROOT,
            r,
            0L,
            DOMAIN_SIZE,
            state,
            f
        );

        return result == -1 ? 0 : result;
    }

    private long minLeftRec(
        int node,
        long r,
        long lo,
        long hi,
        long[] state,
        LongPredicate f
    ) {
        if (node == NULLPTR || r <= lo) {
            return -1;
        }

        if (hi <= r) {
            long next = Op.applyAsLong(value[node], state[0]);

            if (f.test(next)) {
                state[0] = next;
                return -1;
            }

            if (hi - lo == 1) {
                return hi;
            }
        }

        if (hi - lo == 1) {
            return hi;
        }

        push(node);

        long mid = lo + ((hi - lo) >>> 1);

        long result = minLeftRec(
            rp[node],
            r,
            mid,
            hi,
            state,
            f
        );

        if (result != -1) {
            return result;
        }

        return minLeftRec(
            lp[node],
            r,
            lo,
            mid,
            state,
            f
        );
    }

    private void applyTo(int node, long f) {
        long segmentSize = 1L << intervalSize[node];

        value[node] = Mapping.apply(
            f,
            value[node],
            segmentSize
        );

        if (intervalSize[node] > 0) {
            lazy[node] = Composition.applyAsLong(
                f,
                lazy[node]
            );
        }
    }

    private void push(int node) {
        long f = lazy[node];

        if (f == Id) {
            return;
        }

        if (intervalSize[node] == 0) {
            lazy[node] = Id;
            return;
        }

        int left = ensureLeft(node);
        int right = ensureRight(node);

        applyTo(left, f);
        applyTo(right, f);

        lazy[node] = Id;
    }

    private void pull(int node) {
        value[node] = Op.applyAsLong(
            getNodeValue(lp[node]),
            getNodeValue(rp[node])
        );
    }

    private long getNodeValue(int node) {
        return node == NULLPTR ? E : value[node];
    }

    private int ensureLeft(int node) {
        int child = lp[node];

        if (child == NULLPTR) {
            child = makeNode(
                intervalMin[node],
                intervalSize[node] - 1
            );
            lp[node] = child;
        }

        return child;
    }

    private int ensureRight(int node) {
        int child = rp[node];

        if (child == NULLPTR) {
            long min = intervalMin[node]
                | (1L << (intervalSize[node] - 1));

            child = makeNode(
                min,
                intervalSize[node] - 1
            );
            rp[node] = child;
        }

        return child;
    }

    private int makeNode(long min, int size) {
        ensureCapacity(nodeCount + 1);

        int index = nodeCount++;

        lp[index] = NULLPTR;
        rp[index] = NULLPTR;
        intervalMin[index] = min;
        intervalSize[index] = size;
        value[index] = E;
        lazy[index] = Id;

        return index;
    }

    private void ensureCapacity(int required) {
        if (required <= lp.length) {
            return;
        }

        int newCapacity = lp.length;

        while (newCapacity < required) {
            newCapacity =
                newCapacity <= 128
                    ? newCapacity << 1
                    : newCapacity + (newCapacity >> 1);
        }

        lp = Arrays.copyOf(lp, newCapacity);
        rp = Arrays.copyOf(rp, newCapacity);
        intervalMin = Arrays.copyOf(intervalMin, newCapacity);
        intervalSize = Arrays.copyOf(intervalSize, newCapacity);
        value = Arrays.copyOf(value, newCapacity);
        lazy = Arrays.copyOf(lazy, newCapacity);
    }

    private void exclusiveRangeCheck(long p) {
        if (p < 0 || p >= DOMAIN_SIZE) {
            throw new IndexOutOfBoundsException(
                String.format(
                    "Index %d out of bounds for the range [0, %d).",
                    p,
                    DOMAIN_SIZE
                )
            );
        }
    }

    private void inclusiveRangeCheck(long p) {
        if (p < 0 || p > DOMAIN_SIZE) {
            throw new IndexOutOfBoundsException(
                String.format(
                    "Index %d out of bounds for the range [0, %d].",
                    p,
                    DOMAIN_SIZE
                )
            );
        }
    }

    public int nodeCount() {
        return nodeCount;
    }
}