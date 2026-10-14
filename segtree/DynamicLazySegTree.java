import java.util.ArrayList;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

class DynamicLazySegTree<S, F> {
    static final int NULLPTR = -1;
    static final int ROOT = 0;

    final int B;
    final BinaryOperator<S> Op;
    final S E;

    final BiFunction<F, S, S> Mapping;
    final BinaryOperator<F> Composition;
    final F Id;

    // interval = [intervalMin[node],
    //             intervalMin[node] + 2 ^ intervalSize[node])
    final IntMutList lp = new IntMutList();
    final IntMutList rp = new IntMutList();
    final LongMutList intervalMin = new LongMutList();
    final IntMutList intervalSize = new IntMutList();

    final ArrayList<S> value = new ArrayList<>();
    final ArrayList<F> lazy = new ArrayList<>();

    public DynamicLazySegTree(
        int b,
        BinaryOperator<S> op,
        S e,
        BiFunction<F, S, S> mapping,
        BinaryOperator<F> composition,
        F id
    ) {
        if (b < 0 || b >= 63) {
            throw new IllegalArgumentException("B must satisfy 0 <= B < 63.");
        }

        this.B = b;
        this.Op = op;
        this.E = e;
        this.Mapping = mapping;
        this.Composition = composition;
        this.Id = id;

        lp.add(NULLPTR);
        rp.add(NULLPTR);
        intervalMin.add(0L);
        intervalSize.add(B);
        value.add(E);
        lazy.add(Id);
    }

    public long size() {
        return 1L << B;
    }

    /*
     * a[p] = x
     */
    public void set(long p, S x) {
        exclusiveRangeCheck(p);
        setRec(ROOT, p, x);
    }

    private void setRec(int node, long p, S x) {
        int sz = intervalSize.get(node);

        if (sz == 0) {
            value.set(node, x);
            lazy.set(node, Id);
            return;
        }

        push(node);

        long mid = intervalMin.get(node) | (1L << (sz - 1));

        if (p < mid) {
            int child = ensureLeft(node);
            setRec(child, p, x);
        } else {
            int child = ensureRight(node);
            setRec(child, p, x);
        }

        pull(node);
    }

    /*
     * a[p]
     */
    public S get(long p) {
        exclusiveRangeCheck(p);
        return getRec(ROOT, p);
    }

    private S getRec(int node, long p) {
        if (node == NULLPTR) return E;

        int sz = intervalSize.get(node);
        if (sz == 0) {
            return value.get(node);
        }

        push(node);

        long mid = intervalMin.get(node) | (1L << (sz - 1));

        if (p < mid) {
            return getRec(lp.get(node), p);
        } else {
            return getRec(rp.get(node), p);
        }
    }

    /*
     * prod([l, r))
     */
    public S prod(long l, long r) {
        if (l > r) {
            throw new IllegalArgumentException(
                String.format("Invalid range: [%d, %d)", l, r)
            );
        }

        inclusiveRangeCheck(l);
        inclusiveRangeCheck(r);

        if (l == r) return E;

        return prodRec(ROOT, l, r);
    }

    private S prodRec(int node, long l, long r) {
        if (node == NULLPTR) return E;

        long lo = intervalMin.get(node);
        long hi = lo + (1L << intervalSize.get(node));

        if (r <= lo || hi <= l) return E;

        if (l <= lo && hi <= r) {
            return value.get(node);
        }

        push(node);

        return Op.apply(
            prodRec(lp.get(node), l, r),
            prodRec(rp.get(node), l, r)
        );
    }

    public S allProd() {
        return value.get(ROOT);
    }

    /*
     * a[p] = Mapping(f, a[p])
     */
    public void apply(long p, F f) {
        exclusiveRangeCheck(p);
        applyPointRec(ROOT, p, f);
    }

    private void applyPointRec(int node, long p, F f) {
        int sz = intervalSize.get(node);

        if (sz == 0) {
            applyTo(node, f);
            return;
        }

        push(node);

        long mid = intervalMin.get(node) | (1L << (sz - 1));

        if (p < mid) {
            applyPointRec(ensureLeft(node), p, f);
        } else {
            applyPointRec(ensureRight(node), p, f);
        }

        pull(node);
    }

    /*
     * Mapping f to every element in [l, r)
     */
    public void apply(long l, long r, F f) {
        if (l > r) {
            throw new IllegalArgumentException(
                String.format("Invalid range: [%d, %d)", l, r)
            );
        }

        inclusiveRangeCheck(l);
        inclusiveRangeCheck(r);

        if (l == r) return;

        applyRec(ROOT, l, r, f);
    }

    private void applyRec(int node, long l, long r, F f) {
        long lo = intervalMin.get(node);
        long hi = lo + (1L << intervalSize.get(node));

        if (r <= lo || hi <= l) return;

        if (l <= lo && hi <= r) {
            applyTo(node, f);
            return;
        }

        if (intervalSize.get(node) == 0) {
            applyTo(node, f);
            return;
        }

        push(node);

        applyRec(ensureLeft(node), l, r, f);
        applyRec(ensureRight(node), l, r, f);

        pull(node);
    }

    /*
     * Returns the maximum r such that
     * f(prod(l, r)) is true.
     *
     * f must satisfy f(E) == true and must be monotonic
     * with respect to the operation.
     */
    public long maxRight(long l, Predicate<S> f) {
        inclusiveRangeCheck(l);

        if (!f.test(E)) {
            throw new IllegalArgumentException(
                "Identity element must satisfy the condition."
            );
        }

        if (l == size()) return l;

        Object[] state = new Object[]{E};

        long result = maxRightRec(
            ROOT,
            l,
            0L,
            size(),
            state,
            f
        );

        return result == -1 ? size() : result;
    }

    @SuppressWarnings("unchecked")
    private long maxRightRec(
        int node,
        long l,
        long lo,
        long hi,
        Object[] state,
        Predicate<S> f
    ) {
        if (node == NULLPTR || hi <= l) return -1;

        if (l <= lo) {
            S next = Op.apply(
                (S) state[0],
                value.get(node)
            );

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
            lp.get(node),
            l,
            lo,
            mid,
            state,
            f
        );

        if (result != -1) return result;

        return maxRightRec(
            rp.get(node),
            l,
            mid,
            hi,
            state,
            f
        );
    }

    /*
     * Returns the minimum l such that
     * f(prod(l, r)) is true.
     *
     * f must satisfy f(E) == true and must be monotonic
     * with respect to the operation.
     */
    public long minLeft(long r, Predicate<S> f) {
        inclusiveRangeCheck(r);

        if (!f.test(E)) {
            throw new IllegalArgumentException(
                "Identity element must satisfy the condition."
            );
        }

        if (r == 0) return 0;

        Object[] state = new Object[]{E};

        long result = minLeftRec(
            ROOT,
            r,
            0L,
            size(),
            state,
            f
        );

        return result == -1 ? 0 : result;
    }

    @SuppressWarnings("unchecked")
    private long minLeftRec(
        int node,
        long r,
        long lo,
        long hi,
        Object[] state,
        Predicate<S> f
    ) {
        if (node == NULLPTR || r <= lo) return -1;

        if (hi <= r) {
            S next = Op.apply(
                value.get(node),
                (S) state[0]
            );

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
            rp.get(node),
            r,
            mid,
            hi,
            state,
            f
        );

        if (result != -1) return result;

        return minLeftRec(
            lp.get(node),
            r,
            lo,
            mid,
            state,
            f
        );
    }

    /*
     * Apply an action to one node.
     */
    private void applyTo(int node, F f) {
        value.set(node, Mapping.apply(f, value.get(node)));

        if (intervalSize.get(node) > 0) {
            lazy.set(
                node,
                Composition.apply(f, lazy.get(node))
            );
        }
    }

    /*
     * Push lazy[node] to its children.
     *
     * Both children are created because an absent child also has
     * to receive the pending action.
     */
    private void push(int node) {
        F f = lazy.get(node);

        if (Objects.equals(f, Id)) return;
        if (intervalSize.get(node) == 0) {
            lazy.set(node, Id);
            return;
        }

        int left = ensureLeft(node);
        int right = ensureRight(node);

        applyTo(left, f);
        applyTo(right, f);

        lazy.set(node, Id);
    }

    private void pull(int node) {
        value.set(
            node,
            Op.apply(
                getNodeValue(lp.get(node)),
                getNodeValue(rp.get(node))
            )
        );
    }

    private S getNodeValue(int node) {
        return node == NULLPTR ? E : value.get(node);
    }

    private int ensureLeft(int node) {
        int child = lp.get(node);

        if (child == NULLPTR) {
            child = makeNode(
                intervalMin.get(node),
                intervalSize.get(node) - 1
            );
            lp.set(node, child);
        }

        return child;
    }

    private int ensureRight(int node) {
        int child = rp.get(node);

        if (child == NULLPTR) {
            long min = intervalMin.get(node)
                | (1L << (intervalSize.get(node) - 1));

            child = makeNode(
                min,
                intervalSize.get(node) - 1
            );
            rp.set(node, child);
        }

        return child;
    }

    private int makeNode(long min, int size) {
        int index = lp.size();

        lp.add(NULLPTR);
        rp.add(NULLPTR);
        intervalMin.add(min);
        intervalSize.add(size);
        value.add(E);
        lazy.add(Id);

        return index;
    }

    private void exclusiveRangeCheck(long p) {
        if (p < 0 || p >= size()) {
            throw new IndexOutOfBoundsException(
                String.format(
                    "Index %d out of bounds for the range [%d, %d).",
                    p,
                    0,
                    size()
                )
            );
        }
    }

    private void inclusiveRangeCheck(long p) {
        if (p < 0 || p > size()) {
            throw new IndexOutOfBoundsException(
                String.format(
                    "Index %d out of bounds for the range [%d, %d].",
                    p,
                    0,
                    size()
                )
            );
        }
    }

    // =========================================================
    // Primitive mutable lists
    // =========================================================

    static final int DEFAULT_CAPACITY = 8;

    static class IntMutList {
        private int[] data;
        private int size;

        IntMutList() {
            this(DEFAULT_CAPACITY);
        }

        IntMutList(int capacity) {
            data = new int[Math.max(1, capacity)];
        }

        void add(int x) {
            if (size == data.length) grow();
            data[size++] = x;
        }

        void set(int index, int x) {
            checkIndex(index);
            data[index] = x;
        }

        int get(int index) {
            checkIndex(index);
            return data[index];
        }

        int size() {
            return size;
        }

        private void checkIndex(int index) {
            if (index < 0 || index >= size) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
        }

        private void grow() {
            int newCapacity =
                data.length <= 128
                    ? data.length << 1
                    : data.length + (data.length >> 1);

            int[] next = new int[newCapacity];
            System.arraycopy(data, 0, next, 0, size);
            data = next;
        }
    }

    static class LongMutList {
        private long[] data;
        private int size;

        LongMutList() {
            this(DEFAULT_CAPACITY);
        }

        LongMutList(int capacity) {
            data = new long[Math.max(1, capacity)];
        }

        void add(long x) {
            if (size == data.length) grow();
            data[size++] = x;
        }

        void set(int index, long x) {
            checkIndex(index);
            data[index] = x;
        }

        long get(int index) {
            checkIndex(index);
            return data[index];
        }

        private void checkIndex(int index) {
            if (index < 0 || index >= size) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
        }

        private void grow() {
            int newCapacity =
                data.length <= 128
                    ? data.length << 1
                    : data.length + (data.length >> 1);

            long[] next = new long[newCapacity];
            System.arraycopy(data, 0, next, 0, size);
            data = next;
        }
    }
}