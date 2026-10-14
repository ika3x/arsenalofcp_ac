import java.util.*;
import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class DynamicSegTreei64 {
    final int B;
    final java.util.function.LongBinaryOperator op;
    final long E;

    final int NULLPTR = -1, ROOT = 0;
    final IntMutList lp = new IntMutList();
    final IntMutList rp = new IntMutList();
    final LongMutList intervalMin = new LongMutList();
    final IntMutList intervalSize = new IntMutList();
    final LongMutList value = new LongMutList();
    
    public DynamicSegTreei64(int b, LongBinaryOperator op, long e) {
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

    public void set(long idx, long x) {
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
            value.set(path[i], op.applyAsLong(getNodeValue(lp.get(path[i])), getNodeValue(rp.get(path[i]))));
        }
    }

    public long get(long idx) {
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

    public long prod(long l, long r) {
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

    private long prodRec(int node, long l, long r) {
        if (node == NULLPTR) return E;

        long lo = intervalMin.get(node);
        long hi = lo + (1L << intervalSize.get(node));

        if (r <= lo || hi <= l) return E;
        if (l <= lo && hi <= r) return value.get(node);

        return op.applyAsLong(prodRec(lp.get(node), l, r), prodRec(rp.get(node), l, r));
    }

    public long allProd() {
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

    public int maxRight(int l, LongPredicate f) {
        inclusiveRangeCheck(l);

        if (l == size()) return l;

        long[] state = {E};
        int result = maxRightRec(ROOT, l, 0L, size(), state, f);

        return (result == -1) ? (int) size() : result;
    }

    private int maxRightRec(
        int node,
        long l,
        long lo,
        long hi,
        long[] state,
        LongPredicate f
    ) {
        if (node == NULLPTR || hi <= l) return -1;

        if (l <= lo) {
            long next = op.applyAsLong(state[0], getNodeValue(node));

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

    public int minLeft(int r, LongPredicate f) {
        inclusiveRangeCheck(r);

        if (r == 0) return r;

        long[] state = {E};
        int result = minLeftRec(ROOT, r, 0L, size(), state, f);

        return (result == -1) ? 0 : result;
    }

    private int minLeftRec(
        int node,
        long r,
        long lo,
        long hi,
        long[] state,
        LongPredicate f
    ) {
        if (node == NULLPTR || r <= lo) return -1;

        if (hi <= r) {
            long next = op.applyAsLong(getNodeValue(node), state[0]);

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

    private long getNodeValue(int node) {
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
}
