import java.util.*;
import java.util.stream.IntStream;

class TreeMaster {
    private static final int DEFAULT_CAPACITY = 8;

    private static class IntMutList implements Iterable<Integer> {
        private int[] dat;
        private int size;
    
        private IntMutList() {
            this(DEFAULT_CAPACITY);
        }
        
        private IntMutList(int capacity) {
            dat = new int[capacity];
            size = 0;
        }
    
        private IntMutList(int[] in) {
            size = in.length;
            dat = new int[size];
            System.arraycopy(in, 0, dat, 0, size);
        }
    
        public void add(int x) {
            if (size == dat.length) {
                grow();
            }
            dat[size++] = x;
        }
    
        public void set(int index, int value) {
            checkIndex(index);
            dat[index] = value;
        }
    
        public int get(int index) {
            checkIndex(index);
            return dat[index];
        }
    
        public int getFirst() {
            return get(0);
        }
        
        public int getLast() {
            return get(size-1);
        }
    
        public int pollLast() {
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
    
        public int binarySearch(int target) {
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
            int[] newData = new int[newCap];
            System.arraycopy(dat, 0, newData, 0, size);
            dat = newData;
        }
    
        public int[] toArray() {
            int[] res = new int[size];
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
            StringBuilder sb = new StringBuilder(Integer.toString(dat[0]));
            for (int i = 1; i < size; i++) {
                sb.append(' ').append(dat[i]);
            }
    
            return sb.toString();
        }
    
        @Override
        public PrimitiveIterator.OfInt iterator() {
            return new PrimitiveIterator.OfInt() {
                private int cursor = 0;
                @Override
                public boolean hasNext() { return cursor < size; }
                @Override
                public int nextInt() {
                    if (cursor >= size) throw new NoSuchElementException();
                    return dat[cursor++];
                }
            };
        }
    
        public IntStream stream() {
            return Arrays.stream(dat, 0, size);
        }
    }
    

    private int[][] parent;
    private final int[] p1;
    private final int[] depArr;
    private final int K, V, R;

    private final IntMutList[] G;
    private int[][] g2;
    
    public TreeMaster(int n, int root) {
        this.V = n;
        int K = 1;
        this.G = new IntMutList[V];
        for (int i = 0; i < V; i++) { G[i] = new IntMutList(4); }
        this.R = root;
        
        while ((1 << K) < V) K++;
        this.K = K;
        p1 = new int[V];
        Arrays.fill(p1, -1);
        depArr = new int[V];
    }

    public void addEdge(int u, int v) { G[u].add(v); G[v].add(u); }

    // assert: the graph is an tree
    public int[][] buildMain() {
        

        g2 = new int[V][];
        for (int i = 0; i < V; i++) {
            g2[i] = G[i].toArray();
        }

        dfs();

        return g2;
    }

    // assert: buildMain() called
    public void buildLca() {
        parent = new int[K][V];
        for (int[] p : parent) {Arrays.fill(p, -1);}
        parent[0] = p1;

        for (int k = 0; k+1 < K; k++) {
            for (int v = 0; v < V; v++) {
                if (parent[k][v] < 0) {
                    parent[k+1][v] = -1;
                } else {
                    parent[k+1][v] = parent[k][parent[k][v]];
                }
            }
        }
    }

    private void dfs() {
        Arrays.fill(depArr, -1);

        IntMutList stack = new IntMutList();
        p1[R] = -1;
        depArr[R] = 0;

        stack.add(R);

        while (!stack.isEmpty()) {
            int c = stack.pollLast();
            for (int k : g2[c]) {
                if (depArr[k] != -1) { continue; }
                
                depArr[k] = depArr[c] + 1;
                p1[k] = c;

                stack.add(k);
            }
        }
    }

    public int lca(int u, int v) {
        if (depArr[u] < depArr[v]) {
            var tmp = u; u = v; v = tmp;
        }
        int K = parent.length;

        for (int k = 0; k < K; k++) {
            if (((depArr[u] - depArr[v]) >> k & 1) != 0) {
                u = parent[k][u];
            }
        }

        if (u == v) return u;
        for (int k = K - 1; k >= 0; k--) {
            if (parent[k][u] != parent[k][v]) {
                u = parent[k][u];
                v = parent[k][v];
            }
        }
        return parent[0][u];
    }

    // 2^k par of v
    public int directParentArray(int v, int k) {
        return parent[k][v];
    }

    public int parent(final int v) {
        return p1[v];
    }

    public int parent(final int v, final int k) {
        if (k == 1) { return parent(v); }

        int b = k;
        int x = v;
        while (b != 0) {
            int t0 = Integer.numberOfTrailingZeros(b);
            x = parent[t0][x];
            if (x == -1) { return -1; }

            b ^= 1 << t0;
        }

        return x;
    }

    public int distance(int u, int v) {
        int l = lca(u, v);
        return depArr[u] + depArr[v] - 2 * depArr[l];
    }

    public int depth(int v) {
        return depArr[v];
    }

    // returns: {int[] et, int[] back}
    // assert: buildMain() called
    public int[][] eulerTour() {
        IntMutList stack = new IntMutList();
        int[] et = new int[2 * V];
        int[] back = new int[2 * V];

        int index = 0;

        stack.add(R);
        /*
            et[index] = at;
            back[at] = index++;
            for (int v : g[at]) {
                if (v != par) dfs(v);
            }
            et[index] = at;
            back[at + N] = index++;
        */

        while (!stack.isEmpty()) {
            int c = stack.pollLast();
            if (c < 0) {
                c = ~c;
                et[index] = c;
                back[c + V] = index++;
                continue;
            }

            et[index] = c;
            back[c] = index++;
            stack.add(~c);

            for (int k : g2[c]) {
                if (k != parent(c)) stack.add(k);
            }
        }

        return new int[][] {et, back};
    }
}