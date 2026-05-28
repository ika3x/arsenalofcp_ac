class LpIvTuplePQ {
    private static final int DEFAULT_CAPACITY = 8;

    private int size;
    private long[] priorities;
    private int[] values;

    public LpIvTuplePQ() {
        this(DEFAULT_CAPACITY);
    }

    public LpIvTuplePQ(int capacity) {
        this.priorities = new long[capacity + 1];
        this.values = new int[capacity + 1];
        this.size = 0;
    }

    public void add(long p, int v) {
        if (size >= priorities.length-1) {
            grow();
        }

        size++;
        int i = size;
        while (i > 1) {
            int parent = i >> 1;
            if (priorities[parent] <= p) break;
            priorities[i] = priorities[parent];
            values[i] = values[parent];
            i = parent;
        }
        priorities[i] = p;
        values[i] = v;
    }

    public void removeRoot() {
        if (size == 0) throw new java.util.NoSuchElementException();
        long p = priorities[size];
        int v = values[size];
        size--;

        if (size > 0) {
            int i = 1;
            while ((i << 1) <= size) {
                int child = i << 1;
                if (child + 1 <= size && priorities[child + 1] < priorities[child]) child++;
                if (priorities[child] >= p) break;
                priorities[i] = priorities[child];
                values[i] = values[child];
                i = child;
            }
            priorities[i] = p;
            values[i] = v;
        }
    }

    
    public long peekPriority() {
        if (size == 0) throw new java.util.NoSuchElementException();
        return priorities[1];
    }

    public int peekValue() {
        if (size == 0) throw new java.util.NoSuchElementException();
        return values[1];
    }

    private void grow() {
        int newCapacity = priorities.length << 1;
        priorities = Arrays.copyOf(priorities, newCapacity);
        values = Arrays.copyOf(values, newCapacity);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}