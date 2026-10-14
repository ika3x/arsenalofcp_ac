import java.util.*;

class Fast2D_int {
    private final int h;
    private final int w;
    private final int[] data;

    public Fast2D_int(int h, int w) {
        this.h = h;
        this.w = w;
        this.data = new int[h * w];
    }

    public Fast2D_int(int[][] grid) {
        this.h = grid.length;
        this.w = h == 0 ? 0 : grid[0].length;
        this.data = new int[h * w];

        for (int x = 0; x < h; x++) {
            System.arraycopy(grid[x], 0, data, x * w, w);
        }
    }

    public int getH() {
        return h;
    }

    public int getW() {
        return w;
    }

    public int size() {
        return h * w;
    }

    public int index(int x, int y) {
        return x * w + y;
    }

    public boolean inside(int x, int y) {
        return 0 <= x && x < h && 0 <= y && y < w;
    }

    public int get(int x, int y) {
        return data[index(x, y)];
    }

    public int getOr(int x, int y, int def) {
        return inside(x, y) ? data[index(x, y)] : def;
    }

    public void set(int x, int y, int v) {
        data[index(x, y)] = v;
    }

    public boolean trySet(int x, int y, int v) {
        if (!inside(x, y)) return false;
        data[index(x, y)] = v;
        return true;
    }

    public void update(int x, int y, UnaryOp_int op) {
        int i = index(x, y);
        data[i] = op.apply(data[i]);
    }

    public void fill(int v) {
        Arrays.fill(data, v);
    }

    @Override
    public String toString() {
        return numbers(h, w, data);
    }

    @FunctionalInterface
    public interface UnaryOp_int {
        int apply(int v);
    }

    public static String numbers(int h, int w, int[] data) {
        String[] values = new String[data.length];
        for (int i = 0; i < data.length; i++) {
            values[i] = Integer.toString(data[i]);
        }
        return numbers(h, w, values);
    }

    private static String numbers(int h, int w, String[] values) {
        if (h == 0 || w == 0) {
            return "";
        }

        int[] widths = new int[w];

        for (int x = 0; x < h; x++) {
            for (int y = 0; y < w; y++) {
                int i = x * w + y;
                widths[y] = Math.max(widths[y], values[i].length());
            }
        }

        StringBuilder sb = new StringBuilder();

        for (int x = 0; x < h; x++) {
            for (int y = 0; y < w; y++) {
                if (y > 0) {
                    sb.append(',');
                }

                int i = x * w + y;
                appendRightAligned(sb, values[i], widths[y]);
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    private static void appendRightAligned(
            StringBuilder sb,
            String value,
            int width
    ) {
        for (int i = value.length(); i < width; i++) {
            sb.append(' ');
        }
        sb.append(value);
    }
}

class Fast2D_boolean {
    private final int h;
    private final int w;
    private final boolean[] data;

    public Fast2D_boolean(int h, int w) {
        this.h = h;
        this.w = w;
        this.data = new boolean[h * w];
    }

    public Fast2D_boolean(boolean[][] grid) {
        this.h = grid.length;
        this.w = h == 0 ? 0 : grid[0].length;
        this.data = new boolean[h * w];

        for (int x = 0; x < h; x++) {
            System.arraycopy(grid[x], 0, data, x * w, w);
        }
    }

    public int getH() {
        return h;
    }

    public int getW() {
        return w;
    }

    public int size() {
        return h * w;
    }

    public int index(int x, int y) {
        return x * w + y;
    }

    public boolean inside(int x, int y) {
        return 0 <= x && x < h && 0 <= y && y < w;
    }

    public boolean get(int x, int y) {
        return data[index(x, y)];
    }

    public boolean getOr(int x, int y, boolean def) {
        return inside(x, y) ? data[index(x, y)] : def;
    }

    public void set(int x, int y, boolean v) {
        data[index(x, y)] = v;
    }

    public boolean trySet(int x, int y, boolean v) {
        if (!inside(x, y)) return false;
        data[index(x, y)] = v;
        return true;
    }

    public void update(int x, int y, UnaryOp_boolean op) {
        int i = index(x, y);
        data[i] = op.apply(data[i]);
    }

    public void fill(boolean v) {
        Arrays.fill(data, v);
    }

    @Override
    public String toString() {
        if (h == 0 || w == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int x = 0; x < h; x++) {
            for (int y = 0; y < w; y++) {
                sb.append(data[x * w + y] ? '1' : '.');
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    @FunctionalInterface
    public interface UnaryOp_boolean {
        boolean apply(boolean v);
    }
}

class Fast2D_char {
    private final int h;
    private final int w;
    private final char[] data;

    public Fast2D_char(int h, int w) {
        this.h = h;
        this.w = w;
        this.data = new char[h * w];
    }

    public Fast2D_char(char[][] grid) {
        this.h = grid.length;
        this.w = h == 0 ? 0 : grid[0].length;
        this.data = new char[h * w];

        for (int x = 0; x < h; x++) {
            System.arraycopy(grid[x], 0, data, x * w, w);
        }
    }

    public int getH() {
        return h;
    }

    public int getW() {
        return w;
    }

    public int size() {
        return h * w;
    }

    public int index(int x, int y) {
        return x * w + y;
    }

    public boolean inside(int x, int y) {
        return 0 <= x && x < h && 0 <= y && y < w;
    }

    public char get(int x, int y) {
        return data[index(x, y)];
    }

    public char getOr(int x, int y, char def) {
        return inside(x, y) ? data[index(x, y)] : def;
    }

    public void set(int x, int y, char v) {
        data[index(x, y)] = v;
    }

    public boolean trySet(int x, int y, char v) {
        if (!inside(x, y)) return false;
        data[index(x, y)] = v;
        return true;
    }

    public void update(int x, int y, UnaryOp_char op) {
        int i = index(x, y);
        data[i] = op.apply(data[i]);
    }

    public void fill(char v) {
        Arrays.fill(data, v);
    }

    @Override
    public String toString() {
        if (h == 0 || w == 0) return "";

        StringBuilder sb = new StringBuilder();

        for (int x = 0; x < h; x++) {
            for (int y = 0; y < w; y++) {
                sb.append(data[x * w + y]);
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    @FunctionalInterface
    public interface UnaryOp_char {
        char apply(char v);
    }
}

class Fast2D_double {
    private final int h;
    private final int w;
    private final double[] data;

    public Fast2D_double(int h, int w) {
        this.h = h;
        this.w = w;
        this.data = new double[h * w];
    }

    public Fast2D_double(double[][] grid) {
        this.h = grid.length;
        this.w = h == 0 ? 0 : grid[0].length;
        this.data = new double[h * w];

        for (int x = 0; x < h; x++) {
            System.arraycopy(grid[x], 0, data, x * w, w);
        }
    }

    public int getH() {
        return h;
    }

    public int getW() {
        return w;
    }

    public int size() {
        return h * w;
    }

    public int index(int x, int y) {
        return x * w + y;
    }

    public boolean inside(int x, int y) {
        return 0 <= x && x < h && 0 <= y && y < w;
    }

    public double get(int x, int y) {
        return data[index(x, y)];
    }

    public double getOr(int x, int y, double def) {
        return inside(x, y) ? data[index(x, y)] : def;
    }

    public void set(int x, int y, double v) {
        data[index(x, y)] = v;
    }

    public boolean trySet(int x, int y, double v) {
        if (!inside(x, y)) return false;
        data[index(x, y)] = v;
        return true;
    }

    public void update(int x, int y, UnaryOp_double op) {
        int i = index(x, y);
        data[i] = op.apply(data[i]);
    }

    public void fill(double v) {
        Arrays.fill(data, v);
    }

    @Override
    public String toString() {
        if (h == 0 || w == 0) return "";

        StringBuilder sb = new StringBuilder();

        for (int x = 0; x < h; x++) {
            for (int y = 0; y < w; y++) {
                if (y > 0) sb.append(' ');
                sb.append(String.format("%.4f", data[index(x, y)]));
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    @FunctionalInterface
    public interface UnaryOp_double {
        double apply(double v);
    }
}

class Fast2D_float {
    private final int h;
    private final int w;
    private final float[] data;

    public Fast2D_float(int h, int w) {
        this.h = h;
        this.w = w;
        this.data = new float[h * w];
    }

    public Fast2D_float(float[][] grid) {
        this.h = grid.length;
        this.w = h == 0 ? 0 : grid[0].length;
        this.data = new float[h * w];

        for (int x = 0; x < h; x++) {
            System.arraycopy(grid[x], 0, data, x * w, w);
        }
    }

    public int getH() {
        return h;
    }

    public int getW() {
        return w;
    }

    public int size() {
        return h * w;
    }

    public int index(int x, int y) {
        return x * w + y;
    }

    public boolean inside(int x, int y) {
        return 0 <= x && x < h && 0 <= y && y < w;
    }

    public float get(int x, int y) {
        return data[index(x, y)];
    }

    public float getOr(int x, int y, float def) {
        return inside(x, y) ? data[index(x, y)] : def;
    }

    public void set(int x, int y, float v) {
        data[index(x, y)] = v;
    }

    public boolean trySet(int x, int y, float v) {
        if (!inside(x, y)) return false;
        data[index(x, y)] = v;
        return true;
    }

    public void update(int x, int y, UnaryOp_float op) {
        int i = index(x, y);
        data[i] = op.apply(data[i]);
    }

    public void fill(float v) {
        Arrays.fill(data, v);
    }

    @Override
    public String toString() {
        if (h == 0 || w == 0) return "";

        StringBuilder sb = new StringBuilder();

        for (int x = 0; x < h; x++) {
            for (int y = 0; y < w; y++) {
                if (y > 0) sb.append(' ');
                sb.append(String.format("%.4f", data[index(x, y)]));
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    @FunctionalInterface
    public interface UnaryOp_float {
        float apply(float v);
    }
}

class Fast2D_long {
    private final int h;
    private final int w;
    private final long[] data;

    public Fast2D_long(int h, int w) {
        this.h = h;
        this.w = w;
        this.data = new long[h * w];
    }

    public Fast2D_long(long[][] grid) {
        this.h = grid.length;
        this.w = h == 0 ? 0 : grid[0].length;
        this.data = new long[h * w];

        for (int x = 0; x < h; x++) {
            System.arraycopy(grid[x], 0, data, x * w, w);
        }
    }

    public int getH() {
        return h;
    }

    public int getW() {
        return w;
    }

    public int size() {
        return h * w;
    }

    public int index(int x, int y) {
        return x * w + y;
    }

    public boolean inside(int x, int y) {
        return 0 <= x && x < h && 0 <= y && y < w;
    }

    public long get(int x, int y) {
        return data[index(x, y)];
    }

    public long getOr(int x, int y, long def) {
        return inside(x, y) ? data[index(x, y)] : def;
    }

    public void set(int x, int y, long v) {
        data[index(x, y)] = v;
    }

    public boolean trySet(int x, int y, long v) {
        if (!inside(x, y)) return false;
        data[index(x, y)] = v;
        return true;
    }

    public void update(int x, int y, UnaryOp_long op) {
        int i = index(x, y);
        data[i] = op.apply(data[i]);
    }

    public void fill(long v) {
        Arrays.fill(data, v);
    }

    @Override
    public String toString() {
        if (h == 0 || w == 0) return "";

        StringBuilder sb = new StringBuilder();

        for (int x = 0; x < h; x++) {
            for (int y = 0; y < w; y++) {
                if (y > 0) sb.append(' ');
                sb.append(String.format("%.4f", data[index(x, y)]));
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    @FunctionalInterface
    public interface UnaryOp_long {
        long apply(long v);
    }

    public static String numbers(int h, int w, long[] data) {
        String[] values = new String[data.length];
        for (int i = 0; i < data.length; i++) {
            values[i] = Long.toString(data[i]);
        }
        return numbers(h, w, values);
    }

    private static String numbers(int h, int w, String[] values) {
        if (h == 0 || w == 0) {
            return "";
        }

        int[] widths = new int[w];

        for (int x = 0; x < h; x++) {
            for (int y = 0; y < w; y++) {
                int i = x * w + y;
                widths[y] = Math.max(widths[y], values[i].length());
            }
        }

        StringBuilder sb = new StringBuilder();

        for (int x = 0; x < h; x++) {
            for (int y = 0; y < w; y++) {
                if (y > 0) {
                    sb.append(',');
                }

                int i = x * w + y;
                appendRightAligned(sb, values[i], widths[y]);
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    private static void appendRightAligned(
            StringBuilder sb,
            String value,
            int width
    ) {
        for (int i = value.length(); i < width; i++) {
            sb.append(' ');
        }
        sb.append(value);
    }
}