import java.util.*;
import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class DynamicSegTree<T> {
    final int B;
    final java.util.function.BinaryOperator<T> op;
    final T E;

    final int NULLPTR = -1, ROOT = 0;
    final IntMutList lp = new IntMutList();
    final IntMutList rp = new IntMutList();
    final LongMutList intervalMin = new LongMutList();
    final IntMutList intervalSize = new IntMutList();
    final ArrayList<T> value = new ArrayList<>();
    
    public DynamicSegTree(int b, BinaryOperator<T> op, T e) {
        this.B = b;
        this.op = op;
        this.E = e;

        lp.add(NULLPTR);
        rp.add(NULLPTR);
        intervalMin.add(0L);
        intervalSize.add(B);
        value.add(E);
    }

    public long size() {
        return 1L << B;
    }

    public void set(long idx, T x) {
        exclusiveRangeCheck(idx);
        int[] path = new int[B + 1];
        int curNode = ROOT;
        path[0] = curNode;
        int pt = 0;
        int is = -1;
        while ((is = intervalSize.get(curNode)) > 0) {
            long bd = intervalMin.get(curNode) | (1L << (is-1));
            int nextNode = makeNodeIfAbsent(curNode, idx < bd);
            path[++pt] = (curNode = nextNode);
        }

        value.set(curNode, x);

        for (int i = B-1; i >= 0; i--) {
            value.set(path[i], op.apply(getNodeValue(lp.get(path[i])), getNodeValue(rp.get(path[i]))));
        }
    }

    public T get(long idx) {
        exclusiveRangeCheck(idx);
        int curNode = ROOT;
        int is = -1;
        while ((is = intervalSize.get(curNode)) > 0) {
            long bd = intervalMin.get(curNode) | (1L << (is-1));
            curNode = ((idx < bd) ? lp : rp).get(curNode);
            if (curNode == NULLPTR) return E;
        }

        return value.get(curNode);
    }

    public T prod(long l, long r) {
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

    private T prodRec(int node, long l, long r) {
        if (node == NULLPTR) return E;

        long lo = intervalMin.get(node);
        long hi = lo + (1L << intervalSize.get(node));

        if (r <= lo || hi <= l) return E;
        if (l <= lo && hi <= r) return value.get(node);

        return op.apply(prodRec(lp.get(node), l, r), prodRec(rp.get(node), l, r));
    }

    public T allProd() {
        return value.get(ROOT);
    }

    private int makeNodeIfAbsent(int node, boolean left) {
        if (left) {
            if (lp.get(node) == NULLPTR) {
                int nextIndex = lp.size();
                lp.set(node, nextIndex);
                
                lp.add(NULLPTR);
                rp.add(NULLPTR);
                intervalMin.add(intervalMin.get(node));
                intervalSize.add(intervalSize.get(node) - 1);
                value.add(E);
            }

            return lp.get(node);
        } else {
            if (rp.get(node) == NULLPTR) {
                int nextIndex = lp.size();
                rp.set(node, nextIndex);
                
                lp.add(NULLPTR);
                rp.add(NULLPTR);
                intervalMin.add(intervalMin.get(node) | (1L << (intervalSize.get(node) - 1)));
                intervalSize.add(intervalSize.get(node) - 1);
                value.add(E);
            }

            return rp.get(node);
        }
    }

    public int maxRight(int l, java.util.function.Predicate<T> f) {
        inclusiveRangeCheck(l);

        if (l == size()) return l;

        T[] state = (T[]) new Object[]{E};
        int result = maxRightRec(ROOT, l, 0L, size(), state, f);

        return (result == -1) ? (int) size() : result;
    }

    private int maxRightRec(
        int node,
        long l,
        long lo,
        long hi,
        T[] state,
        java.util.function.Predicate<T> f
    ) {
        if (node == NULLPTR || hi <= l) return -1;

        if (l <= lo) {
            T next = op.apply(state[0], getNodeValue(node));

            if (f.test(next)) {
                state[0] = next;
                return -1;
            }

            if (hi - lo == 1) return (int) lo;
        }

        long mid = lo + ((hi - lo) >>> 1);

        int result = maxRightRec(
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

    public int minLeft(int r, java.util.function.Predicate<T> f) {
        inclusiveRangeCheck(r);

        if (r == 0) return r;

        T[] state = (T[]) new Object[]{E};
        int result = minLeftRec(ROOT, r, 0L, size(), state, f);

        return (result == -1) ? 0 : result;
    }

    private int minLeftRec(
        int node,
        long r,
        long lo,
        long hi,
        T[] state,
        java.util.function.Predicate<T> f
    ) {
        if (node == NULLPTR || r <= lo) return -1;

        if (hi <= r) {
            T next = op.apply(getNodeValue(node), state[0]);

            if (f.test(next)) {
                state[0] = next;
                return -1;
            }

            if (hi - lo == 1) return (int) hi;
        }

        long mid = lo + ((hi - lo) >>> 1);

        int result = minLeftRec(
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

    private T getNodeValue(int node) {
        return (node == NULLPTR) ? E : value.get(node);
    }

    private void exclusiveRangeCheck(long p) {
        if (p < 0 || p >= size()) {
            throw new IndexOutOfBoundsException(
                String.format("Index %d out of bounds for the range [%d, %d).", p, 0, size())
            );
        }
    }

    private void inclusiveRangeCheck(long p) {
        if (p < 0 || p > size()) {
            throw new IndexOutOfBoundsException(
                String.format("Index %d out of bounds for the range [%d, %d].", p, 0, size())
            );
        }
    }

    private static final int DEFAULT_CAPACITY = 8;

    static class IntMutList implements Iterable<Integer> {
        private int[] dat;
        private int size;
    
        public IntMutList() {
            this(DEFAULT_CAPACITY);
        }
        
        public IntMutList(int capacity) {
            dat = new int[capacity];
            size = 0;
        }
    
        public IntMutList(int[] in) {
            size = in.length;
            dat = new int[size];
            System.arraycopy(in, 0, dat, 0, size);
        }
    
        public void add(int x) {
            if (size == dat.length) {
                grow();
            }
            dat[size++] = x;
        }
    
        public void set(int index, int value) {
            checkIndex(index);
            dat[index] = value;
        }
    
        public int get(int index) {
            checkIndex(index);
            return dat[index];
        }
    
        public int getFirst() {
            return get(0);
        }
        
        public int getLast() {
            return get(size-1);
        }
    
        public int pollLast() {
            if (size == 0) {throw new NoSuchElementException();}
            return dat[--size];
        }
    
        public void removeLast() {
            pollLast();
        }
    
        public void clear() {
            size = 0;
        }
    
        public void sort() {
            Arrays.sort(dat, 0, size);
        }
    
        public int binarySearch(int target) {
            int left = 0;
            int right = size-1;
            while (left <= right) {
                int mid = left + ((right - left) >> 1);
                if (dat[mid] == target) {return mid;}
    
                if (dat[mid] > target) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
            return ~left;
        }
    
        private void checkIndex(int index) {
            if (index < 0 || index >= size) {
                throw new ArrayIndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
            }
        }
    
        public int size() {
            return size;
        }
    
        public boolean isEmpty() {
            return size == 0;
        }
    
        public static IntMutList[] makeArray(int length) {
            IntMutList[] res = new IntMutList[length];
            for (int i = 0; i < res.length; i++) res[i] = new IntMutList();
            return res;
        }
    
        private void grow() {
            int newCap = (dat.length <= 128) ? dat.length << 1 : dat.length + (dat.length >> 1);
            newCap = Math.max(newCap, DEFAULT_CAPACITY);
            int[] newData = new int[newCap];
            System.arraycopy(dat, 0, newData, 0, size);
            dat = newData;
        }
    
        public int[] toArray() {
            int[] res = new int[size];
            System.arraycopy(dat, 0, res, 0, size);
            return res;
        }
    
        @Override
        public String toString() {
            if (size == 0) {return "[]";}
            StringBuilder sb = new StringBuilder("[");
            sb.append(dat[0]);
            for (int i = 1; i < size; i++) {
                sb.append(',').append(' ').append(dat[i]);
            }
            sb.append(']');
    
            return sb.toString();
        }
    
        public String toCPString() {
            if (size == 0) {return "";}
            StringBuilder sb = new StringBuilder(Integer.toString(dat[0]));
            for (int i = 1; i < size; i++) {
                sb.append(' ').append(dat[i]);
            }
    
            return sb.toString();
        }
    
        @Override
        public PrimitiveIterator.OfInt iterator() {
            return new PrimitiveIterator.OfInt() {
                private int cursor = 0;
                @Override
                public boolean hasNext() { return cursor < size; }
                @Override
                public int nextInt() {
                    if (cursor >= size) throw new NoSuchElementException();
                    return dat[cursor++];
                }
            };
        }
    
        public IntStream stream() {
            return Arrays.stream(dat, 0, size);
        }
    }
    
    static class LongMutList implements Iterable<Long> {
        private long[] dat;
        private int size;
    
        public LongMutList() {
            this(DEFAULT_CAPACITY);
        }
        
        public LongMutList(int capacity) {
            dat = new long[capacity];
            size = 0;
        }
    
        public LongMutList(long[] in) {
            size = in.length;
            dat = new long[size];
            System.arraycopy(in, 0, dat, 0, size);
        }
    
        public void add(long x) {
            if (size == dat.length) {
                grow();
            }
            dat[size++] = x;
        }
    
        public void set(int index, long value) {
            checkIndex(index);
            dat[index] = value;
        }
    
        public long get(int index) {
            checkIndex(index);
            return dat[index];
        }
    
        public long getFirst() {
            return get(0);
        }
        
        public long getLast() {
            return get(size-1);
        }
    
        public long pollLast() {
            if (size == 0) {throw new NoSuchElementException();}
            return dat[--size];
        }
    
        public void removeLast() {
            pollLast();
        }
    
        public void clear() {
            size = 0;
        }
    
        public void sort() {
            Arrays.sort(dat, 0, size);
        }
    
        public int binarySearch(long target) {
            int left = 0;
            int right = size-1;
            while (left <= right) {
                int mid = left + ((right - left) >> 1);
                if (dat[mid] == target) {return mid;}
    
                if (dat[mid] > target) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
            return ~left;
        }
    
        private void checkIndex(int index) {
            if (index < 0 || index >= size) {
                throw new ArrayIndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
            }
        }
    
        public int size() {
            return size;
        }
    
        public boolean isEmpty() {
            return size == 0;
        }
    
        private void grow() {
            int newCap = (dat.length <= 128) ? dat.length << 1 : dat.length + (dat.length >> 1);
            newCap = Math.max(newCap, DEFAULT_CAPACITY);
            long[] newData = new long[newCap];
            System.arraycopy(dat, 0, newData, 0, size);
            dat = newData;
        }
    
        public long[] toArray() {
            long[] res = new long[size];
            System.arraycopy(dat, 0, res, 0, size);
            return res;
        }
    
        public static LongMutList[] makeArray(int length) {
            LongMutList[] res = new LongMutList[length];
            for (int i = 0; i < res.length; i++) res[i] = new LongMutList();
            return res;
        }
    
        @Override
        public String toString() {
            if (size == 0) {return "[]";}
            StringBuilder sb = new StringBuilder("[");
            sb.append(dat[0]);
            for (int i = 1; i < size; i++) {
                sb.append(',').append(' ').append(dat[i]);
            }
            sb.append(']');
    
            return sb.toString();
        }
    
        public String toCPString() {
            if (size == 0) {return "";}
            StringBuilder sb = new StringBuilder(Long.toString(dat[0]));
            for (int i = 1; i < size; i++) {
                sb.append(' ').append(dat[i]);
            }
    
            return sb.toString();
        }
    
        @Override
        public PrimitiveIterator.OfLong iterator() {
            return new PrimitiveIterator.OfLong() {
                private int cursor = 0;
                @Override
                public boolean hasNext() { return cursor < size; }
                @Override
                public long nextLong() {
                    if (cursor >= size) throw new NoSuchElementException();
                    return dat[cursor++];
                }
            };
        }
    
        public LongStream stream() {
            return Arrays.stream(dat, 0, size);
        }
    }

    static class LazySegTree<S, F> {
        
        /* &d LazySegTree | 遅延セグメント木 の使い方
        LazySegTree<Long, Long> lazyseg = new LazySegTree<Long,Long>(
            A,                   &c 初期の配列 
            (s0, s1) -> s0 + s1, &e S同士の演算
            0L,                  &e Sの単位元
            (f, s) -> f + s,     &a mapping: FをSに適用
            (f0, f1) -> f0 + f1, &b F同士の演算
            0L                   &b Fの単位元
        );
        */
    
        final int MAX;
    
        final int N;
        final int Log;
        final java.util.function.BinaryOperator<S> Op;
        final S E;
        final java.util.function.BiFunction<F, S, S> Mapping;
        final java.util.function.BinaryOperator<F> Composition;
        final F Id;
    
        final S[] Dat;
        final F[] Laz;
    
        @SuppressWarnings("unchecked")
        public LazySegTree(int n, java.util.function.BinaryOperator<S> op, S e, java.util.function.BiFunction<F, S, S> mapping, java.util.function.BinaryOperator<F> composition, F id) {
            this.MAX = n;
            int k = 1;
            while (k < n) k <<= 1;
            this.N = k;
            this.Log = Integer.numberOfTrailingZeros(N);
            this.Op = op;
            this.E = e;
            this.Mapping = mapping;
            this.Composition = composition;
            this.Id = id;
            this.Dat = (S[]) new Object[N << 1];
            this.Laz = (F[]) new Object[N];
            java.util.Arrays.fill(Dat, E);
            java.util.Arrays.fill(Laz, Id);
        }
    
        public LazySegTree(S[] dat, java.util.function.BinaryOperator<S> op, S e, java.util.function.BiFunction<F, S, S> mapping, java.util.function.BinaryOperator<F> composition, F id) {
            this(dat.length, op, e, mapping, composition, id);
            build(dat);
        }
    
        private void build(S[] dat) {
            int l = dat.length;
            System.arraycopy(dat, 0, Dat, N, l);
            for (int i = N - 1; i > 0; i--) {
                Dat[i] = Op.apply(Dat[i << 1 | 0], Dat[i << 1 | 1]);
            }
        }
    
        private void push(int k) {
            if (Laz[k] == Id) return;
            int lk = k << 1 | 0, rk = k << 1 | 1;
            Dat[lk] = Mapping.apply(Laz[k], Dat[lk]);
            Dat[rk] = Mapping.apply(Laz[k], Dat[rk]);
            if (lk < N) Laz[lk] = Composition.apply(Laz[k], Laz[lk]);
            if (rk < N) Laz[rk] = Composition.apply(Laz[k], Laz[rk]);
            Laz[k] = Id;
        }
    
        private void pushTo(int k) {
            for (int i = Log; i > 0; i--) push(k >> i);
        }
    
        private void pushTo(int lk, int rk) {
            for (int i = Log; i > 0; i--) {
                if (((lk >> i) << i) != lk) push(lk >> i);
                if (((rk >> i) << i) != rk) push(rk >> i);
            }
        }
    
        private void updateFrom(int k) {
            k >>= 1;
            while (k > 0) {
                Dat[k] = Op.apply(Dat[k << 1 | 0], Dat[k << 1 | 1]);
                k >>= 1;
            }
        }
    
        private void updateFrom(int lk, int rk) {
            for (int i = 1; i <= Log; i++) {
                if (((lk >> i) << i) != lk) {
                    int lki = lk >> i;
                    Dat[lki] = Op.apply(Dat[lki << 1 | 0], Dat[lki << 1 | 1]);
                }
                if (((rk >> i) << i) != rk) {
                    int rki = (rk - 1) >> i;
                    Dat[rki] = Op.apply(Dat[rki << 1 | 0], Dat[rki << 1 | 1]);
                }
            }
        }
    
        public void set(int p, S x) {
            exclusiveRangeCheck(p);
            p += N;
            pushTo(p);
            Dat[p] = x;
            updateFrom(p);
        }
    
        public S get(int p) {
            exclusiveRangeCheck(p);
            p += N;
            pushTo(p);
            return Dat[p];
        }
    
        public S prod(int l, int r) {
            if (l > r) {
                throw new IllegalArgumentException(
                    String.format("Invalid range: [%d, %d)", l, r)
                );
            }
            inclusiveRangeCheck(l);
            inclusiveRangeCheck(r);
            if (l == r) return E;
            l += N; r += N;
            pushTo(l, r);
            S sumLeft = E, sumRight = E;
            while (l < r) {
                if ((l & 1) == 1) sumLeft = Op.apply(sumLeft, Dat[l++]);
                if ((r & 1) == 1) sumRight = Op.apply(Dat[--r], sumRight);
                l >>= 1; r >>= 1;
            }
            return Op.apply(sumLeft, sumRight);
        }
    
        public S allProd() {
            return Dat[1];
        }
    
        public void apply(int p, F f) {
            exclusiveRangeCheck(p);
            p += N;
            pushTo(p);
            Dat[p] = Mapping.apply(f, Dat[p]);
            updateFrom(p);
        }
    
        public void apply(int l, int r, F f) {
            if (l > r) {
                throw new IllegalArgumentException(
                    String.format("Invalid range: [%d, %d)", l, r)
                );
            }
            inclusiveRangeCheck(l);
            inclusiveRangeCheck(r);
            if (l == r) return;
            l += N; r += N;
            pushTo(l, r);
            for (int l2 = l, r2 = r; l2 < r2;) {
                if ((l2 & 1) == 1) {
                    Dat[l2] = Mapping.apply(f, Dat[l2]);
                    if (l2 < N) Laz[l2] = Composition.apply(f, Laz[l2]);
                    l2++;
                }
                if ((r2 & 1) == 1) {
                    r2--;
                    Dat[r2] = Mapping.apply(f, Dat[r2]);
                    if (r2 < N) Laz[r2] = Composition.apply(f, Laz[r2]);
                }
                l2 >>= 1; r2 >>= 1;
            }
            updateFrom(l, r);
        }
    
        public int maxRight(int l, java.util.function.Predicate<S> g) {
            inclusiveRangeCheck(l);
            if (!g.test(E)) {
                throw new IllegalArgumentException("Identity element must satisfy the condition.");
            }
            if (l == MAX) return MAX;
            l += N;
            pushTo(l);
            S sum = E;
            do {
                l >>= Integer.numberOfTrailingZeros(l);
                if (!g.test(Op.apply(sum, Dat[l]))) {
                    while (l < N) {
                        push(l);
                        l = l << 1;
                        if (g.test(Op.apply(sum, Dat[l]))) {
                            sum = Op.apply(sum, Dat[l]);
                            l++;
                        }
                    }
                    return l - N;
                }
                sum = Op.apply(sum, Dat[l]);
                l++;
            } while ((l & -l) != l);
            return MAX;
        }
    
        public int minLeft(int r, java.util.function.Predicate<S> g) {
            inclusiveRangeCheck(r);
            if (!g.test(E)) {
                throw new IllegalArgumentException("Identity element must satisfy the condition.");
            }
            if (r == 0) return 0;
            r += N;
            pushTo(r - 1);
            S sum = E;
            do {
                r--;
                while (r > 1 && (r & 1) == 1) r >>= 1;
                if (!g.test(Op.apply(Dat[r], sum))) {
                    while (r < N) {
                        push(r);
                        r = r << 1 | 1;
                        if (g.test(Op.apply(Dat[r], sum))) {
                            sum = Op.apply(Dat[r], sum);
                            r--;
                        }
                    }
                    return r + 1 - N;
                }
                sum = Op.apply(Dat[r], sum);
            } while ((r & -r) != r);
            return 0;
        }
    
        private void exclusiveRangeCheck(int p) {
            if (p < 0 || p >= MAX) {
                throw new IndexOutOfBoundsException(
                    String.format("Index %d is not in [%d, %d).", p, 0, MAX)
                );
            }
        }
    
        private void inclusiveRangeCheck(int p) {
            if (p < 0 || p > MAX) {
                throw new IndexOutOfBoundsException(
                    String.format("Index %d is not in [%d, %d].", p, 0, MAX)
                );
            }
        }
    
        // **************** DEBUG **************** //
    
        private int indent = 6;
    
        public void setIndent(int newIndent) {
            this.indent = newIndent;
        }
    
        @Override
        public String toString() {
            return toSimpleString();
        }
    
        private S[] simulatePushAll() {
            S[] simDat = java.util.Arrays.copyOf(Dat, 2 * N);
            F[] simLaz = java.util.Arrays.copyOf(Laz, 2 * N);
            for (int k = 1; k < N; k++) {
                if (simLaz[k] == Id) continue;
                int lk = k << 1 | 0, rk = k << 1 | 1;
                simDat[lk] = Mapping.apply(simLaz[k], simDat[lk]);
                simDat[rk] = Mapping.apply(simLaz[k], simDat[rk]);
                if (lk < N) simLaz[lk] = Composition.apply(simLaz[k], simLaz[lk]);
                if (rk < N) simLaz[rk] = Composition.apply(simLaz[k], simLaz[rk]);
                simLaz[k] = Id;
            }
            return simDat;
        }
    
        public String toDetailedString() {
            return toDetailedString(1, 0, simulatePushAll());
        }
    
        private String toDetailedString(int k, int sp, S[] dat) {
            if (k >= N) return indent(sp) + dat[k];
            String s = "";
            s += toDetailedString(k << 1 | 1, sp + indent, dat);
            s += "\n";
            s += indent(sp) + dat[k];
            s += "\n";
            s += toDetailedString(k << 1 | 0, sp + indent, dat);
            return s;
        }
    
        private static String indent(int n) {
            StringBuilder sb = new StringBuilder();
            while (n --> 0) sb.append(' ');
            return sb.toString();
        }
    
        public String toSimpleString() {
            S[] dat = simulatePushAll();
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < N; i++) {
                sb.append(dat[i + N]);
                if (i < N - 1) sb.append(',').append(' ');
            }
            sb.append(']');
            return sb.toString();
        }
    }
}
