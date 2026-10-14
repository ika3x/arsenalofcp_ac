import java.util.Arrays;

public class Fast2D_char {
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