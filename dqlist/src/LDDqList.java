import java.util.*;

class LDDqList {

    // ignore this
    private static final int DEFAULT_CAPACITY = 8;
    // endIgnore

    private long[] dat;
    private int head;
    private int tail;
    private int mask;
    private int size;

    public LDDqList() {
        this(DEFAULT_CAPACITY);
    }

    public LDDqList(int capacity) {
        int n = 1;
        while (n < capacity) {n <<= 1;}
        dat = new long[n];
        mask = n - 1;
    }

    public void addLast(long x) {
        if (size == dat.length) grow();
        dat[tail] = x;
        tail = (tail + 1) & mask;
        size++;
    }

    public void addFirst(long x) {
        if (size == dat.length) grow();
        head = (head - 1) & mask;
        dat[head] = x;
        size++;
    }

    public long get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
        return dat[(head + index) & mask];
    }

    public long getFirst() {
        if (size == 0) throw new NoSuchElementException();
        return dat[head];
    }

    public long getLast() {
        if (size == 0) throw new NoSuchElementException();
        return dat[(tail - 1) & mask];
    }

    public long pollFirst() {
        if (size == 0) throw new NoSuchElementException();
        long res = dat[head];
        head = (head + 1) & mask;
        size--;
        return res;
    }

    public long pollLast() {
        if (size == 0) throw new NoSuchElementException();
        tail = (tail - 1) & mask;
        long res = dat[tail];
        size--;
        return res;
    }

    private void grow() {
        int oldCap = dat.length;
        long[] newDat = new long[oldCap << 1];
        int len1 = oldCap - head;
        System.arraycopy(dat, head, newDat, 0, len1);
        System.arraycopy(dat, 0, newDat, len1, head);
        
        dat = newDat;
        head = 0;
        tail = oldCap;
        mask = dat.length - 1;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
}

class DoubleDqList {

    // ignore this
    private static final int DEFAULT_CAPACITY = 8;
    // endIgnore

    private final LDDqList dql;
    
    public DoubleDqList(int capacity) {
        dql = new LDDqList(capacity);
    }

    public DoubleDqList() {
        this(DEFAULT_CAPACITY);
    }

    public void addLast(double x) {
        dql.addLast(Double.doubleToRawLongBits(x));
    }

    public void addFirst(double x) {
        dql.addFirst(Double.doubleToRawLongBits(x));
    }

    public double get(int index) {
        return Double.longBitsToDouble(dql.get(index));
    }

    public double getFirst() {
        return Double.longBitsToDouble(dql.getFirst());
    }

    public double getLast() {
        return Double.longBitsToDouble(dql.getLast());
    }

    public double pollFirst() {
        return Double.longBitsToDouble(dql.pollFirst());
    }

    public double pollLast() {
        return Double.longBitsToDouble(dql.pollLast());
    }

    public int size() { return dql.size(); }
    public boolean isEmpty() { return dql.isEmpty(); }
}