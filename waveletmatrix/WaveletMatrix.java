import java.util.*;

/**
 * Wavelet Matrix for values in [0, Long.MAX_VALUE] (63-bit range is enough,
 * but this implementation actually supports the full 64-bit unsigned range
 * by using unsigned comparisons everywhere. If you only need [0, Long.MAX_VALUE],
 * that's automatically covered since it's a subset of the 64-bit unsigned range).
 *
 * Supports the standard set of queries:
 *  - access(k)                       : value at position k
 *  - rangeFreq(l, r, x)              : count of x in [l, r)
 *  - rangeFreq(l, r, lower, upper)   : count of values in [lower, upper) within [l, r)
 *  - select(x, k)                    : position of the (k+1)-th (0-indexed) occurrence of x
 *  - quantile(l, r, k)               : k-th smallest (0-indexed) value in [l, r)
 *  - kthLargest(l, r, k)             : wrapper around quantile for k-th largest (0-indexed)
 *  - prevValue(l, r, upper)          : largest value < upper in [l, r) (or null)
 *  - nextValue(l, r, lower)          : smallest value >= lower in [l, r) (or null)
 *  - topK(l, r, k)                   : top-k most frequent values in [l, r) with counts
 *
 * Values are treated as UNSIGNED 64-bit longs (i.e. Long.compareUnsigned).
 * Since the requested domain is [0, Long.MAX_VALUE], all values are already
 * non-negative Java longs, so ordinary comparisons would also work, but we use
 * unsigned comparisons throughout so the structure is correct for the full
 * 64-bit unsigned domain as well (no downside, same behavior for non-negative longs).
 */
public class WaveletMatrix {
    // Number of bits used for the domain. 64 covers [0, Long.MAX_VALUE] and beyond.
    private final int bitLen;
    private final int n;
    // Each level: a bit vector (with rank support) over n elements
    private final BitVector[] bv;
    // Number of zeros at each level (elements with bit=0 come first after stable partition)
    private final int[] zeroCount;
    // mid[level] is a shortcut equal to zeroCount[level]
    private final long[] originalValues; // kept only for reference size / not required, but useful for debug

    /**
     * Build a wavelet matrix over the given array.
     * @param values input array, all values interpreted as unsigned 64-bit
     * @param bitLen number of bits to use (e.g. 64 for full unsigned range,
     *               63 is enough for [0, Long.MAX_VALUE])
     */
    public WaveletMatrix(long[] values, int bitLen) {
        this.n = values.length;
        this.bitLen = bitLen;
        this.bv = new BitVector[bitLen];
        this.zeroCount = new int[bitLen];
        this.originalValues = values;

        long[] cur = Arrays.copyOf(values, n);
        long[] next = new long[n];

        for (int level = 0; level < bitLen; level++) {
            int shift = bitLen - 1 - level;
            BitVector b = new BitVector(n);
            for (int i = 0; i < n; i++) {
                long bit = (cur[i] >>> shift) & 1L;
                if (bit == 1) b.set(i);
            }
            b.build();
            bv[level] = b;

            // stable partition: zeros first, then ones, preserving relative order
            int zi = 0, oi = 0;
            // count zeros
            int zeros = n - b.rank1(n);
            zeroCount[level] = zeros;
            int[] posZero = null; // not needed; do it via two passes
            for (int i = 0; i < n; i++) {
                long bit = (cur[i] >>> shift) & 1L;
                if (bit == 0) {
                    next[zi++] = cur[i];
                } else {
                    next[zeros + oi] = cur[i];
                    oi++;
                }
            }
            long[] tmp = cur;
            cur = next;
            next = tmp;
        }
    }

    /** Convenience constructor: uses 64 bits (full unsigned range). */
    public WaveletMatrix(long[] values) {
        this(values, 64);
    }

    public int length() {
        return n;
    }

    // ---------- access ----------

    /** Returns the value at position k (0-indexed). */
    public long access(int k) {
        long val = 0;
        int pos = k;
        for (int level = 0; level < bitLen; level++) {
            BitVector b = bv[level];
            long bit = b.get(pos);
            val = (val << 1) | bit;
            if (bit == 0) {
                pos = b.rank0(pos);
            } else {
                pos = zeroCount[level] + b.rank1(pos);
            }
        }
        return val;
    }

    // ---------- rank / freq ----------

    /** Number of occurrences of value x in [0, r). */
    private int rankPrefix(long x, int r) {
        int lo = 0, hi = r;
        for (int level = 0; level < bitLen; level++) {
            BitVector b = bv[level];
            int shift = bitLen - 1 - level;
            long bit = (x >>> shift) & 1L;
            if (bit == 0) {
                lo = b.rank0(lo);
                hi = b.rank0(hi);
            } else {
                lo = zeroCount[level] + b.rank1(lo);
                hi = zeroCount[level] + b.rank1(hi);
            }
        }
        return hi - lo;
    }

    /** Number of occurrences of value x within [l, r). */
    public int rangeFreq(int l, int r, long x) {
        return rankPrefix(x, r) - rankPrefix(x, l);
    }

    /**
     * Number of values v in [l, r) such that lower <= v < upper (unsigned comparison).
     * If upper <= lower (unsigned), returns 0.
     */
    public int rangeFreq(int l, int r, long lower, long upper) {
        if (Long.compareUnsigned(upper, lower) <= 0) return 0;
        return rangeFreqBelow(l, r, upper) - rangeFreqBelow(l, r, lower);
    }

    /** Count of values v in [l, r) with v < upper (unsigned). Helper for range-freq-by-value-range. */
    private int rangeFreqBelow(int l, int r, long upper) {
        if (l >= r) return 0;
        int lo = l, hi = r;
        int count = 0;
        for (int level = 0; level < bitLen && lo < hi; level++) {
            BitVector b = bv[level];
            int shift = bitLen - 1 - level;
            long bit = (upper >>> shift) & 1L;
            int lo0 = b.rank0(lo), hi0 = b.rank0(hi);
            if (bit == 1) {
                // all zero-branch elements at this level are < upper (their bit is 0 while upper's bit is 1)
                count += (hi0 - lo0);
                lo = zeroCount[level] + b.rank1(lo);
                hi = zeroCount[level] + b.rank1(hi);
            } else {
                lo = lo0;
                hi = hi0;
            }
        }
        return count;
    }

    // ---------- select ----------

    /**
     * Returns the position (0-indexed, in the original array) of the (k+1)-th
     * (0-indexed k) occurrence of value x. Returns -1 if it does not exist.
     */
    public long select(long x, int k) {
        // First check existence / bounds using rank at n
        int total = rangeFreq(0, n, x);
        if (k < 0 || k >= total) return -1;

        // Compute the range [lo, hi) at the bottom level corresponding to value x,
        // then walk back up to find the original position.
        int lo = 0, hi = n;
        for (int level = 0; level < bitLen; level++) {
            BitVector b = bv[level];
            int shift = bitLen - 1 - level;
            long bit = (x >>> shift) & 1L;
            if (bit == 0) {
                lo = b.rank0(lo);
                hi = b.rank0(hi);
            } else {
                lo = zeroCount[level] + b.rank1(lo);
                hi = zeroCount[level] + b.rank1(hi);
            }
        }
        // position in bottom-level array
        int pos = lo + k;
        // walk back up from bottom level to level 0 to find original index
        for (int level = bitLen - 1; level >= 0; level--) {
            BitVector b = bv[level];
            long bit = (x >>> (bitLen - 1 - level)) & 1L;
            if (bit == 0) {
                pos = b.select0(pos);
            } else {
                pos = b.select1(pos - zeroCount[level]);
            }
        }
        return pos;
    }

    // ---------- quantile (kth smallest / kth largest) ----------

    /**
     * Returns the k-th smallest value (0-indexed) among elements in [l, r).
     * k = 0 means the smallest.
     * Throws IllegalArgumentException if k is out of range.
     */
    public long quantile(int l, int r, int k) {
        int len = r - l;
        if (k < 0 || k >= len) {
            throw new IllegalArgumentException("k out of range: k=" + k + ", range size=" + len);
        }
        int lo = l, hi = r;
        long val = 0;
        for (int level = 0; level < bitLen; level++) {
            BitVector b = bv[level];
            int lo0 = b.rank0(lo), hi0 = b.rank0(hi);
            int zerosInRange = hi0 - lo0;
            if (k < zerosInRange) {
                // go to zero branch
                lo = lo0;
                hi = hi0;
                val = val << 1;
            } else {
                // go to one branch
                k -= zerosInRange;
                lo = zeroCount[level] + (lo - lo0);
                hi = zeroCount[level] + (hi - hi0);
                val = (val << 1) | 1L;
            }
        }
        return val;
    }

    /**
     * Wrapper: returns the k-th LARGEST value (0-indexed) among elements in [l, r).
     * k = 0 means the largest value.
     * This just converts k-th-largest into the equivalent k-th-smallest index for you,
     * so you don't have to do the (len - 1 - k) math yourself.
     */
    public long kthLargest(int l, int r, int k) {
        int len = r - l;
        if (k < 0 || k >= len) {
            throw new IllegalArgumentException("k out of range: k=" + k + ", range size=" + len);
        }
        return quantile(l, r, len - 1 - k);
    }

    // ---------- prev / next value ----------

    /**
     * Largest value v in [l, r) such that v < upper (unsigned comparison).
     * Returns null (as Long) if no such value exists.
     */
    public Long prevValue(int l, int r, long upper) {
        if (l >= r) return null;
        int cnt = rangeFreqBelow(l, r, upper);
        if (cnt == 0) return null;
        return quantile(l, r, cnt - 1);
    }

    /**
     * Smallest value v in [l, r) such that v >= lower (unsigned comparison).
     * Returns null (as Long) if no such value exists.
     */
    public Long nextValue(int l, int r, long lower) {
        if (l >= r) return null;
        int cnt = rangeFreqBelow(l, r, lower); // count of values < lower
        int len = r - l;
        if (cnt >= len) return null;
        return quantile(l, r, cnt);
    }

    // ---------- top-k frequent values ----------

    /** Simple holder for (value, count) pairs used by topK. */
    public static final class ValueCount {
        public final long value;
        public final int count;
        public ValueCount(long value, int count) {
            this.value = value;
            this.count = count;
        }
        @Override public String toString() {
            return "(" + Long.toUnsignedString(value) + ", " + count + ")";
        }
    }

    /**
     * Returns up to k values with the highest frequency within [l, r), sorted by
     * frequency descending (ties broken by value ascending, unsigned).
     * Implemented via a recursive descent on the wavelet tree using a max-heap on count,
     * which is much faster than scanning every distinct value for large ranges.
     */
    public List<ValueCount> topK(int l, int r, int k) {
        List<ValueCount> result = new ArrayList<>();
        if (l >= r || k <= 0) return result;

        // Each heap node represents a sub-range at some level, with an accumulated value prefix.
        // We expand nodes lazily; a node's "count" is (hi - lo) at that level.
        PriorityQueue<Node> heap = new PriorityQueue<>(
                (a, b) -> b.count != a.count ? Integer.compare(b.count, a.count)
                        : Long.compareUnsigned(a.prefix, b.prefix)
        );
        heap.add(new Node(0, l, r, 0, 0L));

        while (!heap.isEmpty() && result.size() < k) {
            Node cur = heap.poll();
            if (cur.level == bitLen) {
                result.add(new ValueCount(cur.prefix, cur.count));
                continue;
            }
            BitVector b = bv[cur.level];
            int lo0 = b.rank0(cur.lo), hi0 = b.rank0(cur.hi);
            int zeros = hi0 - lo0;
            if (zeros > 0) {
                heap.add(new Node(cur.level + 1, lo0, hi0, zeros, cur.prefix << 1));
            }
            int ones = (cur.hi - cur.lo) - zeros;
            if (ones > 0) {
                int oneLo = zeroCount[cur.level] + (cur.lo - lo0);
                int oneHi = zeroCount[cur.level] + (cur.hi - hi0);
                heap.add(new Node(cur.level + 1, oneLo, oneHi, ones, (cur.prefix << 1) | 1L));
            }
        }
        return result;
    }

    private static final class Node {
        final int level, lo, hi, count;
        final long prefix;
        Node(int level, int lo, int hi, int count, long prefix) {
            this.level = level; this.lo = lo; this.hi = hi; this.count = count; this.prefix = prefix;
        }
    }

    // ==================== Bit vector with O(1) rank / select ====================

    /**
     * A simple bit vector supporting rank0/rank1 in O(1) (via block-level popcount
     * precomputation) and select0/select1 in O(log n) (binary search over rank).
     * Good enough for competitive programming sized inputs (up to ~1e6-1e7).
     */
    private static final class BitVector {
        private final int n;
        private final long[] words;
        private int[] blockRank; // rank1 at the start of each 64-bit word

        BitVector(int n) {
            this.n = n;
            this.words = new long[(n >>> 6) + 1];
        }

        void set(int i) {
            words[i >>> 6] |= (1L << (i & 63));
        }

        long get(int i) {
            return (words[i >>> 6] >>> (i & 63)) & 1L;
        }

        void build() {
            blockRank = new int[words.length + 1];
            for (int i = 0; i < words.length; i++) {
                blockRank[i + 1] = blockRank[i] + Long.bitCount(words[i]);
            }
        }

        /** Number of 1-bits in [0, i). */
        int rank1(int i) {
            int wi = i >>> 6;
            int bi = i & 63;
            int r = blockRank[wi];
            if (bi != 0) {
                long mask = (bi == 64) ? -1L : (1L << bi) - 1;
                r += Long.bitCount(words[wi] & mask);
            }
            return r;
        }

        /** Number of 0-bits in [0, i). */
        int rank0(int i) {
            return i - rank1(i);
        }

        /** Position of the (k+1)-th (0-indexed) 1-bit. Assumes it exists. */
        int select1(int k) {
            // binary search on rank1
            int low = 0, high = n;
            int target = k + 1;
            while (low < high) {
                int mid = (low + high) >>> 1;
                if (rank1(mid) >= target) {
                    high = mid;
                } else {
                    low = mid + 1;
                }
            }
            return low - 1;
        }

        /** Position of the (k+1)-th (0-indexed) 0-bit. Assumes it exists. */
        int select0(int k) {
            int low = 0, high = n;
            int target = k + 1;
            while (low < high) {
                int mid = (low + high) >>> 1;
                if (rank0(mid) >= target) {
                    high = mid;
                } else {
                    low = mid + 1;
                }
            }
            return low - 1;
        }
    }
}