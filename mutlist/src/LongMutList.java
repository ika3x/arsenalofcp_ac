import java.util.*;
import java.util.stream.*;

class LongMutList implements Iterable<Long> {
    
    // ignore this
    private static final int DEFAULT_CAPACITY = 8;
    // endIgnore

    private long[] dat;
    private int size;

    public LongMutList() {
        this(DEFAULT_CAPACITY);
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
        if (size == 0) {return "";}
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(dat[i]).append(' ');
        }
        sb.deleteCharAt(sb.length()-1);

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