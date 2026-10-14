public class BsAlias {
    public static int lowerBound(int[] arr, int key) {
        int lo = 0;
        int hi = arr.length;

        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (arr[mid] < key) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }

        return lo;
    }

    public static int lowerBound(long[] arr, long key) {
        int lo = 0;
        int hi = arr.length;

        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (arr[mid] < key) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }

        return lo;
    }

    public static int upperBound(int[] arr, int key) {
        int lo = 0;
        int hi = arr.length;

        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (arr[mid] <= key) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }

        return lo;
    }

    public static int upperBound(long[] arr, long key) {
        int lo = 0;
        int hi = arr.length;

        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (arr[mid] <= key) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }

        return lo;
    }

    public static int countEqual(int[] arr, int key) {
        return upperBound(arr, key) - lowerBound(arr, key);
    }

    public static int countEqual(long[] arr, long key) {
        return upperBound(arr, key) - lowerBound(arr, key);
    }

    public static int countLess(int[] arr, int key) {
        return lowerBound(arr, key);
    }

    public static int countLess(long[] arr, long key) {
        return lowerBound(arr, key);
    }

    public static int countLessOrEqual(int[] arr, int key) {
        return upperBound(arr, key);
    }

    public static int countLessOrEqual(long[] arr, long key) {
        return upperBound(arr, key);
    }

    public static int countGreater(int[] arr, int key) {
        return arr.length - upperBound(arr, key);
    }

    public static int countGreater(long[] arr, long key) {
        return arr.length - upperBound(arr, key);
    }

    public static int countGreaterOrEqual(int[] arr, int key) {
        return arr.length - lowerBound(arr, key);
    }

    public static int countGreaterOrEqual(long[] arr, long key) {
        return arr.length - lowerBound(arr, key);
    }

    public static boolean contains(int[] arr, int key) {
        int index = lowerBound(arr, key);
        return index < arr.length && arr[index] == key;
    }

    public static boolean contains(long[] arr, long key) {
        int index = lowerBound(arr, key);
        return index < arr.length && arr[index] == key;
    }

    public static int countRange(int[] arr, int fromInclusive, int toExclusive) {
        return lowerBound(arr, toExclusive) - lowerBound(arr, fromInclusive);
    }

    public static int countRange(long[] arr, long fromInclusive, long toExclusive) {
        return lowerBound(arr, toExclusive) - lowerBound(arr, fromInclusive);
    }

    public static int countRangeClosed(int[] arr, int fromInclusive, int toInclusive) {
        return upperBound(arr, toInclusive) - lowerBound(arr, fromInclusive);
    }

    public static int countRangeClosed(long[] arr, long fromInclusive, long toInclusive) {
        return upperBound(arr, toInclusive) - lowerBound(arr, fromInclusive);
    }

    public static int ceiling(int[] arr, int key) {
        int index = lowerBound(arr, key);
        return index < arr.length ? arr[index] : Integer.MAX_VALUE;
    }

    public static long ceiling(long[] arr, long key) {
        int index = lowerBound(arr, key);
        return index < arr.length ? arr[index] : Long.MAX_VALUE;
    }

    public static int higher(int[] arr, int key) {
        int index = upperBound(arr, key);
        return index < arr.length ? arr[index] : Integer.MAX_VALUE;
    }

    public static long higher(long[] arr, long key) {
        int index = upperBound(arr, key);
        return index < arr.length ? arr[index] : Long.MAX_VALUE;
    }

    public static int lower(int[] arr, int key) {
        int index = lowerBound(arr, key)-1;
        return index >= 0 ? arr[index] : Integer.MIN_VALUE;
    }

    public static long lower(long[] arr, long key) {
        int index = lowerBound(arr, key)-1;
        return index >= 0 ? arr[index] : Long.MIN_VALUE;
    }

    public static int floor(int[] arr, int key) {
        int index = upperBound(arr, key)-1;
        return index >= 0 ? arr[index] : Integer.MIN_VALUE;
    }

    public static long floor(long[] arr, long key) {
        int index = upperBound(arr, key)-1;
        return index >= 0 ? arr[index] : Long.MIN_VALUE;
    }
}