class PowTower {
    /**
     * a[0]^a[1]^...^a[n-1] mod m
     */
    public static long powTower(long[] a, int mod) {
        if (a.length == 0) return 1 % mod;
        else if (a.length == 1) return a[0] % mod;
        else if (a.length == 2) return modPow(a[0] % mod, a[1], mod);
        long[] chain = buildPhiChain(mod);
        long[] res = solve(a, 0, chain, 0);
        return res[0] % mod;
    }

    // builds [m, phi(m), phi(phi(m)), ... , 1] 
    private static long[] buildPhiChain(int m) {
        List<Long> list = new ArrayList<>();
        long cur = m;
        list.add(cur);
        while (cur != 1) {
            cur = phi(cur);
            list.add(cur);
        }
        long[] chain = new long[list.size()];
        for (int i = 0; i < list.size(); i++) chain[i] = list.get(i);
        return chain;
    }

    private static long[] solve(long[] a, int idx, long[] chain, int depth) {
        long mod = chain[Math.min(depth, chain.length - 1)];
        
        if (idx == a.length - 1) {
            long val = a[idx];
            long modded = ((val % mod) + mod) % mod;
            return new long[]{modded, val >= mod ? 1 : 0};
        } else if (mod == 1) {
            return new long[]{0, 1};
        }

        long[] sub = solve(a, idx + 1, chain, depth + 1);
        long subMod = chain[Math.min(depth + 1, chain.length - 1)];
        long exponent = sub[0] + (sub[1] == 1 ? subMod : 0);

        long base = a[idx];
        long baseMod = ((base % mod) + mod) % mod;
        long resVal = modPow(baseMod, exponent, mod);

        boolean over = isOverflow(base, exponent, mod);

        return new long[]{resVal, over ? 1 : 0};
    }

    // base^exponent >= mod ?
    private static boolean isOverflow(long base, long exponent, long mod) {
        if (mod <= 1) return true;
        if (base == 0) return false;
        if (base == 1) return false;
        if (exponent == 0) return false; 
        // base >= 2, exponent >= 1
        long result = 1;
        for (long e = 0; e < exponent; e++) {
            result *= base;
            if (result >= mod) return true;
        }
        return result >= mod;
    }

    private static long modPow(long base, long exp, long mod) {
        if (mod == 1) return 0;
        base %= mod;
        if (base < 0) base += mod;
        long result = 1 % mod;
        while (exp > 0) {
            if ((exp & 1) == 1) result = result * base % mod;
            base = base * base % mod;
            exp >>= 1;
        }
        return result;
    }

    private static long phi(long n) {
        long result = n;
        for (long p = 2; p * p <= n; p++) {
            if (n % p == 0) {
                while (n % p == 0) n /= p;
                result -= result / p;
            }
        }
        if (n > 1) result -= result / n;
        return result;
    }
}