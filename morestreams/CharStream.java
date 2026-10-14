import java.util.*;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.LongUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class CharStream {
    private static class CharList {
        private char[] data;
        private int size;

        private CharList() {
            this.data = new char[8];
            this.size = 0;
        }

        public void add(char c) {
            if (size >= data.length) {
                // 容量が足りなくなったら2倍に拡張
                char[] next = new char[data.length << 1];
                System.arraycopy(data, 0, next, 0, data.length);
                data = next;
            }
            data[size++] = c;
        }

        public char[] toArray() {
            char[] result = new char[size];
            System.arraycopy(data, 0, result, 0, size);
            return result;
        }
    }

    @FunctionalInterface
    public interface CharPredicate { boolean test(char c); }
    @FunctionalInterface
    public interface CharUnaryOperator { char applyAsChar(char c); }
    @FunctionalInterface
    public interface CharConsumer { void accept(char c); }

    private final char[] data;
    private final int[] nextPtr;
    private int size;
    
    public CharStream(char[] ca) {
        this.data = ca.clone();
        this.nextPtr = IntStream.rangeClosed(0,data.length).toArray();
        nextPtr[data.length-1] = -1;
        nextPtr[data.length] = 0;

        size = data.length;
    }
    public CharStream(String s) {
        this.data = s.toCharArray();
        this.nextPtr = IntStream.rangeClosed(0,data.length).toArray();
        nextPtr[data.length-1] = -1;
        nextPtr[data.length] = 0;

        size = data.length;
    }

    public static CharStream fromInt(IntStream s) {
        int[] t = s.toArray();

        char[] d = new char[t.length];
        for (int i = 0; i < t.length; i++) {
            d[i] = (char) t[i];
        }

        return new CharStream(d);
    }

    public CharStream filter(CharPredicate f) {
        int i = nextPtr[data.length];
        int prev = data.length;
        while (i != -1) {
            if (f.test(data[i])) {
                prev = i;   
            } else {
                nextPtr[prev] = nextPtr[i];
                size--;
            }
            i = nextPtr[i];
        }
        return this;
    }

    public CharStream map(CharUnaryOperator op) {
        int i = nextPtr[data.length];
        while (i != -1) {
            data[i] = op.applyAsChar(data[i]);
            i = nextPtr[i];
        }
        return this;
    }

    public CharStream peek(CharConsumer action) {
        int i = nextPtr[data.length];
        while (i != -1) {
            action.accept(data[i]);
            i = nextPtr[i];
        }
        return this;
    }

    public CharStream distinct() {
        if (size >= (Character.MAX_VALUE >> 1)) {
            _distinctByBooleanArray();
        } else {
            _distinctBySet();
        }

        return this;
    }

    private void _distinctByBooleanArray() {
        boolean[] seen = new boolean[Character.MAX_VALUE+1];
        int i = nextPtr[data.length];
        int prev = data.length;

        while (i != -1) {
            if (!seen[data[i]]) {
                prev = i;
                seen[data[i]] = true;
            } else {
                nextPtr[prev] = nextPtr[i];
                size--;
            }

            i = nextPtr[i];
        }
    }

    private void _distinctBySet() {
        int i = nextPtr[data.length];
        int prev = data.length;
        Set<Character> seen = new HashSet<>();

        while (i != -1) {
            if (seen.add(data[i])) {
                prev = i;
            } else {
                nextPtr[prev] = nextPtr[i];
                size--;
            }

            i = nextPtr[i];
        }
    }

    public IntStream asInt() {
        int[] r = new int[size];
        int i = nextPtr[data.length];
        int j = 0;
        while (i != -1) {
            r[j++] = data[i];
            i = nextPtr[i];
        }
        return Arrays.stream(r);
    }

    public IntStream mapToInt(IntUnaryOperator op) {
        int[] r = new int[size];
        int i = nextPtr[data.length];
        int j = 0;
        while (i != -1) {
            r[j++] = op.applyAsInt(data[i]);
            i = nextPtr[i];
        }
        return Arrays.stream(r);
    }

    public <R> Stream<R> mapToObj(Function<Character, R> op) {
        Stream.Builder<R> builder = Stream.builder();

        int i = nextPtr[data.length];
        while (i != -1) {
            builder.add(op.apply(data[i]));
            i = nextPtr[i];
        }

        return builder.build();
    }

    public CharStream skip(long n) {
        if (n <= 0) { return this; }
        int i = nextPtr[data.length];
        while (i != -1 && n-- > 0) {
            i = nextPtr[i];
        }
        nextPtr[data.length] = i;
        return this;
    }

    public CharStream limit(long n) {
        if (n < 0) {
            throw new IllegalArgumentException("limit must be non-negative");
        }
        int i = nextPtr[data.length];
        if (n == 0) {
            nextPtr[data.length] = -1;
            return this;
        }
        while (i != -1 && --n > 0) {
            i = nextPtr[i];
        }
        if (i != -1) {
            nextPtr[i] = -1;
        }
        return this;
    }

    // end

    
}
