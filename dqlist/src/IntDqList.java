import java.util.*;

class IntDqList {

    // ignore this
    private static final int DEFAULT_CAPACITY = 8;
    // endIgnore

    private int[] dat;
    private int head;
    private int tail;
    private int mask;
    private int size;

    public IntDqList() {
        this(DEFAULT_CAPACITY);
    }

    public IntDqList(int capacity) {
        int n = 1;
        while (n < capacity) {n <<= 1;}
        dat = new int[n];
        mask = n - 1;
    }

    public void addLast(int x) {
        if (size == dat.length) grow();
        dat[tail] = x;
        tail = (tail + 1) & mask;
        size++;
    }

    public void addFirst(int x) {
        if (size == dat.length) grow();
        head = (head - 1) & mask;
        dat[head] = x;
        size++;
    }

    public int pollFirst() {
        if (size == 0) throw new NoSuchElementException();
        int res = dat[head];
        head = (head + 1) & mask;
        size--;
        return res;
    }

    public int pollLast() {
        if (size == 0) throw new NoSuchElementException();
        tail = (tail - 1) & mask;
        int res = dat[tail];
        size--;
        return res;
    }

    public int get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
        return dat[(head + index) & mask];
    }

    public int getFirst() {
        if (size == 0) throw new NoSuchElementException();
        return dat[head];
    }

    public int getLast() {
        if (size == 0) throw new NoSuchElementException();
        return dat[(tail - 1) & mask];
    }

    private void grow() {
        int oldCap = dat.length;
        int[] newDat = new int[oldCap << 1];
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