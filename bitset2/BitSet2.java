class BitSet2 implements Cloneable {
    private final long[] dat;
    private final int size;
    
    public BitSet2(int n) {
        this.size = n;
        this.dat = new long[(size+63) >> 6];
    }

    private BitSet2(int size, long[] dat) {
        this.size = size;
        this.dat = dat.clone();    
    }

    public int size() {
        return size;
    }

    public void inverse(int index) {
        int bk = index >> 6;
        int at = index & 63;
        dat[bk] ^= 1L << at;
    }

    public void flip(int index) {
        inverse(index);
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

    public void rangedTo1(int from, int to) {
        if (from < 0 || to > size || from > to) {
            throw new IllegalArgumentException("Invalid range: [" + from + ", " + to + ")");
        } else if (from == to) {return;}
        
        int startBk = from >> 6;
        int endBk = (to - 1) >> 6;

        if (startBk == endBk) {
            long mask = ((-1L >>> (63 - (to - 1 - (startBk << 6)))) & (-1L << (from - (startBk << 6))));
            dat[startBk] |= mask;
        } else {
            dat[startBk] |= (-1L << (from & 63));
            for (int i = startBk + 1; i < endBk; i++) {
                dat[i] = -1L;
            }
            dat[endBk] |= (-1L >>> (63 - ((to - 1) & 63)));
        }
    }

    public void rangedTo0(int from, int to) {
        if (from < 0 || to > size || from > to) {
            throw new IllegalArgumentException("Invalid range: [" + from + ", " + to + ")");
        } else if (from == to) {return;}

        int startBk = from >> 6;
        int endBk = (to - 1) >> 6;

        if (startBk == endBk) {
            long mask = ((-1L >>> (63 - (to - 1 - (startBk << 6)))) & (-1L << (from - (startBk << 6))));
            dat[startBk] &= ~mask;
        } else {
            dat[startBk] &= ~(-1L << (from & 63));
            for (int i = startBk + 1; i < endBk; i++) {
                dat[i] = 0L;
            }
            dat[endBk] &= ~(-1L >>> (63 - ((to - 1) & 63)));
        }
    }

    public boolean is1(int index) {
        int bk = index >> 6;
        int at = index & 63;
        return (dat[bk] & (1L << at)) != 0;
    }

    @Override
    public BitSet2 clone() {
        return new BitSet2(this.size, this.dat);
    }

    public BitSet2 or(BitSet2 o) {
        BitSet2 a = this, b = o;
        if (a.size() < b.size()) {var tmp = b; b = a; a = tmp;}
        long[] res = a.dat.clone();
        for (int i = 0; i < b.dat.length; i++) {
            res[i] |= b.dat[i];
        }

        return new BitSet2(a.size(), res);
    }

    public void orAsg(BitSet2 o) {
        for (int i = 0; i < o.dat.length; i++) {
            this.dat[i] |= o.dat[i];
        }
    }

    public BitSet2 and(BitSet2 o) {
        long[] res = new long[this.dat.length];
        int common = Math.min(this.dat.length, o.dat.length);
        
        for (int i = 0; i < common; i++) {
            res[i] = this.dat[i] & o.dat[i];
        }

        return new BitSet2(this.size, res);
    }

    public void andAsg(BitSet2 o) {
        int common = Math.min(this.dat.length, o.dat.length);
        for (int i = 0; i < common; i++) {
            this.dat[i] &= o.dat[i];
        }

        for (int i = common; i < this.dat.length; i++) {
            this.dat[i] = 0L;
        }
    }

    public BitSet2 xor(BitSet2 o) {
        BitSet2 a = this, b = o;
        if (a.size() < b.size()) { var tmp = b; b = a; a = tmp; }
        long[] res = a.dat.clone();
        for (int i = 0; i < b.dat.length; i++) res[i] ^= b.dat[i];
        return new BitSet2(a.size(), res);
    }

    public void xorAsg(BitSet2 o) {
        for (int i = 0; i < o.dat.length; i++) this.dat[i] ^= o.dat[i];
    }
    
    public BitSet2 nand(BitSet2 o) {
        long[] res = new long[this.dat.length];
        int common = Math.min(this.dat.length, o.dat.length);
        for (int i = 0; i < common; i++) res[i] = ~(this.dat[i] & o.dat[i]);
        for (int i = common; i < this.dat.length; i++) res[i] = -1L;
        return new BitSet2(this.size, res);
    }

    public void nandAsg(BitSet2 o) {
        int common = Math.min(this.dat.length, o.dat.length);
        for (int i = 0; i < common; i++) this.dat[i] = ~(this.dat[i] & o.dat[i]);
        for (int i = common; i < this.dat.length; i++) this.dat[i] = -1L;
    }

    public BitSet2 nor(BitSet2 o) {
        BitSet2 a = this, b = o;
        if (a.size() < b.size()) { var tmp = b; b = a; a = tmp; }
        long[] res = a.dat.clone();
        int common = b.dat.length;
        for (int i = 0; i < common; i++) res[i] = ~(a.dat[i] | b.dat[i]);
        for (int i = common; i < a.dat.length; i++) res[i] = ~a.dat[i];
        return new BitSet2(a.size(), res);
    }

    public void norAsg(BitSet2 o) {
        int common = Math.min(this.dat.length, o.dat.length);
        for (int i = 0; i < common; i++) this.dat[i] = ~(this.dat[i] | o.dat[i]);
        for (int i = common; i < this.dat.length; i++) this.dat[i] = ~this.dat[i];
    }

    public int popcount() {
        int res = 0;
        for (long v : this.dat) {
            res += Long.bitCount(v);
        }

        return res;
    }

    public int getAny1Index() {
        for (int i = 0; i < dat.length; i++) {
            if (dat[i] != 0) {
                return (i << 6) | Long.numberOfTrailingZeros(dat[i]);
            }
        }
        throw new IllegalStateException("No 1s here.");
    }

    public int getAnyBoth1(BitSet2 o) {
        int end = Math.min(this.dat.length, o.dat.length);
        for (int i = 0; i < end; i++) {
            long and = this.dat[i] & o.dat[i];
            if (and != 0) {
                return (i << 6) | Long.numberOfTrailingZeros(and);
            }
        }

        return -1;
    }

    public void forEach1(IntConsumer action) {
        for (int i = 0; i < dat.length; i++) {
            long v = dat[i];
            while (v != 0) {
                int tz = Long.numberOfTrailingZeros(v);
                action.accept((i << 6) | tz);
                v &= v - 1L;
            }
        }
    }

    public void forEach0(IntConsumer action) {
        for (int i = 0; i < dat.length; i++) {
            long v = ~dat[i];
            int base = i << 6;
            while (v != 0) {
                int tz = Long.numberOfTrailingZeros(v);
                int idx = base | tz;
                if (idx >= size) break;
                action.accept(idx);
                v &= v - 1L;
            }
        }
    }

    public int next0(int from) {
        int bk = from >> 6;
        if (bk >= dat.length) return -1;
        long v = dat[bk] & (-1L << (from & 63));
        if (v != 0) return (bk << 6) | Long.numberOfTrailingZeros(v);
        for (int i = bk + 1; i < dat.length; i++) {
            if (dat[i] != 0) return (i << 6) | Long.numberOfTrailingZeros(dat[i]);
        }
        return -1;
    }

    public int next1(int from) {
        int bk = from >> 6;
        if (bk >= dat.length) return from < size ? from : -1;
        long v = ~dat[bk] & (-1L << (from & 63));
        if (v != 0) {
            int idx = (bk << 6) | Long.numberOfTrailingZeros(v);
            return idx < size ? idx : -1;
        }
        for (int i = bk + 1; i < dat.length; i++) {
            if (~dat[i] != 0) {
                int idx = (i << 6) | Long.numberOfTrailingZeros(~dat[i]);
                return idx < size ? idx : -1;
            }
        }
        return -1;
    }

    public long _directGet(int blockIndex) {
        return dat[blockIndex];
    }

    public void _directSet(int blockIndex, long value) {
        dat[blockIndex] = value;
    }
    
    public long[] _rawArray() {
        return dat;
    }

    public void removeOverIndex() {
        int lastIdx = dat.length - 1;
        int unusedBits = (64 - (size & 63)) & 63;
        if (unusedBits != 0) {
            dat[lastIdx] &= (-1L >>> unusedBits);
        }
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