import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class BinarySearchProbe {
    interface Fn { void run() throws Exception; }
    static void emit(String k, Object v) { System.out.println(k + "=" + v); }
    static void ex(String k, Fn f) {
        try { f.run(); }
        catch (Throwable t) { System.out.println(k + "=exception=" + t.getClass().getName()); }
    }
    public static void main(String[] a) {
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        try {
            int[] arr = {-10, -1, 0, 3, 5, 9, 100};
            emit("found", Arrays.binarySearch(arr, 3));
            emit("missing.mid", Arrays.binarySearch(arr, 4));
            emit("missing.before", Arrays.binarySearch(arr, -100));
            emit("missing.after", Arrays.binarySearch(arr, 1000));
            emit("first", Arrays.binarySearch(arr, -10));
            emit("last", Arrays.binarySearch(arr, 100));
            emit("dup", Arrays.binarySearch(new int[]{1, 1, 1, 2, 2}, 2));
            emit("empty", Arrays.binarySearch(new int[0], 1));

            List<Integer> list = Arrays.asList(-5, 0, 7, 8);
            emit("listFound", Collections.binarySearch(list, 7));
            emit("listMissing", Collections.binarySearch(list, 3));

            String[] strings = {"a", "c", "e"};
            emit("stringFound", Arrays.binarySearch(strings, "c"));
            emit("stringMissing", Arrays.binarySearch(strings, "b"));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
