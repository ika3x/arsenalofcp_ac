import java.util.Arrays;

public class Fast2D_int {
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