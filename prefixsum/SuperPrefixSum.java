import java.util.*;
import java.lang.reflect.Array;

public class SuperPrefixSum {
    private final int[] dimensions;
    private final long[] prefix;
    private final long[] strides;
    private final int totalLength;
    private final Object source;

    // s will be overwritten.
    public SuperPrefixSum(Object s, int[] d) {
        if (s == null) {
            throw new IllegalArgumentException(
                    "Invalid source array: s must not be null."
            );
        }
        if (d == null) {
            throw new IllegalArgumentException(
                    "Invalid dimension definition: d must not be null."
            );
        }
        if (d.length == 0) {
            throw new IllegalArgumentException(
                    "Invalid dimension definition: d must contain at least one dimension."
            );
        }
        if (!s.getClass().isArray()) {
            throw new IllegalArgumentException(
                    "Invalid source array: s must be a multidimensional long array, "
                    + "but the actual type is " + s.getClass().getTypeName() + "."
            );
        }

        this.source = s;
        this.dimensions = Arrays.copyOf(d, d.length);

        long elementCount = 1L;
        for (int i = 0; i < dimensions.length; i++) {
            if (dimensions[i] < 0) {
                throw new IllegalArgumentException(
                        "Invalid dimension size: d[" + i + "] must be non-negative, "
                        + "but was " + dimensions[i] + "."
                );
            }

            elementCount *= dimensions[i];
            if (elementCount > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "Array is too large: the product of all dimension sizes "
                        + "must not exceed Integer.MAX_VALUE, but exceeded it at dimension "
                        + i + "."
                );
            }
        }

        this.totalLength = (int) elementCount;
        this.prefix = new long[this.totalLength];
        this.strides = new long[dimensions.length];

        long stride = 1L;
        for (int i = dimensions.length - 1; i >= 0; i--) {
            strides[i] = stride;
            stride *= dimensions[i];
        }

        validateAndRead(s, 0, 0, "s");
        buildPrefixSum();
        writeBack(source, 0, 0, "s");
    }

    public long sum(int[] from, int[] to) {
        validateRange(from, to);

        for (int i = 0; i < dimensions.length; i++) {
            if (from[i] == to[i]) {
                return 0L;
            }
        }

        int[] coordinate = new int[dimensions.length];
        return calculateRangeSum(0, 0, coordinate, from, to);
    }

    private void validateRange(int[] from, int[] to) {
        if (from == null) {
            throw new IllegalArgumentException(
                    "Invalid range: from must not be null."
            );
        }
        if (to == null) {
            throw new IllegalArgumentException(
                    "Invalid range: to must not be null."
            );
        }
        if (from.length != dimensions.length) {
            throw new IllegalArgumentException(
                    "Invalid range: from length must be " + dimensions.length
                    + ", but was " + from.length
                    + ". from=" + Arrays.toString(from)
                    + ", to=" + Arrays.toString(to) + "."
            );
        }
        if (to.length != dimensions.length) {
            throw new IllegalArgumentException(
                    "Invalid range: to length must be " + dimensions.length
                    + ", but was " + to.length
                    + ". from=" + Arrays.toString(from)
                    + ", to=" + Arrays.toString(to) + "."
            );
        }

        for (int i = 0; i < dimensions.length; i++) {
            if (from[i] < 0 || from[i] > dimensions[i]) {
                throw new IllegalArgumentException(
                        "Invalid range at dimension " + i
                        + ": from[" + i + "] must be in [0, " + dimensions[i]
                        + "], but was " + from[i]
                        + ". from=" + Arrays.toString(from)
                        + ", to=" + Arrays.toString(to) + "."
                );
            }

            if (to[i] < 0 || to[i] > dimensions[i]) {
                throw new IllegalArgumentException(
                        "Invalid range at dimension " + i
                        + ": to[" + i + "] must be in [0, " + dimensions[i]
                        + "], but was " + to[i]
                        + ". from=" + Arrays.toString(from)
                        + ", to=" + Arrays.toString(to) + "."
                );
            }

            if (from[i] > to[i]) {
                throw new IllegalArgumentException(
                        "Invalid range at dimension " + i
                        + ": from[" + i + "] must be less than or equal to to["
                        + i + "], but from[" + i + "]=" + from[i]
                        + " and to[" + i + "]=" + to[i]
                        + ". from=" + Arrays.toString(from)
                        + ", to=" + Arrays.toString(to) + "."
                );
            }
        }
    }

    private void buildPrefixSum() {
        for (int dimension = 0; dimension < dimensions.length; dimension++) {

            if (dimensions[dimension] == 0) {
                continue;
            }

            long stride = strides[dimension];

            for (int index = 0; index < totalLength; index++) {
                long coordinate = (index / stride) % dimensions[dimension];

                if (coordinate > 0) {
                    prefix[index] += prefix[(int) (index - stride)];
                }
            }
        }
    }

    private long calculateRangeSum(
            int dimension,
            int negativeCount,
            int[] coordinate,
            int[] from,
            int[] to) {

        if (dimension == dimensions.length) {
            for (int value : coordinate) {
                if (value < 0) {
                    return 0L;
                }
            }

            long flatIndex = 0L;
            for (int i = 0; i < dimensions.length; i++) {
                flatIndex += (long) coordinate[i] * strides[i];
            }

            long value = prefix[(int) flatIndex];
            return (negativeCount & 1) == 0 ? value : -value;
        }

        long result = 0L;

        coordinate[dimension] = to[dimension] - 1;
        result += calculateRangeSum(
                dimension + 1,
                negativeCount,
                coordinate,
                from,
                to
        );

        coordinate[dimension] = from[dimension] - 1;
        result += calculateRangeSum(
                dimension + 1,
                negativeCount + 1,
                coordinate,
                from,
                to
        );

        return result;
    }

    private void validateAndRead(
            Object array,
            int dimension,
            int baseIndex,
            String path) {

        if (array == null) {
            throw new IllegalArgumentException(
                    "Invalid source array: " + path + " is null."
            );
        }

        if (!array.getClass().isArray()) {
            throw new IllegalArgumentException(
                    "Invalid source array at " + path
                    + ": an array was expected, but the actual type is "
                    + array.getClass().getTypeName() + "."
            );
        }

        int actualLength = Array.getLength(array);
        int expectedLength = dimensions[dimension];

        if (actualLength != expectedLength) {
            throw new IllegalArgumentException(
                    "Invalid source array length at " + path
                    + ": expected " + expectedLength
                    + ", but was " + actualLength
                    + ". Dimension " + dimension
                    + " must match d[" + dimension + "]="
                    + expectedLength + "."
            );
        }

        if (dimension == dimensions.length - 1) {
            if (array.getClass().getComponentType() != long.class) {
                throw new IllegalArgumentException(
                        "Invalid source array at " + path
                        + ": the innermost component type must be long, but the actual type is "
                        + array.getClass().getComponentType().getTypeName() + "."
                );
            }

            long[] leaf = (long[]) array;
            System.arraycopy(leaf, 0, prefix, baseIndex, leaf.length);
            return;
        }

        Class<?> componentType = array.getClass().getComponentType();
        if (!componentType.isArray()) {
            throw new IllegalArgumentException(
                    "Invalid source array at " + path
                    + ": dimension " + dimension
                    + " must contain nested arrays."
            );
        }

        for (int i = 0; i < actualLength; i++) {
            Object child = Array.get(array, i);

            validateAndRead(
                    child,
                    dimension + 1,
                    (int) (baseIndex + i * strides[dimension]),
                    path + "[" + i + "]"
            );
        }
    }

    private void writeBack(
            Object array,
            int dimension,
            int baseIndex,
            String path) {

        if (dimension == dimensions.length - 1) {
            long[] leaf = (long[]) array;
            System.arraycopy(prefix, baseIndex, leaf, 0, leaf.length);
            return;
        }

        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object child = Array.get(array, i);

            writeBack(
                    child,
                    dimension + 1,
                    (int) (baseIndex + i * strides[dimension]),
                    path + "[" + i + "]"
            );
        }
    }
}