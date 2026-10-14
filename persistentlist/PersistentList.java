import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class PersistentList<T> {
    private static final class Node<E> {
        final Object[] children;
        final E value;

        Node(E value) {
            this.children = null;
            this.value = value;
        }

        Node(Object[] children) {
            this.children = children;
            this.value = null;
        }

        boolean isLeaf() {
            return children == null;
        }
    }

    private final int size;
    private final int branchingFactor;
    private final int height;
    private final long[] blockSize;
    private final Class<?> componentType;
    private final ArrayList<Node<T>> versions;

    @SuppressWarnings("unchecked")
    public PersistentList(int n, int k) {
        validateSize(n);
        validateBranchingFactor(k);

        this.size = n;
        this.branchingFactor = k;
        this.height = calculateHeight(n, k);
        this.blockSize = calculateBlockSizes(height, k);
        this.componentType = Object.class;
        this.versions = new ArrayList<>();

        versions.add(null);
    }

    public PersistentList(T[] a, int k) {
        Objects.requireNonNull(a, "Debug: input array must not be null.");
        validateBranchingFactor(k);

        this.size = a.length;
        this.branchingFactor = k;
        this.height = calculateHeight(a.length, k);
        this.blockSize = calculateBlockSizes(height, k);
        this.componentType = a.getClass().getComponentType();
        this.versions = new ArrayList<>();

        Object[] source = Arrays.copyOf(a, a.length, Object[].class);
        Node<T> root = build(source, 0, height);
        versions.add(root);
    }

    public int set(int version, int index, T x) {
        checkVersion(version);
        checkIndex(index);

        Node<T> oldRoot = versions.get(version);
        Node<T> newRoot = update(oldRoot, height, index, x);

        versions.add(newRoot);
        return versions.size() - 1;
    }

    public int setL(int index, T x) {
        
        return set(latestVersion(), index, x);
    }

    public T get(int version, int index) {
        checkVersion(version);
        checkIndex(index);

        Node<T> node = versions.get(version);
        int level = height;

        while (node != null && level > 0) {
            int childIndex = childIndex(index, level);
            node = castNode(node.children[childIndex]);
            level--;
        }

        return node == null ? null : node.value;
    }

    public T getL(int index) {
        return get(latestVersion(), index);
    }

    public Iterator<T> iterator(int version) {
        checkVersion(version);

        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException(
                            "Debug: iterator reached the end. version="
                                    + version + ", size=" + size);
                }
                return get(version, index++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(
                        "Debug: PersistentList iterator does not support remove().");
            }
        };
    }

    public Iterator<T> iteratorL() {
        return iterator(latestVersion());
    }

    public int binarySearch(
            int version,
            T target,
            Comparator<? super T> comparator) {
        checkVersion(version);
        Comparator<? super T> cmp = comparatorOrNatural(comparator);

        int low = 0;
        int high = size - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int result = cmp.compare(get(version, mid), target);

            if (result < 0) {
                low = mid + 1;
            } else if (result > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }

        return -low - 1;
    }

    public int binarySearch(int version, T target) {
        return binarySearch(version, target, null);
    }

    public int binarySearchL(
            T target,
            Comparator<? super T> comparator) {
        return binarySearch(latestVersion(), target, comparator);
    }

    public int binarySearchL(T target) {
        return binarySearch(latestVersion(), target);
    }

    public int lowerBound(
            int version,
            T target,
            Comparator<? super T> comparator) {
        checkVersion(version);
        Comparator<? super T> cmp = comparatorOrNatural(comparator);

        int low = 0;
        int high = size;

        while (low < high) {
            int mid = (low + high) >>> 1;
            if (cmp.compare(get(version, mid), target) < 0) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }

        return low;
    }

    public int lowerBound(int version, T target) {
        return lowerBound(version, target, null);
    }

    public int lowerBoundL(
            T target,
            Comparator<? super T> comparator) {
        return lowerBound(latestVersion(), target, comparator);
    }

    public int lowerBoundL(T target) {
        return lowerBound(latestVersion(), target);
    }

    public int upperBound(
            int version,
            T target,
            Comparator<? super T> comparator) {
        checkVersion(version);
        Comparator<? super T> cmp = comparatorOrNatural(comparator);

        int low = 0;
        int high = size;

        while (low < high) {
            int mid = (low + high) >>> 1;
            if (cmp.compare(get(version, mid), target) <= 0) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }

        return low;
    }

    public int upperBound(int version, T target) {
        return upperBound(version, target, null);
    }

    public int upperBoundL(
            T target,
            Comparator<? super T> comparator) {
        return upperBound(latestVersion(), target, comparator);
    }

    public int upperBoundL(T target) {
        return upperBound(latestVersion(), target);
    }

    @SuppressWarnings("unchecked")
    public T[] toArray(int version) {
        checkVersion(version);

        T[] result = (T[]) Array.newInstance(componentType, size);

        for (int i = 0; i < size; i++) {
            result[i] = get(version, i);
        }

        return result;
    }

    public T[] toArrayL() {
        return toArray(latestVersion());
    }

    public int latestVersion() {
        return versions.size() - 1;
    }

    @Override
    public String toString() {
        return "PersistentArray(n: "
                + size
                + ", Versions: "
                + versions.size()
                + ")";
    }

    private Node<T> update(
            Node<T> node,
            int level,
            int index,
            T value) {
        if (level == 0) {
            return new Node<>(value);
        }

        Object[] children = node == null
                ? new Object[branchingFactor]
                : node.children.clone();

        int childIndex = childIndex(index, level);
        Node<T> oldChild = castNode(children[childIndex]);

        children[childIndex] = update(
                oldChild,
                level - 1,
                index,
                value);

        return new Node<>(children);
    }

    private Node<T> build(
            Object[] source,
            int offset,
            int level) {
        if (offset >= source.length) {
            return null;
        }

        if (level == 0) {
            @SuppressWarnings("unchecked")
            T value = (T) source[offset];

            return value == null ? null : new Node<>(value);
        }

        Object[] children = new Object[branchingFactor];
        boolean hasValue = false;
        long childBlockSize = blockSize[level - 1];

        int firstChild = 0;
        int lastChild = branchingFactor;

        int remaining = source.length - offset;
        if (remaining < childBlockSize * branchingFactor) {
            lastChild = (int) ((remaining + childBlockSize - 1)
                    / childBlockSize);
        }

        for (int childIndex = firstChild;
             childIndex < lastChild;
             childIndex++) {
            long childOffsetLong = offset
                    + childIndex * childBlockSize;

            if (childOffsetLong >= source.length) {
                break;
            }

            int childOffset = (int) childOffsetLong;
            Node<T> child = build(
                    source,
                    childOffset,
                    level - 1);

            children[childIndex] = child;
            hasValue |= child != null;
        }

        return hasValue ? new Node<>(children) : null;
    }

    private int childIndex(int index, int level) {
        return (int) ((index / blockSize[level - 1])
                % branchingFactor);
    }

    private static int calculateHeight(int n, int k) {
        if (n <= 1) {
            return 0;
        }

        int height = 0;
        long capacity = 1;

        while (capacity < n) {
            if (capacity > Long.MAX_VALUE / k) {
                throw new IllegalArgumentException(
                        "Debug: tree capacity overflow. n="
                                + n + ", k=" + k);
            }

            capacity *= k;
            height++;
        }

        return height;
    }

    private static long[] calculateBlockSizes(
            int height,
            int k) {
        long[] result = new long[height + 1];
        result[0] = 1;

        for (int i = 1; i <= height; i++) {
            if (result[i - 1] > Long.MAX_VALUE / k) {
                throw new IllegalArgumentException(
                        "Debug: block size overflow. height="
                                + height + ", k=" + k);
            }
            result[i] = result[i - 1] * k;
        }

        return result;
    }

    private static void validateSize(int n) {
        if (n < 0) {
            throw new IllegalArgumentException(
                    "Debug: n must be non-negative. n=" + n);
        }
    }

    private static void validateBranchingFactor(int k) {
        if (k < 2 || (k & (k - 1)) != 0) {
            throw new IllegalArgumentException(
                    "Debug: k must be a power of two and at least 2. k="
                            + k);
        }
    }

    private void checkVersion(int version) {
        if (version < 0 || version >= versions.size()) {
            throw new IllegalArgumentException(
                    "Debug: invalid version. version="
                            + version
                            + ", validRange=0.."
                            + latestVersion());
        }
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                    "Debug: index out of range. index="
                            + index
                            + ", validRange=0.."
                            + (size - 1));
        }
    }

    private void returnValueCheck() {
        if (versions.isEmpty()) {
            throw new IllegalStateException(
                    "Debug: version storage is unexpectedly empty.");
        }
    }

    @SuppressWarnings("unchecked")
    private static <E> Node<E> castNode(Object object) {
        return (Node<E>) object;
    }

    @SuppressWarnings("unchecked")
    private Comparator<? super T> comparatorOrNatural(
            Comparator<? super T> comparator) {
        if (comparator != null) {
            return comparator;
        }

        return (left, right) -> {
            if (left == null || right == null) {
                throw new IllegalStateException(
                        "Debug: natural ordering cannot compare null values.");
            }

            try {
                return ((Comparable<Object>) left).compareTo(right);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(
                        "Debug: elements must implement Comparable "
                                + "when comparator is null. leftType="
                                + left.getClass().getName()
                                + ", rightType="
                                + right.getClass().getName(),
                        e);
            }
        };
    }
}