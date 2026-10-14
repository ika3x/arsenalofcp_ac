import java.util.*;
import java.util.stream.*;

class SortedListGenerics<T> implements Iterable<T> {

    // ────────────────────────────────────────────────
    //  Inner: MutList<T> (customized for SortedListGenerics)
    // ────────────────────────────────────────────────
    private static final class MutList<T> implements Iterable<T> {
        private Object[] dat;
        private int size;

        MutList(int capacity) { dat = new Object[capacity]; }

        void add(T x) { if (size == dat.length) grow(); dat[size++] = x; }

        void addAll(MutList<T> other) {
            for (int i = 0; i < other.size(); i++) add(other.get(i));
        }

        void insert(int index, T x) {
            if (size == dat.length) grow();
            System.arraycopy(dat, index, dat, index + 1, size - index);
            dat[index] = x;
            size++;
        }

        void removeAt(int index) {
            System.arraycopy(dat, index + 1, dat, index, size - index - 1);
            size--;
        }

        @SuppressWarnings("unchecked")
        T pollLast()                 { return (T) dat[--size]; }
        @SuppressWarnings("unchecked")
        T get(int index)             { return (T) dat[index]; }
        void set(int index, T value) { dat[index] = value; }
        @SuppressWarnings("unchecked")
        T getLast()                  { return (T) dat[size - 1]; }

        int lowerBound(T x, Comparator<? super T> cmp) {
            int lo = 0, hi = size;
            while (lo < hi) { int mid=(lo+hi)>>>1; if(cmp.compare(get(mid), x) < 0) lo=mid+1; else hi=mid; }
            return lo;
        }

        int upperBound(T x, Comparator<? super T> cmp) {
            int lo = 0, hi = size;
            while (lo < hi) { int mid=(lo+hi)>>>1; if(cmp.compare(get(mid), x) <= 0) lo=mid+1; else hi=mid; }
            return lo;
        }

        int size()        { return size; }
        boolean isEmpty() { return size == 0; }
        void clear()      { size = 0; }
        void trimTo(int newSize) { size = newSize; }

        @SuppressWarnings("unchecked")
        T[] toArray(T[] template) { return (T[]) Arrays.copyOf(dat, size, template.getClass()); }

        private void grow() {
            dat = Arrays.copyOf(dat, dat.length <= 128
                    ? dat.length << 1 : dat.length + (dat.length >> 1));
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                int cursor = 0;
                public boolean hasNext() { return cursor < size; }
                public T next() {
                    if (cursor >= size) throw new NoSuchElementException();
                    return get(cursor++);
                }
            };
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < size; i++) { if(i>0) sb.append(", "); sb.append(get(i)); }
            return sb.append(']').toString();
        }
    }

    // ────────────────────────────────────────────────
    //  Inner: BlockBIT
    //  BIT over block sizes → prefix-sum of block sizes = rank boundary
    //  Index: 1-based, length = blocks.size() (resized dynamically)
    // ────────────────────────────────────────────────
    private static final class BlockBIT {
        private int[] tree;   // 1-based Fenwick tree storing block sizes
        private int   cap;    // current allocated capacity (# of blocks)

        BlockBIT(int initialCap) {
            cap  = Math.max(initialCap, 8);
            tree = new int[cap + 1];
        }

        /** Rebuild BIT from scratch using current block-size array. O(m) */
        <T> void rebuild(ArrayList<MutList<T>> blocks) {
            int m = blocks.size();
            ensureCap(m);
            Arrays.fill(tree, 0, m + 1, 0);
            for (int i = 1; i <= m; i++) {
                tree[i] += blocks.get(i - 1).size();
                int j = i + (i & -i);
                if (j <= m) tree[j] += tree[i];
            }
        }

        void update(int pos, int delta) {
            for (; pos <= cap; pos += pos & -pos) tree[pos] += delta;
        }

        int query(int pos) {
            int s = 0;
            for (; pos > 0; pos -= pos & -pos) s += tree[pos];
            return s;
        }

        int[] find(int k, int numBlocks) {
            int pos = 0, rem = k;
            for (int pw = Integer.highestOneBit(numBlocks); pw > 0; pw >>= 1) {
                int next = pos + pw;
                if (next <= numBlocks && tree[next] <= rem) {
                    rem -= tree[next];
                    pos  = next;
                }
            }
            return new int[]{ pos + 1, rem };
        }

        void ensureCap(int m) {
            if (m <= cap) return;
            int newCap = Math.max(m, cap + (cap >> 1));
            tree = Arrays.copyOf(tree, newCap + 1);
            cap  = newCap;
        }
    }

    // ────────────────────────────────────────────────
    //  SortedListGenerics Main Body
    // ────────────────────────────────────────────────

    private static final int DEFAULT_BLOCK = 750;

    @SuppressWarnings("rawtypes")
    private static final Comparator NATURAL_ORDER =
            (a, b) -> ((Comparable) a).compareTo(b);

    private final ArrayList<MutList<T>> blocks;
    private final BlockBIT              bit;
    private final Comparator<? super T> cmp;
    private final int                   blockSize;
    private int                         totalSize;

    public SortedListGenerics() { this(DEFAULT_BLOCK); }

    @SuppressWarnings("unchecked")
    public SortedListGenerics(int blockSize) {
        this(blockSize, (Comparator<T>) NATURAL_ORDER);
    }

    public SortedListGenerics(Comparator<? super T> comparator) {
        this(DEFAULT_BLOCK, comparator);
    }

    public SortedListGenerics(int blockSize, Comparator<? super T> comparator) {
        this.blockSize = blockSize;
        this.blocks    = new ArrayList<>();
        this.bit       = new BlockBIT(16);
        this.cmp       = comparator;
        this.totalSize = 0;
    }

    @SuppressWarnings("unchecked")
    public SortedListGenerics(T[] sorted, int blockSize) {
        this(sorted, blockSize, (Comparator<T>) NATURAL_ORDER);
    }

    public SortedListGenerics(T[] sorted, int blockSize, Comparator<? super T> comparator) {
        this.blockSize = blockSize;
        this.cmp       = comparator;
        this.totalSize = sorted.length;
        this.blocks    = new ArrayList<>((sorted.length + blockSize - 1) / blockSize);
        for (int i = 0; i < sorted.length; i += blockSize) {
            int end = Math.min(i + blockSize, sorted.length);
            MutList<T> bl = new MutList<>(blockSize + 1);
            for (int j = i; j < end; j++) bl.add(sorted[j]);
            blocks.add(bl);
        }
        this.bit = new BlockBIT(blocks.size() + 16);
        bit.rebuild(blocks);
    }

    public void add(T x) {
        totalSize++;
        if (blocks.isEmpty()) {
            MutList<T> bl = new MutList<>(blockSize + 1);
            bl.add(x);
            blocks.add(bl);
            bit.ensureCap(1);
            bit.rebuild(blocks);
            return;
        }
        int bi = findBlock(x);
        MutList<T> bl = blocks.get(bi);
        bl.insert(bl.lowerBound(x, cmp), x);
        bit.update(bi + 1, +1);
        if (bl.size() > blockSize * 2) split(bi);
    }

    public boolean remove(T x) {
        if (blocks.isEmpty()) return false;
        int bi = findBlock(x);
        MutList<T> bl = blocks.get(bi);
        int pos = bl.lowerBound(x, cmp);
        if (pos >= bl.size() || cmp.compare(bl.get(pos), x) != 0) return false;
        bl.removeAt(pos);
        bit.update(bi + 1, -1);
        totalSize--;
        if (bl.isEmpty()) {
            blocks.remove(bi);
            bit.rebuild(blocks);
        } else if (bl.size() < blockSize / 2 && blocks.size() > 1) {
            merge(bi);
        }
        return true;
    }

    private void merge(int bi) {
        if (bi + 1 < blocks.size()) {
            blocks.get(bi).addAll(blocks.get(bi + 1));
            blocks.remove(bi + 1);
        } else {
            blocks.get(bi - 1).addAll(blocks.get(bi));
            blocks.remove(bi);
            bi = bi - 1;
        }
        bit.rebuild(blocks);
        if (blocks.get(bi).size() > blockSize * 2) split(bi);
    }

    public T get(int k) {
        if (k < 0 || k >= totalSize) throw new IndexOutOfBoundsException(String.valueOf(k));
        int[] res = bit.find(k, blocks.size());
        return blocks.get(res[0] - 1).get(res[1]);
    }

    public int lowerBound(T x) {
        int bi = findFirstBlockWithLastGe(x);
        if (bi < 0) return totalSize;
        int offset = bi == 0 ? 0 : bit.query(bi);
        return offset + blocks.get(bi).lowerBound(x, cmp);
    }

    public int upperBound(T x) {
        int bi = findFirstBlockWithLastGt(x);
        if (bi < 0) return totalSize;
        int offset = bi == 0 ? 0 : bit.query(bi);
        return offset + blocks.get(bi).upperBound(x, cmp);
    }

    // ── floor / ceiling / lower / higher ─────────────

    public T floor(T x) {
        int idx = upperBound(x) - 1;
        return idx >= 0 ? get(idx) : null;
    }

    public T ceiling(T x) {
        int idx = lowerBound(x);
        return idx < totalSize ? get(idx) : null;
    }

    public T lower(T x) {
        int idx = lowerBound(x) - 1;
        return idx >= 0 ? get(idx) : null;
    }

    public T higher(T x) {
        int idx = upperBound(x);
        return idx < totalSize ? get(idx) : null;
    }

    public boolean contains(T x) {
        int idx = lowerBound(x);
        return idx < totalSize && cmp.compare(get(idx), x) == 0;
    }
    public int countEqual(T x)          { return upperBound(x) - lowerBound(x); }
    public int countLess(T x)           { return lowerBound(x); }
    public int countLessOrEqual(T x)    { return upperBound(x); }
    public int countGreaterOrEqual(T x) { return totalSize - lowerBound(x); }
    public int countGreater(T x)        { return totalSize - upperBound(x); }

    public T getFirst() {
        if (totalSize == 0) throw new NoSuchElementException();
        return blocks.get(0).get(0);
    }

    public T getLast() {
        if (totalSize == 0) throw new NoSuchElementException();
        MutList<T> bl = blocks.get(blocks.size() - 1);
        return bl.get(bl.size() - 1);
    }

    public T pollFirst() {
        if (totalSize == 0) throw new NoSuchElementException();
        MutList<T> bl = blocks.get(0);
        T v = bl.get(0);
        bl.removeAt(0);
        bit.update(1, -1);
        totalSize--;
        if (bl.isEmpty()) { blocks.remove(0); bit.rebuild(blocks); }
        return v;
    }

    public T pollLast() {
        if (totalSize == 0) throw new NoSuchElementException();
        int last = blocks.size() - 1;
        MutList<T> bl = blocks.get(last);
        T v = bl.pollLast();
        bit.update(last + 1, -1);
        totalSize--;
        if (bl.isEmpty()) { blocks.remove(last); bit.rebuild(blocks); }
        return v;
    }

    public int size()        { return totalSize; }
    public boolean isEmpty() { return totalSize == 0; }

    public void clear() {
        blocks.clear();
        bit.rebuild(blocks);
        totalSize = 0;
    }

    @SuppressWarnings("unchecked")
    public Object[] toArray() {
        Object[] res = new Object[totalSize];
        int i = 0;
        for (MutList<T> bl : blocks)
            for (T v : bl) res[i++] = v;
        return res;
    }

    public Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    private int findBlock(T x) {
        int lo = 0, hi = blocks.size() - 1;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (cmp.compare(blocks.get(mid).getLast(), x) < 0) lo = mid + 1; else hi = mid;
        }
        return lo;
    }

    private int findFirstBlockWithLastGe(T x) {
        int lo = 0, hi = blocks.size() - 1, ans = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (cmp.compare(blocks.get(mid).getLast(), x) >= 0) { ans = mid; hi = mid - 1; }
            else lo = mid + 1;
        }
        return ans;
    }

    private int findFirstBlockWithLastGt(T x) {
        int lo = 0, hi = blocks.size() - 1, ans = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (cmp.compare(blocks.get(mid).getLast(), x) > 0) { ans = mid; hi = mid - 1; }
            else lo = mid + 1;
        }
        return ans;
    }

    private void split(int bi) {
        MutList<T> src = blocks.get(bi);
        int half = src.size() / 2;
        MutList<T> neo = new MutList<>(blockSize + 1);
        for (int i = half; i < src.size(); i++) neo.add(src.get(i));
        src.trimTo(half);
        blocks.add(bi + 1, neo);
        bit.rebuild(blocks);
    }

    // ── toString / iterator ───────────────────────────

    @Override
    public String toString() {
        if (totalSize == 0) return "SortedListGenerics([])";
        StringBuilder sb = new StringBuilder("SortedListGenerics([");
        boolean first = true;
        for (MutList<T> bl : blocks)
            for (T v : bl) {
                if (!first) sb.append(", ");
                sb.append(v);
                first = false;
            }
        return sb.append("])").toString();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int bi = 0;
            private Iterator<T> cur =
                blocks.isEmpty() ? Collections.emptyIterator() : blocks.get(0).iterator();

            public boolean hasNext() {
                while (!cur.hasNext() && bi + 1 < blocks.size())
                    cur = blocks.get(++bi).iterator();
                return cur.hasNext();
            }
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                return cur.next();
            }
        };
    }

    public String toCPString() {
        return stream().map(String::valueOf).collect(Collectors.joining(" "));
    }
}