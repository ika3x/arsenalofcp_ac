import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.LongStream;

public class PrimeCafe {
    private static final int[] PRIMES_UNDER_50 = {
        2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47
    };

    private static final long[] MILLER_RABIN_BASES = {2, 325, 9375, 28178, 450775, 9780504, 1795265022};

    private static class LongMutList implements Iterable<Long> {
        private static final int DEFAULT_CAPACITY = 8;

        private long[] dat;
        private int size;

        private LongMutList() {
            this(DEFAULT_CAPACITY);
        }
        
        private LongMutList(int capacity) {
            dat = new long[capacity];
            size = 0;
        }

        private LongMutList(long[] in) {
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

    private static class BitSet2 implements Cloneable {
        private final long[] dat;
        private final int size;
        
        private BitSet2(int n) {
            this.size = n;
            this.dat = new long[(size+63) >> 6];
        }
    
        public int size() {
            return size;
        }
    
        public void to1(int index) {
            int bk = index >> 6;
            int at = index & 63;
            dat[bk] |= 1L << at;
        }
    
        public void to0(int index) {
            int bk = index >> 6;
            int at = index & 63;
            dat[bk] &= ~(1L << at);
        }
    
        public boolean is1(int index) {
            int bk = index >> 6;
            int at = index & 63;
            return (dat[bk] & (1L << at)) != 0;
        }

    
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("BitSet2(");
            for (int i = 0; i < size; i++) {
                sb.append(is1(i) ? '1' : '0');
            }
    
            sb.append(')');
            return sb.toString();
        }
    }

    private PrimeCafe() {}

    // Computes (a * b) mod M using repeated reduction of the high 64 bits.
    // Let R = 2^64 mod M.
    // Since a*b = hi*2^64 + lo,
    // repeatedly replace hi*2^64 by hi*R until hi becomes zero.
    private static long mulMod(long a, long b, long M) {
        a %= M; b %= M;
        if (a < 0) a += M; if (b < 0) b += M;
        if (a == 0 || b == 0) return 0;

        long hi = Math.unsignedMultiplyHigh(a, b);
        long lo = a * b;

        if (hi == 0) { return Long.remainderUnsigned(lo, M); }

        long R = Long.remainderUnsigned(-M, M);
        long res = Long.remainderUnsigned(lo, M);
        
        while (hi != 0) {
            long p_hi = Math.unsignedMultiplyHigh(hi, R);
            long p_lo = hi * R;

            res = addMod(res, Long.remainderUnsigned(p_lo, M), M);
            hi = p_hi;
        }

        return res;
    }

    private static long powerMod(long base, long exp, long mod) {
        long res = 1;
        base %= mod;

        while (exp > 0) {
            if ((exp & 1) == 1) { res = mulMod(res, base, mod); }
            base = mulMod(base, base, mod);
            exp >>= 1;
        }

        return res;
    }

    private static long addMod(long a, long b, long M) {
        long res = a + b;
        if (Long.compareUnsigned(res, a) < 0 || Long.compareUnsigned(res, M) >= 0) {
            res -= M;
        }
        return res;
    }

    public static boolean millerRabinIsPrime(long n) {
        // pretest
        if (n < 2) return false;
        if ((n & 1) == 0) return n == 2;
        for (long x : PRIMES_UNDER_50) {
            if (n % x == 0) { return n == x; }
        }

        long d = n - 1;
        int s = Long.numberOfTrailingZeros(d);
        d >>= s;

        for (long a : MILLER_RABIN_BASES) {
            if (a % n == 0) { continue; }
            else if (!checkComposite(n, a, d, s)) { return false; }
        }

        return true;
    }


    private static boolean checkComposite(long n, long a, long d, int s) {
        long x = powerMod(a % n, d, n);
        if (x == 1 || x == n - 1) { return true; }
        for (int r = 1; r < s; r++) {
            if ((x = mulMod(x, x, n)) == n - 1) { return true; }
        }
        return false;
    }

    private static long gcd(long a, long b) {
        while (b != 0) { long t = a % b; a = b; b = t; }
        return Math.abs(a);
    }
    
    public static long pollardRho(long n) {
        if ((n & 1) == 0) return 2;
        if (n % 3 == 0) return 3;
        if (n % 5 == 0) return 5;

        long y = ThreadLocalRandom.current().nextLong(1, n);
        long c = ThreadLocalRandom.current().nextLong(1, n);
        long m = 128;
        long g = 1;
        long r = 1;
        long q = 1;
        long x = 0;
        long ys = 0;

        while (g == 1) {
            x = y;
            for (int i = 0; i < r; i++) {
                y = (mulMod(y, y, n) + c) % n;
            }

            long k = 0;
            while (k < r && g == 1) {
                ys = y;
                long limit = Math.min(m, r - k);
                for (int i = 0; i < limit; i++) {
                    y = (mulMod(y, y, n) + c) % n;
                    long diff = (x >= y) ? (x - y) : (y - x);
                    q = mulMod(q, diff, n);
                }
                g = gcd(q, n);
                k += limit;
            }
            r <<= 1;
        }

        if (g == n) {
            do {
                ys = (mulMod(ys, ys, n) + c) % n;
                g = gcd(Math.abs(x - ys), n);
            } while (g == 1);
        }

        return (g == n) ? pollardRho(n) : g;
    }

    /**
     * factorize n -> factors long[]
     */
    public static long[] factorizeToArray(long n) {
        if (n == 1) return new long[0];

        LongMutList factors = new LongMutList();
        if (n <= 100_000) {
            int n2 = (int)n;
            for (int i = 2; i*i <= n; i++) {
                while (n2 % i == 0) {
                    factors.add(i);
                    n2 /= i;
                }
            }
            if (n2 != 1) {
                factors.add(n2);
            }
        } else {
            factorizeRecursive(n, factors);
        }
        
        factors.sort();
        return factors.toArray();
    }

    public static LinkedHashMap<Long, Integer> factorizeToLinkedHashMap(long n) {
        long[] arr = factorizeToArray(n);
        LinkedHashMap<Long, Integer> h = new LinkedHashMap<>();
        for (long v : arr) {
            h.merge(v, 1, Integer::sum);
        }
        return h;
    }

    private static void factorizeRecursive(long n, LongMutList factors) {
        if (n <= 1) return;
        
        if (millerRabinIsPrime(n)) {
            factors.add(n);
            return;
        }

        long d = pollardRho(n);
        factorizeRecursive(d, factors);
        factorizeRecursive(n / d, factors);
    }

    public static long[] divisors(long n) {
        if (n <= 0) return new long[0];
        
        LongMutList divs = new LongMutList();

        if (n <= 100_000L) {
            for (long i = 1; i * i <= n; i++) {
                if (n % i == 0) {
                    divs.add(i);
                    if (i * i != n) {
                        divs.add(n / i);
                    }
                }
            }
        } else {
            LinkedHashMap<Long, Integer> primeFactors = factorizeToLinkedHashMap(n);
            
            List<Map.Entry<Long, Integer>> entries = new ArrayList<>(primeFactors.entrySet());
            generateDivisorsDfs(0, 1L, entries, divs);
        }

        divs.sort();
        return divs.toArray();
    }

    private static void generateDivisorsDfs(int index, long current, List<Map.Entry<Long, Integer>> entries, LongMutList divs) {
        if (index == entries.size()) {
            divs.add(current);
            return;
        }

        Map.Entry<Long, Integer> entry = entries.get(index);
        long p = entry.getKey();
        int count = entry.getValue();

        long pow = 1;
        for (int i = 0; i <= count; i++) {
            generateDivisorsDfs(index + 1, current * pow, entries, divs);
            pow *= p;
        }
    }

    public static long[] sieveToArray(int N) {
        BitSet2 isComposite = sieveToBitSet2(N);
        LongMutList primes = new LongMutList();
        for (int i = 2; i <= N; i++) {
            if (!isComposite.is1(i)) { primes.add(i); }
        }
        return primes.toArray();
    }

    // NOTE: returns IS"COMPOSITE", not IS"PRIME"
    public static BitSet2 sieveToBitSet2(int N) {
        BitSet2 isComposite = new BitSet2(N + 1);
        
        if (N >= 0) isComposite.to1(0);
        if (N >= 1) isComposite.to1(1);

        for (int p = 2; (long) p * p <= N; p++) {
            if (!isComposite.is1(p)) { 
                for (int i = p * p; i <= N; i += p) {
                    isComposite.to1(i);
                }
            }
        }

        return isComposite;
    }

    // Warning: Closed Interval
    public static long[] segmentedSieveToArray(long L, long R) {
        BitSet2 isCompositeSegment = segmentedSieveToBitSet2(L, R);
        LongMutList primes = new LongMutList();
        for (long i = L; i <= R; i++) {
            if (!isCompositeSegment.is1((int) (i - L))) { primes.add(i); }
        }
        return primes.toArray();
    }

    // Warning: Closed Interval
    public static BitSet2 segmentedSieveToBitSet2(long L, long R) {
        int length = (int) (R - L + 1);
        BitSet2 isCompositeSegment = new BitSet2(length);

        if (L <= 1) {
            for (long i = L; i <= Math.min(1L, R); i++) {
                isCompositeSegment.to1((int) (i - L));
            }
        }

        int sqrtR = (int) Math.sqrt(R);
        BitSet2 smallSieve = sieveToBitSet2(sqrtR);

        for (int p = 2; p <= sqrtR; p++) {
            if (!smallSieve.is1(p)) { 
                
                long start = ((L + p - 1) / p) * p;
                
                if (start == p) {
                    start += p;
                }

                for (long j = start; j <= R; j += p) {
                    isCompositeSegment.to1((int) (j - L));
                }
            }
        }

        return isCompositeSegment;
    }

    public static class LpfMachine {
        private final int[] lpf;

        public LpfMachine(int maxN) {
            lpf = new int[maxN + 1];
            
            for (int i = 0; i <= maxN; i++) {
                lpf[i] = i;
            }
            
            for (int p = 2; p * p <= maxN; p++) {
                if (lpf[p] == p) {
                    for (int i = p * p; i <= maxN; i += p) {
                        if (lpf[i] == i) {
                            lpf[i] = p;
                        }
                    }
                }
            }
        }

        public long[] factorizeToArray(int n) {
            if (n < 2) { return new long[0]; }
            LongMutList factors = new LongMutList();
            while (n > 1) {
                int p = lpf[n];
                factors.add(p);
                n /= p;
            }
            return factors.toArray();
        }

        public LinkedHashMap<Integer, Integer> factorizeToLinkedHashMap(int n) {
            if (n < 2) return new LinkedHashMap<>();
            
            LinkedHashMap<Integer, Integer> factors = new LinkedHashMap<>();
            while (n > 1) {
                int p = lpf[n];
                factors.merge(p, 1, Integer::sum);
                n /= p;
            }
            return factors;
        }

        public boolean isPrime(int n) {
            if (n < 2) return false;
            return lpf[n] == n;
        }
    }
}
