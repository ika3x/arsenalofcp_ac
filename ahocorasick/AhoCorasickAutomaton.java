import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Aho-Corasick automaton.
 *
 * This class builds a trie and completes its transitions using
 * failure links.
 */
public class AhoCorasickAutomaton {
    public static final String LOWER_CASE_ALPHABET =
            "abcdefghijklmnopqrstuvwxyz";

    public static final String UPPER_CASE_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final char[] alphabet;
    private final int[] alphabetIndex;

    /*
     * edge[v][c] is the trie edge from vertex v using alphabet[c].
     * A value of -1 means that the trie edge does not exist.
     */
    private final ArrayList<int[]> edge = new ArrayList<>();

    /*
     * transition[v][c] is the completed Aho-Corasick transition.
     * It is available only after build().
     */
    private int[][] transition;

    private final ArrayList<Integer> addedStringLengths = new ArrayList<>();
    private final ArrayList<int[]> prefixVertices = new ArrayList<>();

    /*
     * terminal[v] is true when vertex v itself represents the end
     * of a registered string.
     */
    private final ArrayList<Boolean> terminal = new ArrayList<>();

    /*
     * output[v] is true when vertex v itself, or one of its suffixes,
     * represents the end of a registered string.
     * It is available only after build().
     */
    private boolean[] output;

    private boolean built;

    /**
     * Creates an automaton using the characters in the given alphabet.
     *
     * @param alphabet characters that may be used by the automaton
     */
    public AhoCorasickAutomaton(String alphabet) {
        this(requireAlphabet(alphabet));
    }

    /**
     * Creates an automaton using the characters in the given alphabet.
     *
     * @param alphabet characters that may be used by the automaton
     */
    public AhoCorasickAutomaton(char[] alphabet) {
        if (alphabet == null) {
            throw new NullPointerException(
                    "AhoCorasickAutomaton constructor received null alphabet");
        }
        if (alphabet.length == 0) {
            throw new IllegalArgumentException(
                    "AhoCorasickAutomaton alphabet must not be empty");
        }

        this.alphabet = alphabet.clone();
        this.alphabetIndex = new int[Character.MAX_VALUE + 1];
        Arrays.fill(this.alphabetIndex, -1);

        for (int i = 0; i < this.alphabet.length; i++) {
            char c = this.alphabet[i];
            if (this.alphabetIndex[c] != -1) {
                throw new IllegalArgumentException(
                        "AhoCorasickAutomaton alphabet contains duplicate character "
                                + describeChar(c)
                                + " at positions "
                                + this.alphabetIndex[c]
                                + " and "
                                + i);
            }
            this.alphabetIndex[c] = i;
        }

        addVertex();
    }

    /**
     * Adds a string to the trie.
     *
     * @param s string to add
     * @return this object
     */
    public AhoCorasickAutomaton add(String s) {
        if (s == null) {
            throw new NullPointerException(
                    "AhoCorasickAutomaton.add(String) received null string");
        }
        return add(s.toCharArray());
    }

    /**
     * Adds a string to the trie.
     *
     * The input array is copied logically while this method is running.
     * Later modifications to the caller's array do not affect this object.
     *
     * @param s string to add
     * @return this object
     */
    public AhoCorasickAutomaton add(char[] s) {
        checkNotBuilt("add");

        if (s == null) {
            throw new NullPointerException(
                    "AhoCorasickAutomaton.add(char[]) received null array");
        }

        char[] copied = s.clone();
        int[] vertices = new int[copied.length + 1];

        int v = 0;
        vertices[0] = 0;

        for (int i = 0; i < copied.length; i++) {
            char c = copied[i];
            int alphabetPosition = indexOf(c);

            if (alphabetPosition < 0) {
                throw new IllegalArgumentException(
                        "AhoCorasickAutomaton.add encountered a character "
                                + describeChar(c)
                                + " at string position "
                                + i
                                + ", but that character is not contained in the alphabet");
            }

            int next = edge.get(v)[alphabetPosition];
            if (next == -1) {
                next = addVertex();
                edge.get(v)[alphabetPosition] = next;
            }

            v = next;
            vertices[i + 1] = v;
        }

        addedStringLengths.add(copied.length);
        prefixVertices.add(vertices);
        terminal.set(v, true);

        return this;
    }

    /**
     * Builds all Aho-Corasick transitions.
     */
    public void build() {
        checkNotBuilt("build");

        int vertexCount = edge.size();
        int alphabetSize = alphabet.length;

        transition = new int[vertexCount][alphabetSize];
        output = new boolean[vertexCount];

        transition = new int[vertexCount][alphabetSize];

        for (int v = 0; v < vertexCount; v++) {
            for (int c = 0; c < alphabetSize; c++) {
                transition[v][c] = edge.get(v)[c];
            }
        }

        int[] failure = new int[vertexCount];
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        /*
         * Initialize transitions from the root.
         */
        output[0] = terminal.get(0);

        for (int c = 0; c < alphabetSize; c++) {
            int child = edge.get(0)[c];

            if (child == -1) {
                transition[0][c] = 0;
            } else {
                failure[child] = 0;
                queue.addLast(child);
            }
        }

        /*
         * Breadth-first construction of failure links and
         * completion of missing transitions.
         */
        while (!queue.isEmpty()) {
            int v = queue.removeFirst();

            output[v] = terminal.get(v) || output[failure[v]];

            for (int c = 0; c < alphabetSize; c++) {
                int child = edge.get(v)[c];

                if (child == -1) {
                    transition[v][c] = transition[failure[v]][c];
                    continue;
                }

                failure[child] = transition[failure[v]][c];
                queue.addLast(child);
            }
        }

        built = true;
    }

    /**
     * Returns the number of vertices in the automaton.
     *
     * @return number of vertices
     */
    public int size() {
        checkBuilt("size");
        return transition.length;
    }

    /**
     * Returns the destination of a completed Aho-Corasick transition.
     *
     * @param v source vertex
     * @param c input character
     * @return destination vertex
     */
    public int next(int v, char c) {
        checkBuilt("next");

        checkVertex(v, "next source vertex");

        int alphabetPosition = indexOf(c);
        if (alphabetPosition < 0) {
            throw new IllegalArgumentException(
                    "AhoCorasickAutomaton.next received character "
                            + describeChar(c)
                            + ", which is not contained in the alphabet");
        }

        return transition[v][alphabetPosition];
    }

    /**
     * Returns the trie vertex corresponding exactly to the given string.
     *
     * This method does not interpret failure transitions. Therefore, the
     * returned vertex exists only when the exact string is a trie prefix.
     *
     * @param s string to find
     * @return corresponding vertex
     */
    public int vertexOf(String s) {
        if (s == null) {
            throw new NullPointerException(
                    "AhoCorasickAutomaton.vertexOf(String) received null string");
        }
        return vertexOf(s.toCharArray());
    }

    /**
     * Returns the trie vertex corresponding exactly to the given string.
     *
     * @param s string to find
     * @return corresponding vertex
     */
    public int vertexOf(char[] s) {
        checkBuilt("vertexOf(char[])");

        if (s == null) {
            throw new NullPointerException(
                    "AhoCorasickAutomaton.vertexOf(char[]) received null array");
        }

        int v = 0;

        for (int i = 0; i < s.length; i++) {
            char c = s[i];
            int alphabetPosition = indexOf(c);

            if (alphabetPosition < 0) {
                throw new IllegalArgumentException(
                        "AhoCorasickAutomaton.vertexOf(char[]) encountered "
                                + describeChar(c)
                                + " at string position "
                                + i
                                + ", but that character is not contained in the alphabet");
            }

            int next = edge.get(v)[alphabetPosition];
            if (next == -1) {
                throw new IllegalArgumentException(
                        "AhoCorasickAutomaton.vertexOf(char[]) failed at string position "
                                + i
                                + " for character "
                                + describeChar(c)
                                + ": no trie vertex represents the prefix "
                                + quoteChars(s, 0, i + 1));
            }

            v = next;
        }

        return v;
    }

    /**
     * Returns the vertex corresponding to the prefix [0, j) of the
     * i-th string passed to add().
     *
     * @param i index of the added string
     * @param j prefix end position, inclusive of 0 and exclusive of j
     * @return corresponding vertex
     */
    public int vertexOf(int i, int j) {
        checkBuilt("vertexOf(int, int)");

        if (i < 0 || i >= addedStringLengths.size()) {
            throw new IndexOutOfBoundsException(
                    "AhoCorasickAutomaton.vertexOf(int, int) received i="
                            + i
                            + ", but the valid range is [0, "
                            + addedStringLengths.size()
                            + ")");
        }

        int stringLength = addedStringLengths.get(i);
        if (j < 0 || j > stringLength) {
            throw new IndexOutOfBoundsException(
                    "AhoCorasickAutomaton.vertexOf(int, int) received j="
                            + j
                            + " for added string index i="
                            + i
                            + " whose length is "
                            + stringLength
                            + "; the valid range is [0, "
                            + stringLength
                            + "]");
        }

        return prefixVertices.get(i)[j];
    }

    /**
     * Returns whether vertex v itself represents the end of a
     * registered string.
     *
     * @param v vertex
     * @return true if vertex v is terminal
     */
    public boolean isTerminal(int v) {
        checkBuilt("isTerminal");
        checkVertex(v, "isTerminal vertex");
        return terminal.get(v);
    }

    /**
     * Returns whether vertex v itself, or one of its suffixes,
     * represents the end of a registered string.
     *
     * This is an alias of hasPatternSuffix(int).
     *
     * @param v vertex
     * @return true if a registered string exists as a suffix of v
     */
    public boolean hasOutput(int v) {
        checkBuilt("hasOutput");
        checkVertex(v, "hasOutput vertex");
        return output[v];
    }

    public int root() {
        return 0;
    }

    private int addVertex() {
        int[] row = new int[alphabet.length];
        Arrays.fill(row, -1);
        edge.add(row);
        terminal.add(false);
        return edge.size() - 1;
    }

    private int indexOf(char c) {
        return alphabetIndex[c];
    }

    private void checkBuilt(String operation) {
        if (!built) {
            throw new IllegalStateException(
                    "AhoCorasickAutomaton."
                            + operation
                            + " cannot be called before build()");
        }
    }

    private void checkNotBuilt(String operation) {
        if (built) {
            throw new IllegalStateException(
                    "AhoCorasickAutomaton."
                            + operation
                            + " cannot be called after build(); "
                            + "create a new automaton to add more strings");
        }
    }

    private void checkVertex(int v, String description) {
        if (v < 0 || v >= transition.length) {
            throw new IndexOutOfBoundsException(
                    "AhoCorasickAutomaton "
                            + description
                            + " is out of range: v="
                            + v
                            + ", valid range is [0, "
                            + transition.length
                            + ")");
        }
    }

    private static char[] requireAlphabet(String alphabet) {
        if (alphabet == null) {
            throw new NullPointerException(
                    "AhoCorasickAutomaton constructor received null alphabet String");
        }
        return alphabet.toCharArray();
    }

    private static String describeChar(char c) {
        if (c >= 0x20 && c <= 0x7e) {
            return "'" + c + "' (U+" + hex4(c) + ")";
        }
        return "U+" + hex4(c);
    }

    private static String hex4(char c) {
        return String.format("%04X", (int) c);
    }

    private static String quoteChars(char[] s, int from, int to) {
        StringBuilder sb = new StringBuilder(to - from + 2);
        sb.append('"');
        for (int i = from; i < to; i++) {
            char c = s[i];
            if (c >= 0x20 && c <= 0x7e && c != '"' && c != '\\') {
                sb.append(c);
            } else {
                sb.append("\\u").append(hex4(c));
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
