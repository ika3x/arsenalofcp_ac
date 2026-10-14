import java.io.*;
import java.util.*;
import java.util.stream.*;

public class SkeletonCf {
    static FastScanner sc = new FastScanner();
    static PrintWriter2 out = new PrintWriter2(new BufferedWriter(new OutputStreamWriter(System.out)));
    static final long STACK_RAM_MiB = 128;
    static final boolean SINGLE_TESTCASE = false;

    public static void codeforces() {
        /* Start here. */
        
        /* Main-logic method ends here. */
    }

    public static void main(String[] args) {
        Thread mainsolve = new Thread(null, () -> {
            try {
                multiTestCasesBridge();
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }, "all", STACK_RAM_MiB << 20);

        mainsolve.start();

        try {
            mainsolve.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void multiTestCasesBridge() {
        int T = SINGLE_TESTCASE ? 1 : sc.nextInt();
        while (T-- > 0) {
            codeforces();
        }
    }

    static class FastScanner {
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

    static class PrintWriter2 extends PrintWriter {
        public PrintWriter2(Writer out) {
            super(out);
        }

        public void println(int[] ar) {
            println(ar, 0, -1);
        }

        public void println(int[] ar, int startIdx, int endIdx) {
            if (endIdx < 0) {endIdx = ar.length;}
            int e = Math.min(ar.length, endIdx);

            if (startIdx == e) {
                super.println();
                return;
            }
            if (startIdx >= e || startIdx < 0) {
                throw new IllegalArgumentException(
                    "Length=" + ar.length + ", unable to print " + " [" + startIdx + "," + endIdx + ")"
                );
            }
            StringBuilder sb = new StringBuilder();
            sb.append(ar[startIdx]);
            for (int i = startIdx+1; i < e; i++) {
                sb.append(' ').append(ar[i]);
            }

            super.println(sb.toString());
        }

        public void println(long[] ar) {
            println(ar, 0, -1);
        }

        public void println(long[] ar, int startIdx, int endIdx) {
            if (endIdx < 0) {endIdx = ar.length;}
            int e = Math.min(ar.length, endIdx);

            
            if (startIdx == e) {
                super.println();
                return;
            }
            if (startIdx >= e || startIdx < 0) {
                throw new IllegalArgumentException(
                    "Length=" + ar.length + ", unable to print " + " [" + startIdx + "," + endIdx + ")"
                );
            }
            StringBuilder sb = new StringBuilder();
            sb.append(ar[startIdx]);
            for (int i = startIdx+1; i < e; i++) {
                sb.append(' ').append(ar[i]);
            }

            super.println(sb.toString());
        }

        public <T> void println(List<T> list) {
            if (list == null) {
                throw new NullPointerException("Couldn't print the list because it was null.");
            }

            if (list.isEmpty()) {
                super.println();
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(list.get(0));
            
            for (int i = 1; i < list.size(); i++) {
                sb.append(' ').append(list.get(i));
            }
            
            super.println(sb.toString());
        }

        public void debug_grid(char[][] g) {
            for (char[] c : g) {
                System.err.println(c);
            }
        }

        public void printgrid(char[][] g) {
            for (char[] c : g) {
                super.println(c);
            }
        }

        @Override
        public void println(double val) {
            super.printf("%.16f\n", val);
        }

        @Override
        public void print(double val) {
            super.printf("%.16f", val);
        }

        public void yn_println(boolean bool) {
            super.println(bool?"Yes":"No");
        }

        public void bin_print(boolean bool) {
            super.print(bool?1:0);
        }

        public void bin_println(boolean bool) {
            bin_print(bool);
            super.println();
        }

        public void bin_println(boolean[] bar) {
            for (boolean b : bar) {
                bin_print(b);
            }
            super.println();
        }
    }
}

/* ==================================
 * References / Licenses
 * Almost all libraries in this code come from the following two sources.
 * - "AtCoderLibraryForJava" by NASU41: https://github.com/NASU41/AtCoderLibraryForJava (SPDX-License-Identifier: CC0-1.0)
 * - "ArsenalOfCp" by ika3x: https://github.com/ika3x/arsenalofcp_ac (SPDX-License-Identifier: Unlicense)
 * 
 * In particular, all code that I generated with generative AI comes from ArsenalOfCp (listed above).
 * Any other code was written by me (ika3gg) without generative AI, or is the third-party code credited above.
 * 
 * [ika3x] on GitHub is [ika3gg] on AtCoder, Codeforces, etc.
 * 
 * License notice for my submissions:
 * [On or after 2026-10-14 (UTC)]
 * All submissions by [ika3gg] on AtCoder and Codeforces are released under
 * The Unlicense (SPDX-License-Identifier: Unlicense), excluding third-party code credited above,
 * which remains under its original license.
 *
 * [Before 2026-10-14 (UTC)]
 * Code written by [ika3gg] in earlier submissions is also under The Unlicense.
 * However, those submissions may contain third-party code (e.g., libraries) that was not
 * credited at the time. Such code is NOT covered by this notice and remains under its
 * original license. If you reuse an earlier submission, please check the license yourself.
 * 
 * ika3gg - 2026-10-14
 * https://atcoder.jp/contests/abc472/submissions/80038150
 * ===================================
 */