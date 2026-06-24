class RHF {
    private final long MOD;
    private final long BASE;
    private final IntMutList pb = new IntMutList(16);

    private static long id = 0;
    private final long this_id;

    private static class IntMutList {
        private int[] dat;
        private int size;

        public IntMutList(int capacity) {
            dat = new int[capacity];
            size = 0;
        }

        public void add(int x) {
            if (size == dat.length) {
                grow((dat.length <= 128) ? dat.length << 1 : dat.length + (dat.length >> 1));
            }
            dat[size++] = x;
        }

        public int get(int index) {
            checkIndex(index);
            return dat[index];
        }

        public int getLast() {
            return get(size-1);
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

        public void ensureCapacity(int cap) {
            int newCap = (dat.length <= 128) ? dat.length << 1 : dat.length + (dat.length >> 1);
            newCap = Math.max(newCap, cap);
            grow(cap);
        }

        private void grow(int newCap) {
            int[] newData = new int[newCap];
            System.arraycopy(dat, 0, newData, 0, size);
            dat = newData;
        }
    }

    public RHF(int mod, int base) {
        assert(1 < mod && 1 <= base && base < mod);

        this.MOD = mod;
        this.BASE = base;

        pb.add(1);

        this_id = id++;
    }
    
    public class RhStr {
        private final String str;
        private final int len;
        private long[] preh;

        /*
        * RollingHash equals:
        * r1.getRh() == r2.getRh()
        */
        @Deprecated
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof RhStr)) {return false;}
            return this.str.equals(((RhStr)obj).str); 
        }

        private RhStr(String s) {
            this.str = s;
            this.len = s.length();
            makeHashTable();
        }

        private void makeHashTable() {
            preh = new long[len + 1];
            for (int i = 0; i < len; i++) {
                preh[i+1] = (preh[i] * BASE + str.charAt(i)) % MOD;
            }
        }

        public int getRh() {
            return (int) preh[len];
        }

        public int subRhAsRev(int si, int ei) {
            int reversedStart = len - ei;
            int reversedEnd = len - si;

            return subRh(reversedStart, reversedEnd);
        }

        public int subRh(int startIndex, int endIndex) {
            if (startIndex < 0) {
                String err = "Index " + startIndex + " out of bounds for length " + len;
                throw new ArrayIndexOutOfBoundsException(err);
            } else if (endIndex > len) {
                String err = "Index " + endIndex + " out of bounds for length " + len;
                throw new ArrayIndexOutOfBoundsException(err);
            } else if (endIndex <= startIndex) {
                throw new IllegalArgumentException("Invalid range: [" + startIndex + ", "  + endIndex + ")");
            }

            int substrLen = endIndex - startIndex;
            long del = preh[startIndex] * pb.get(substrLen) % MOD;
            long res = preh[endIndex] - del; res %= MOD; if (res < 0) {res += MOD;}
            return (int) res;
        }

        public int length() {
            return len;
        }

        @Deprecated
        public String rawStr() {
            return str;
        }

        @Override
        public String toString() {
            if (toStr == null) {
                makeToStr();
            }

            return toStr;
        }

        private String toStr;
        
        private void makeToStr() {
            StringBuilder sb = new StringBuilder("RhStr(");
            sb.append(preh[len]).append(", ");
            if (len <= 13) {
                sb.append('\"');
                sb.append(str);
                sb.append('\"');
            } else {
                sb.append(str.substring(0, 5));
                sb.append("...");
                sb.append(str.substring(len - 5, len));
            }

            sb.append(", id=").append(this_id).append(')');
            toStr = sb.toString();
        }
    }

    private void growPowTable(int to) {
        pb.ensureCapacity(to + 3);
        while (to >= pb.size()) {
            long last = pb.getLast();
            last *= BASE; last %= MOD;
            pb.add((int)last);
        }
    }

    public RhStr create(String s) {
        if (s.length() + 4 > pb.size()) {
            growPowTable(s.length() + 4);
        }
        return new RhStr(s);
    }

    public RhStr createReversed(String s) {
        s = new StringBuilder(s).reverse().toString();
        return create(s);
    }

    public int concat(RhStr r1, RhStr r2) {
        long h1 = r1.getRh();
        long h2 = r2.getRh();
        return (int)((h1 * pb.get(r2.length()) % MOD + h2) % MOD);
    }

    public int concat(int s1, int s2, int s2_length) {
        return (int)((s1 * pb.get(s2_length) % MOD + s2) % MOD);
    }

    public long getPb(int index) {
        if (index >= pb.size()) {
            growPowTable(index + 3);
        }

        return pb.get(index);
    }

    @Override
    public String toString() {
        return "RHF(mod="+MOD+", base="+BASE+", id="+this_id+")";
    }
}

record Rh2Pair(int h1, int h2) implements Comparable<Rh2Pair> {
    @Override
    public final int hashCode() {
        return (h1 * 73) ^ h2;
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof Rh2Pair)) {return false;}
        Rh2Pair o = (Rh2Pair) obj;
        return this.h1 == o.h1 && this.h2 == o.h2; 
    }

    @Override
    public int compareTo(Rh2Pair o) {
        if (this.h1 != o.h1) {return Integer.compare(this.h1, o.h1);}
        else {return Integer.compare(this.h2, o.h2);}
    }
}

record Rh3Pair(int h1, int h2, int h3) implements Comparable<Rh3Pair> {
    @Override
    public final int hashCode() {
        return (h1 * 9973) + ((h2 * 97) ^ h3);
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof Rh3Pair)) {return false;}
        Rh3Pair o = (Rh3Pair) obj;
        return this.h1 == o.h1 && this.h2 == o.h2 && this.h3 == o.h3; 
    }

    @Override
    public int compareTo(Rh3Pair o) {
        if (this.h1 != o.h1) {return Integer.compare(this.h1, o.h1);}
        else if (this.h2 != o.h2) {return Integer.compare(this.h2, o.h2);}
        else {return Integer.compare(this.h3, o.h3);}
    }
}

record Rh4Pair(int h1, int h2, int h3, int h4) implements Comparable<Rh4Pair> {
    @Override
    public final int hashCode() {
        return (h1 * (929285503 ^ h4)) + ((h2 * h3) - h4);
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof Rh4Pair)) {return false;}
        Rh4Pair o = (Rh4Pair) obj;
        return this.h1 == o.h1 && this.h2 == o.h2 && this.h3 == o.h3 && this.h4 == o.h4; 
    }

    @Override
    public int compareTo(Rh4Pair o) {
        if (this.h1 != o.h1) {return Integer.compare(this.h1, o.h1);}
        else if (this.h2 != o.h2) {return Integer.compare(this.h2, o.h2);}
        else if (this.h3 != o.h3) {return Integer.compare(this.h3, o.h3);}
        else {return Integer.compare(this.h4, o.h4);}
    }
}