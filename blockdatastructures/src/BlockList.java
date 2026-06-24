import java.util.*;
import java.util.stream.*;


class BlockList implements Iterable<Long> {

    // ────────────────────────────────────────────────
    //  Inner: LongMutList (customized for bl)
    // ────────────────────────────────────────────────
    private static final class LongMutList implements Iterable<Long> {
        private long[] dat;
        private int size;

        LongMutList(int capacity) { dat = new long[capacity]; }

        void add(long x) { if (size == dat.length) grow(); dat[size++] = x; }

        void addAll(LongMutList other) {
            for (int i = 0; i < other.size(); i++) add(other.get(i));
        }

        void insert(int index, long x) {
            if (size == dat.length) grow();
            System.arraycopy(dat, index, dat, index + 1, size - index);
            dat[index] = x;
            size++;
        }

        void removeAt(int index) {
            System.arraycopy(dat, index + 1, dat, index, size - index - 1);
            size--;
        }

        long pollLast()                  { return dat[--size]; }
        long get(int index)              { return dat[index]; }
        void set(int index, long value)  { dat[index] = value; }
        long getLast()                   { return dat[size - 1]; }


        int size()        { return size; }
        boolean isEmpty() { return size == 0; }
        void clear()      { size = 0; }
        void trimTo(int newSize) { size = newSize; }

        long[] toArray() { return Arrays.copyOf(dat, size); }

        private void grow() {
            dat = Arrays.copyOf(dat, dat.length <= 128
                    ? dat.length << 1 : dat.length + (dat.length >> 1));
        }

        @Override
        public PrimitiveIterator.OfLong iterator() {
            return new PrimitiveIterator.OfLong() {
                int cursor = 0;
                public boolean hasNext() { return cursor < size; }
                public long nextLong() {
                    if (cursor >= size) throw new NoSuchElementException();
                    return dat[cursor++];
                }
            };
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < size; i++) { if(i>0) sb.append(", "); sb.append(dat[i]); }
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
        void rebuild(ArrayList<LongMutList> blocks) {
            int m = blocks.size();
            ensureCap(m);
            Arrays.fill(tree, 0, m + 1, 0);
            // O(m) build via cascading update
            for (int i = 1; i <= m; i++) {
                tree[i] += blocks.get(i - 1).size();
                int j = i + (i & -i);
                if (j <= m) tree[j] += tree[i];
            }
        }

        /** Add delta to block at 1-based position pos. O(log m) */
        void update(int pos, int delta) {
            for (; pos <= cap; pos += pos & -pos) tree[pos] += delta;
        }

        /**
         * Prefix sum of block sizes [1..pos]. O(log m)
         * = total element count in blocks[0..pos-1]
         */
        int query(int pos) {
            int s = 0;
            for (; pos > 0; pos -= pos & -pos) s += tree[pos];
            return s;
        }

        /**
         * Find the 1-based block index containing the k-th element (0-based k).
         * Returns the pair (blockIndex1based, offsetWithinBlock) via a 2-element int[].
         * Uses BIT descent: O(log m).
         */
        int[] find(int k, int numBlocks) {
            // BIT descent (find smallest prefix-sum > k)
            int pos = 0, rem = k;
            for (int pw = Integer.highestOneBit(numBlocks); pw > 0; pw >>= 1) {
                int next = pos + pw;
                if (next <= numBlocks && tree[next] <= rem) {
                    rem -= tree[next];
                    pos  = next;
                }
            }
            // pos is last block where prefix-sum <= k, so answer is pos+1 (1-based)
            return new int[]{ pos + 1, rem };
        }

        /** Ensure internal array can hold at least m blocks. */
        void ensureCap(int m) {
            if (m <= cap) return;
            int newCap = Math.max(m, cap + (cap >> 1));
            tree = Arrays.copyOf(tree, newCap + 1);
            cap  = newCap;
        }

        /**
         * Insert a new block at 1-based position pos with given initial size.
         * Requires a full rebuild because shifting is O(m) anyway. O(m).
         */
        void rebuildNeeded() { /* caller calls rebuild() explicitly */ }
    }

    // ────────────────────────────────────────────────
    //  BlockList Main Body
    // ────────────────────────────────────────────────

    private static final int DEFAULT_BLOCK = 750;

    private final ArrayList<LongMutList> blocks;
    private final BlockBIT               bit;
    private final int                    blockSize;
    private int                          totalSize;

    public BlockList() { this(DEFAULT_BLOCK); }

    public BlockList(int blockSize) {
        this.blockSize = blockSize;
        this.blocks    = new ArrayList<>();
        this.bit       = new BlockBIT(16);
        this.totalSize = 0;
    }

    // ── Insertion O(√n) ──────────────────────────────

    public void add(long x) {
        addLast(x);
    }

    public void addFirst(long x) {
        insertAt(0, x);
    }

    public void addLast(long x) {
        totalSize++;
        if (blocks.isEmpty()) {
            LongMutList bl = new LongMutList(blockSize + 1);
            bl.add(x);
            blocks.add(bl);
            bit.ensureCap(1);
            bit.rebuild(blocks);
            return;
        }
        int last = blocks.size() - 1;
        LongMutList bl = blocks.get(last);
        bl.add(x);
        bit.update(last + 1, +1);
        if (bl.size() > blockSize * 2) split(last);
    }

    public void insertAt(int index, long x) {
        if (index < 0 || index > totalSize) throw new IndexOutOfBoundsException(index);
        if (index == totalSize) {
            addLast(x); // (index == totalSize) -> addLast
            return;
        }
        totalSize++;
        int[] res = bit.find(index, blocks.size());
        int bi = res[0] - 1, posInBlock = res[1];
        LongMutList bl = blocks.get(bi);
        bl.insert(posInBlock, x);
        bit.update(bi + 1, +1);
        if (bl.size() > blockSize * 2) split(bi);
    }

    public void insertAfter(int index, long x) {
        insertAt(index + 1, x);
    }

    // ── Deletion O(√n) ───────────────────────────────

    public long removeAt(int index) {
        if (index < 0 || index >= totalSize) throw new IndexOutOfBoundsException(index);
        int[] res = bit.find(index, blocks.size());
        int bi = res[0] - 1, posInBlock = res[1];
        LongMutList bl = blocks.get(bi);
        long v = bl.get(posInBlock);
        bl.removeAt(posInBlock);
        bit.update(bi + 1, -1);
        totalSize--;
        if (bl.isEmpty()) {
            blocks.remove(bi);
            bit.rebuild(blocks);
        } else if (bl.size() < blockSize / 2 && blocks.size() > 1) {
            merge(bi);
        }
        return v;
    }

    public long removeFirst() {
        if (totalSize == 0) throw new NoSuchElementException();
        return removeAt(0);
    }

    public long removeLast() {
        if (totalSize == 0) throw new NoSuchElementException();
        return removeAt(totalSize - 1);
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

    // ── k-th Element Access O(log n) ─────────────────

    /**
     * Returns the k-th element (0-based) in O(log m) using BIT descent,
     * then O(1) array access inside the located block.
     */
    public long get(int k) {
        if (k < 0 || k >= totalSize) throw new IndexOutOfBoundsException(k);
        int[] res = bit.find(k, blocks.size());  // O(log m)
        return blocks.get(res[0] - 1).get(res[1]);
    }

    // ── First / Last Elements ────────────────────────

    public long getFirst() {
        if (totalSize == 0) throw new NoSuchElementException();
        return blocks.get(0).get(0);
    }

    public long getLast() {
        if (totalSize == 0) throw new NoSuchElementException();
        LongMutList bl = blocks.get(blocks.size() - 1);
        return bl.get(bl.size() - 1);
    }

    public long pollFirst() {
        if (totalSize == 0) throw new NoSuchElementException();
        LongMutList bl = blocks.get(0);
        long v = bl.get(0);
        bl.removeAt(0);
        bit.update(1, -1);
        totalSize--;
        if (bl.isEmpty()) { blocks.remove(0); bit.rebuild(blocks); }
        return v;
    }

    public long pollLast() {
        if (totalSize == 0) throw new NoSuchElementException();
        int last = blocks.size() - 1;
        LongMutList bl = blocks.get(last);
        long v = bl.pollLast();
        bit.update(last + 1, -1);
        totalSize--;
        if (bl.isEmpty()) { blocks.remove(last); bit.rebuild(blocks); }
        return v;
    }

    // ── Basic Operations ─────────────────────────────

    public int size()        { return totalSize; }
    public boolean isEmpty() { return totalSize == 0; }

    public void clear() {
        blocks.clear();
        bit.rebuild(blocks);
        totalSize = 0;
    }

    public void set(int index, long value) {
        if (index < 0 || index >= totalSize) throw new IndexOutOfBoundsException(index);
        int[] res = bit.find(index, blocks.size());
        blocks.get(res[0] - 1).set(res[1], value);
    }

    public void sort() {
        long[] arr = toArray();
        Arrays.sort(arr);
        clear();
        // Rebuild blocks
        for (int i = 0; i < arr.length; i += blockSize) {
            int end = Math.min(i + blockSize, arr.length);
            LongMutList bl = new LongMutList(blockSize + 1);
            for (int j = i; j < end; j++) bl.add(arr[j]);
            blocks.add(bl);
        }
        totalSize = arr.length;
        bit.ensureCap(blocks.size());
        bit.rebuild(blocks);
    }

    public long[] toArray() {
        long[] res = new long[totalSize];
        int i = 0;
        for (LongMutList bl : blocks)
            for (PrimitiveIterator.OfLong it = bl.iterator(); it.hasNext();)
                res[i++] = it.nextLong();
        return res;
    }

    public LongStream stream() { return Arrays.stream(toArray()); }

    // ── Internal Utility ──────────────────────────────

    /** Splits block bi into two halves. Rebuilds BIT. */
    private void split(int bi) {
        LongMutList src = blocks.get(bi);
        int half = src.size() / 2;
        LongMutList neo = new LongMutList(blockSize + 1);
        for (int i = half; i < src.size(); i++) neo.add(src.get(i));
        src.trimTo(half);
        blocks.add(bi + 1, neo);
        bit.rebuild(blocks);   // O(m) — structural change
    }

    // ── toString / iterator ───────────────────────────

    @Override
    public String toString() {
        if (totalSize == 0) return "BlockList([])";
        StringBuilder sb = new StringBuilder("BlockList([");
        boolean first = true;
        for (LongMutList bl : blocks)
            for (PrimitiveIterator.OfLong it = bl.iterator(); it.hasNext();) {
                if (!first) sb.append(", ");
                sb.append(it.nextLong());
                first = false;
            }
        return sb.append("])").toString();
    }

    @Override
    public PrimitiveIterator.OfLong iterator() {
        return new PrimitiveIterator.OfLong() {
            private int bi = 0;
            private PrimitiveIterator.OfLong cur =
                blocks.isEmpty() ? emptyLongIterator() : blocks.get(0).iterator();

            public boolean hasNext() {
                while (!cur.hasNext() && bi + 1 < blocks.size())
                    cur = blocks.get(++bi).iterator();
                return cur.hasNext();
            }
            public long nextLong() {
                if (!hasNext()) throw new NoSuchElementException();
                return cur.nextLong();
            }
        };
    }

    public PrimitiveIterator.OfLong rangedIterator(int l, int r) {
        if (l < 0 || r > totalSize || l > r) throw new IndexOutOfBoundsException();
        
        return new PrimitiveIterator.OfLong() {
            private int currentIdx = l;
            private final int endIdx = r;
            
            private int bi;
            private int posInBlock;
            private PrimitiveIterator.OfLong cur;

            {
                int[] startPos = bit.find(l, blocks.size());
                bi = startPos[0] - 1;
                posInBlock = startPos[1];
                cur = blocks.get(bi).iterator();
                
                for (int i = 0; i < posInBlock; i++) {
                    cur.nextLong();
                }
            }

            public boolean hasNext() {
                return currentIdx < endIdx;
            }

            public long nextLong() {
                if (!hasNext()) throw new NoSuchElementException();
                
                if (!cur.hasNext()) {
                    bi++;
                    cur = blocks.get(bi).iterator();
                }
                
                currentIdx++;
                return cur.nextLong();
            }
        };
    }

    private static PrimitiveIterator.OfLong emptyLongIterator() {
        return new PrimitiveIterator.OfLong() {
            public boolean hasNext() { return false; }
            public long nextLong()   { throw new NoSuchElementException(); }
        };
    }

    public String toCPString() {
        return stream().mapToObj(Long::toString).collect(Collectors.joining(" "));
    }
}