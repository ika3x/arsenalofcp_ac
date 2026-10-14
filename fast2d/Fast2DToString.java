final class Fast2DToString {

    private Fast2DToString() {
    }

    public static String numbers(int h, int w, int[] data) {
        String[] values = new String[data.length];
        for (int i = 0; i < data.length; i++) {
            values[i] = Integer.toString(data[i]);
        }
        return numbers(h, w, values);
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

        // 列ごとの最大文字数を求める
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

    public static String chars(int h, int w, char[] data) {
        if (h == 0 || w == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int x = 0; x < h; x++) {
            for (int y = 0; y < w; y++) {
                sb.append(data[x * w + y]);
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    public static String booleans(int h, int w, boolean[] data) {
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
}