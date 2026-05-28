import java.util.*;
import java.util.stream.*;

class DoubleMutList implements Iterable<Double> {

    // ignore this
    private static final int DEFAULT_CAPACITY = 8;
    // endIgnore

    private double[] dat;
    private int size;

    public DoubleMutList() {
        this(DEFAULT_CAPACITY);
    }
    
    public DoubleMutList(int capacity) {
        dat = new double[capacity];
        size = 0;
    }

    public DoubleMutList(double[] in) {
        size = in.length;
        dat = new double[size];
        System.arraycopy(in, 0, dat, 0, size);
    }

    public void add(double x) {
        if (size == dat.length) {
            grow();
        }
        dat[size++] = x;
    }

    public void set(int index, double value) {
        checkIndex(index);
        dat[index] = value;
    }

    public double get(int index) {
        checkIndex(index);
        return dat[index];
    }

    public double getFirst() {
        return get(0);
    }
    
    public double getLast() {
        return get(size-1);
    }

    public double pollLast() {
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

    public int binarySearch(double target) {
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

    public double size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void grow() {
        int newCap = (dat.length <= 128) ? dat.length << 1 : dat.length + (dat.length >> 1);
        newCap = Math.max(newCap, DEFAULT_CAPACITY);
        double[] newData = new double[newCap];
        System.arraycopy(dat, 0, newData, 0, size);
        dat = newData;
    }

    public double[] toArray() {
        double[] res = new double[size];
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
    public PrimitiveIterator.OfDouble iterator() {
        return new PrimitiveIterator.OfDouble() {
            private int cursor = 0;
            @Override
            public boolean hasNext() { return cursor < size; }
            @Override
            public double nextDouble() {
                if (cursor >= size) throw new NoSuchElementException();
                return dat[cursor++];
            }
        };
    }

    public DoubleStream stream() {
        return Arrays.stream(dat, 0, size);
    }
}