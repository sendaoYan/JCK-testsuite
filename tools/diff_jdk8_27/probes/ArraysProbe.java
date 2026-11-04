import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

public class ArraysProbe {
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
            int[] x = {5, 3, 9, 1};
            Arrays.sort(x);
            emit("sort", Arrays.toString(x));
            emit("binarySearch", Arrays.binarySearch(x, 3));
            emit("equals", Arrays.equals(new int[]{1, 2}, new int[]{1, 2}));
            emit("equals.order", Arrays.equals(new int[]{1, 2}, new int[]{2, 1}));
            emit("copyOf.grow", Arrays.toString(Arrays.copyOf(new int[]{1, 2, 3}, 5)));
            emit("copyOf.shrink", Arrays.toString(Arrays.copyOf(new int[]{1, 2, 3}, 2)));
            emit("copyOfRange", Arrays.toString(Arrays.copyOfRange(new int[]{1, 2, 3}, 1, 3)));
            int[] f = new int[3];
            Arrays.fill(f, 7);
            emit("fill", Arrays.toString(f));
            ex("sort.null", () -> Arrays.sort((int[]) null));
            ex("copyOfRange.bad", () -> Arrays.copyOfRange(new int[]{1, 2}, 2, 1));
            emit("deepEquals", Arrays.deepEquals(new Object[]{new int[]{1}}, new Object[]{new int[]{1}}));
            emit("hashCode", Arrays.hashCode(new int[]{1, 2, 3}));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
