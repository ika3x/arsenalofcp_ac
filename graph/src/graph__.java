import java.io.*;
import java.util.*;
import java.util.stream.*;

public class graph__ {}

class Graph {
    public static class IntMutList implements Iterable<Integer> {
        private int[] dat;
        private int size;
        
        private IntMutList(int capacity) {
            dat = new int[capacity];
            size = 0;
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
            newCap = Math.max(newCap, 8);
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
            return toString_addK(0);
        }

        public String toString_addK(int k) {
            if (size == 0) {return "[]";}
            StringBuilder sb = new StringBuilder("[");
            sb.append(dat[0] + k);
            for (int i = 1; i < size; i++) {
                sb.append(',').append(' ').append(dat[i] + k);
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

    public static class IntDqList {
        private int[] dat;
        private int head;
        private int tail;
        private int mask;
        private int size;

        private IntDqList(int capacity) {
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

        public void addLast1(int x) {
            --x;
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

        public void addFirst1(int x) {
            --x;
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

        public int pollFirst1() {
            if (size == 0) throw new NoSuchElementException();
            int res = dat[head];
            head = (head + 1) & mask;
            size--;
            return res + 1;
        }

        public int pollLast() {
            if (size == 0) throw new NoSuchElementException();
            tail = (tail - 1) & mask;
            int res = dat[tail];
            size--;
            return res;
        }

        public int pollLast1() {
            if (size == 0) throw new NoSuchElementException();
            tail = (tail - 1) & mask;
            int res = dat[tail];
            size--;
            return res + 1;
        }

        private int get(int index) {
            if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
            return dat[(head + index) & mask];
        }

        public int getFirst() {
            if (size == 0) throw new NoSuchElementException();
            return dat[head];
        }

        public int getFirst1() {
            if (size == 0) throw new NoSuchElementException();
            return dat[head] + 1;
        }

        public int getLast() {
            if (size == 0) throw new NoSuchElementException();
            return dat[(tail - 1) & mask];
        }

        public int getLast1() {
            if (size == 0) throw new NoSuchElementException();
            return dat[(tail - 1) & mask] + 1;
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

        @Override
        public String toString() {
            if (size == 0) {return "[]";}
            StringBuilder sb = new StringBuilder("[");
            sb.append(getFirst());
            for (int i = 1; i < size; i++) {
                sb.append(',').append(' ').append(get(i));
            }
            return sb.append(']').toString();
        }
    }

    @FunctionalInterface
    public interface ForEachOp {
        void run(int n);
    }

    private final int[] inDeg, outDeg;
    private final IntMutList[] g;
    private final int n;
    private int edgeCount = 0;
    private int uflag = 0;

    public final IntDqList dq;

    public Graph(int vertexCount) {
        this.n = vertexCount;
        this.g = new IntMutList[n];
        for (int i = 0; i < n; i++) {
            this.g[i] = new IntMutList(4);
        }
        this.inDeg = new int[n];
        this.outDeg = new int[n];

        this.dq = new IntDqList(8);
    }

    public void addEdge(int u, int v) { g[u].add(v); inDeg[v]++; outDeg[u]++; uflag--; edgeCount++; }
    public void uAddEdge(int u, int v) { addEdge(u, v); addEdge(v, u); uflag += 2; }
    public void addEdge1(int u, int v) { addEdge(u-1, v-1); }
    public void uAddEdge1(int u, int v) { uAddEdge(u-1, v-1); }

    public void scanEdge(FastScanner sc) { addEdge(sc.nextInt(), sc.nextInt()); }
    public void scanEdge1(FastScanner sc) { addEdge1(sc.nextInt(), sc.nextInt()); }
    public void uScanEdge(FastScanner sc) { uAddEdge(sc.nextInt(), sc.nextInt()); }
    public void uScanEdge1(FastScanner sc) { uAddEdge1(sc.nextInt(), sc.nextInt()); }

    public void scanManyEdges(FastScanner sc, int m) {
        for (int i = 0; i < m; i++) {
            addEdge(sc.nextInt(), sc.nextInt());
        }
    }

    public void scanManyEdges1(FastScanner sc, int m) {
        for (int i = 0; i < m; i++) {
            addEdge(sc.nextInt()-1, sc.nextInt()-1);
        }
    }

    public void uScanManyEdges(FastScanner sc, int m) {
        for (int i = 0; i < m; i++) {
            uAddEdge(sc.nextInt(), sc.nextInt());
        }
    }

    public void uScanManyEdges1(FastScanner sc, int m) {
        for (int i = 0; i < m; i++) {
            uAddEdge(sc.nextInt()-1, sc.nextInt()-1);
        }
    }

    public int getInDeg(int i) { return inDeg[i]; }
    public int getInDeg1(int i) { return inDeg[i-1]; }
    public int getOutDeg(int i) { return outDeg[i]; }
    public int getOutDeg1(int i) { return outDeg[i-1]; }

    public int uGetDeg(int i) {
        if (!debug_isUndirected()) { throw new IllegalStateException("The graph must be undirected!"); }
        return inDeg[i];
    }
    public int uGetDeg1(int i) { return uGetDeg(i-1); }

    public int[] cloneOfInDegArray() { return inDeg.clone(); }
    public int[] cloneOfOutDegArray() { return outDeg.clone(); }

    public int[] uCloneOfDegArray() {
        if (!debug_isUndirected()) { throw new IllegalStateException("The graph must be undirected!"); }
        return inDeg.clone();
    }

    public PrimitiveIterator.OfInt iteratorOf(int v) { return g[v].iterator(); }
    public PrimitiveIterator.OfInt iteratorOf1(int v) { return g[v-1].iterator(); }

    public void forEach(int v, ForEachOp op) {
        var it = iteratorOf(v);
        while (it.hasNext()) {
            op.run(it.nextInt());
        }
    }
    public void forEach1(int v, ForEachOp op) {
        var it = iteratorOf(v-1);
        while (it.hasNext()) {
            op.run(it.nextInt() + 1);
        }
    }

    public int[] distArray(int start) {
        IntDqList queue = new IntDqList(8);
        int[] d = new int[n]; Arrays.fill(d,-1); d[start] = 0;
        queue.addLast(start);
        while (!queue.isEmpty()) {
            int c = queue.pollFirst();
            var gc = g[c];
            int gcSize = gc.size();
            for (int i = 0; i < gcSize; i++) {
                int k = gc.get(i);
                if (d[k] == -1) {
                    d[k] = d[c] + 1;
                    queue.addLast(k);
                }
            }
        }

        return d;
    }
    public int[] distArray1(int s) { return distArray(s-1); }

    /*
    * Get depth / parent
    * res[0]: dist/depth
    * res[1]: parent
    */
    public int[][] __tree__multiInfo(int start, int parOfRoot) {
        IntDqList queue = new IntDqList(8);
        int[][] res = new int[2][n];
        for (int[] r : res) Arrays.fill(r,-1);
        res[0][start] = 0; res[1][start] = parOfRoot;

        queue.addLast(start);
        while (!queue.isEmpty()) {
            int c = queue.pollFirst();
            var gc = g[c];
            int gcSize = gc.size();
            for (int i = 0; i < gcSize; i++) {
                int k = gc.get(i);
                if (res[0][k] == -1) {
                    res[0][k] = res[0][c] + 1;
                    res[1][k] = c;
                    queue.addLast(k);
                }
            }
        }

        return res;
    }
    public int[][] __tree__multiInfo1(int s, int parOfRoot) { return __tree__multiInfo(s-1, parOfRoot); }

    public Graph reversedGraph() {
        Graph h = new Graph(n);
        for (int i = 0; i < n; i++) {
            var gi = g[i];
            int giSize = gi.size();
            for (int j = 0; j < giSize; j++) {
                h.addEdge(gi.dat[j], i);
            }
        }

        h.uflag = this.uflag;
        return h;
    }

    public boolean debug_isUndirected() { return uflag == 0; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Graph(");
        boolean ud = debug_isUndirected();
        sb.append(ud ? "Und" : "D").append("irected, V=").append(n).append(", E=");
        sb.append(ud ? (edgeCount >> 1) : edgeCount).append(")");

        return sb.toString();
    }

    public String adjString_1Indexed() { return adjString_kIndexed(1); }
    public String adjString_0Indexed() { return adjString_kIndexed(0); }

    private String adjString_kIndexed(int k) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < n; i++) {
            if (i != 0) {sb.append('\n');}
            sb.append(i+k).append(": ");
            sb.append(g[i].toString_addK(k));
        }

        return sb.toString();
    }
    
    public IntMutList[] _rawAdjData() {
        return g;
    }
}

class FastScanner {
        private final byte[] buffer = new byte[1 << 16];
        private int ptr = 0, len = 0;
        private final InputStream in = System.in;

        private int readByte() {
            if (ptr >= len) {
                try {
                    len = in.read(buffer);
                    ptr = 0;
                    if (len <= 0) return -1;
                } catch (IOException e) {
                    return -1;
                }
            }
            return buffer[ptr++];
        }

        private boolean isPrintable(int c) {
            return c > ' ';
        }

        public String next() {
            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = readByte()) != -1 && !isPrintable(c));
            if (c == -1) return null;
            do {
                sb.append((char) c);
            } while ((c = readByte()) != -1 && isPrintable(c));
            return sb.toString();
        }
        
        public char nextChar() {
            return next().charAt(0);
        }

        public String nextLine() {
            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = readByte()) != -1) {
                if (c == '\n') break;
                if (c != '\r') sb.append((char) c);
            }
            return sb.toString();
        }

        public int nextInt() {
            return (int) nextLong();
        }

        public long nextLong() {
            int c;
            while ((c = readByte()) != -1 && c <= ' ');
            boolean neg = false;
            if (c == '-') {
                neg = true;
                c = readByte();
            }
            long val = 0;
            while (c > ' ') {
                val = val * 10 + (c - '0');
                c = readByte();
            }
            return neg ? -val : val;
        }

        public double nextDouble() {
            return Double.parseDouble(next());
        }

        public int[] nextIntArray(int n) {
            int[] arr = new int[n];
            for (int i = 0; i < n; i++) {
                arr[i] = nextInt();
            }
            return arr;
        }

        public long[] nextLongArray(int n) {
            long[] arr = new long[n];
            for (int i = 0; i < n; i++) {
                arr[i] = nextLong();
            }
            return arr;
        }

        public String[] nextStringArray(int n) {
            String[] arr = new String[n];
            for (int i = 0; i < n; i++) {
                arr[i] = next();
            }
            return arr;
        }

        public char[][] nextCharGrid(int h, int w) {
            char[][] g = new char[h][w];
            for (int i = 0; i < h; i++) {
                g[i] = next().toCharArray();
            }
            return g;
        }

        public int[][] nextIntMatrix(int h, int w) {
            int[][] mat = new int[h][];
            for (int i = 0; i < h; i++) {
                mat[i] = nextIntArray(w);
            }
            return mat;
        }

        public long[][] nextLongMatrix(int h, int w) {
            long[][] mat = new long[h][];
            for (int i = 0; i < h; i++) {
                mat[i] = nextLongArray(w);
            }
            return mat;
        }
    }

