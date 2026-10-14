import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.stream.LongStream;

/*
 * XorCafe
 * ----------------------------------------------------------------
 * A linear basis over GF(2) for 64-bit (long) vectors, commonly
 * called "XOR Basis" / "Linear Basis" in competitive programming.
 *
 * Supported operations:
 *   - insert(x)          : add a vector into the basis (O(BITS))
 *   - canRepresent(x)    : check if x is in the span (O(BITS))
 *   - maxXor()           : maximum XOR value obtainable from the span
 *   - maxXorWith(x)      : maximum of (x XOR any element in the span)
 *   - minXorWith(x)      : minimum of (x XOR any element in the span)
 *   - size()             : rank of the basis (number of independent vectors)
 *   - kthXor(k)          : k-th smallest XOR value in the span (1-indexed)
 *                          (requires buildOrtho() to be called first)
 *   - merge(other)       : merge another basis into this one
 *   - copy()             : deep copy of this basis
 *
 * Typical usage:
 *   XorBasis basis = new XorBasis(60); // enough bits for values < 2^60
 *   for (long v : arr) basis.insert(v);
 *   long ans = basis.maxXor();
 *
 * Notes:
 *   - BITS should be set to cover the maximum bit length of inputs.
 *     For values up to 1e18, 60 bits is enough. For values up to
 *     2^63 - 1, use 63 (avoid 64 due to sign issues with long).
 *   - All values are treated as non-negative in [0, 2^BITS).
 * ----------------------------------------------------------------
 */

public class XorCafe {
    // basis[i] : a vector whose highest set bit is exactly i (or 0 if unused)
    private final long[] basis;
    private final int BITS;
    private int rank; // number of independent vectors currently held
    private boolean orthoBuilt; // whether basis has been reduced for kthXor

    public XorCafe(int bits) {
        this.BITS = bits;
        this.basis = new long[bits];
        this.rank = 0;
        this.orthoBuilt = false;
    }

    // Insert x into the basis. Returns true if x increased the rank
    // (i.e. x was linearly independent from the current basis).
    public boolean insert(long x) {
        orthoBuilt = false; // basis changed, ortho form is stale
        for (int i = BITS - 1; i >= 0; i--) {
            if (((x >> i) & 1L) == 0) continue;
            if (basis[i] == 0) {
                basis[i] = x;
                rank++;
                return true;
            }
            x ^= basis[i];
        }
        return false; // x was fully reduced to 0, already in span
    }

    // Returns true if x can be represented as XOR of some subset
    // of the inserted vectors (i.e. x is in the span).
    public boolean canRepresent(long x) {
        for (int i = BITS - 1; i >= 0; i--) {
            if (((x >> i) & 1L) == 0) continue;
            if (basis[i] == 0) return false;
            x ^= basis[i];
        }
        return true;
    }

    // Maximum XOR value achievable by XORing any subset of inserted vectors.
    public long maxXor() {
        return maxXorWith(0L);
    }

    // Maximum of (x XOR v) over all v in the span (including v = 0).
    public long maxXorWith(long x) {
        long res = x;
        for (int i = BITS - 1; i >= 0; i--) {
            if (basis[i] == 0) continue;
            if (((res >> i) & 1L) == 0) {
                res ^= basis[i];
            }
        }
        return res;
    }

    // Minimum of (x XOR v) over all v in the span (including v = 0).
    public long minXorWith(long x) {
        long res = x;
        for (int i = BITS - 1; i >= 0; i--) {
            if (basis[i] == 0) continue;
            if (((res >> i) & 1L) != 0) {
                res ^= basis[i];
            }
        }
        return res;
    }

    // Number of independent vectors in the basis (rank).
    public int size() {
        return rank;
    }

    // Number of distinct values representable by the span (2^rank).
    public long spanSize() {
        return 1L << rank;
    }

    // Reduces the basis so that each basis[i] has a unique highest bit
    // AND that bit does not appear in any other basis vector.
    // Required before calling kthXor.
    private void buildOrtho() {
        // Reduce: for every pair, clear overlapping lower bits so that
        // basis vectors become "independent" in a strong sense, which
        // lets us treat rank as independent binary digits for kth-XOR.
        for (int i = 0; i < BITS; i++) {
            if (basis[i] == 0) continue;
            for (int j = i + 1; j < BITS; j++) {
                if (basis[j] == 0) continue;
                if (((basis[j] >> i) & 1L) != 0) {
                    basis[j] ^= basis[i];
                }
            }
        }
        orthoBuilt = true;
    }

    // Returns the k-th smallest value (1-indexed) representable by
    // XORing any subset of the basis vectors, including 0 as the 1st
    // element if includeZero is handled naturally by k=1 -> 0 when
    // the basis is not full rank versus BITS (subset XORs always
    // include the empty subset = 0).
    // Throws IllegalArgumentException if k is out of range [1, spanSize()].
    public long kthXor(long k) {
        if (!orthoBuilt) buildOrtho();
        long span = spanSize();
        if (k < 1 || k > span) {
            throw new IllegalArgumentException("k out of range: " + k + " (span size = " + span + ")");
        }
        // Collect the non-zero basis vectors in increasing bit order.
        // Each acts as one independent binary digit of k-1.
        long[] independent = new long[rank];
        int idx = 0;
        for (int i = 0; i < BITS; i++) {
            if (basis[i] != 0) independent[idx++] = basis[i];
        }
        long kk = k - 1; // 0-indexed
        long res = 0;
        for (int i = 0; i < rank; i++) {
            if (((kk >> i) & 1L) != 0) {
                res ^= independent[i];
            }
        }
        return res;
    }

    // Merge another XorBasis into this one (union of spans).
    public void merge(XorCafe other) {
        for (int i = other.BITS - 1; i >= 0; i--) {
            if (other.basis[i] != 0) {
                insert(other.basis[i]);
            }
        }
    }

    // Deep copy of this basis.
    public XorCafe copy() {
        XorCafe c = new XorCafe(BITS);
        System.arraycopy(this.basis, 0, c.basis, 0, BITS);
        c.rank = this.rank;
        c.orthoBuilt = false;
        return c;
    }

    // Returns the raw basis vectors currently stored (non-zero entries
    // only), useful for debugging or custom processing.
    public long[] vectors() {
        LongMutList list = new LongMutList();
        for (long v : basis) {
            if (v != 0) list.add(v);
        }
        return list.toArray();
    }

    @Override
    public String toString() {
        return "XorBasis{rank=" + rank + ", vectors=" + Arrays.toString(vectors()) + "}";
    }

    private static class LongMutList implements Iterable<Long> {
        private long[] dat;
        private int size;
    
        private LongMutList() {
            this(8);
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
            newCap = Math.max(newCap, 8);
            long[] newData = new long[newCap];
            System.arraycopy(dat, 0, newData, 0, size);
            dat = newData;
        }
    
        public long[] toArray() {
            long[] res = new long[size];
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