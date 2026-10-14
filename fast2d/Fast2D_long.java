import java.util.Arrays;

public class Fast2D_long {
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