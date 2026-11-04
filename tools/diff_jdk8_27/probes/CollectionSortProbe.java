import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CollectionSortProbe {
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
            List<Integer> l = new ArrayList<Integer>(Arrays.asList(3, -1, 2, -1, 0, 100, Integer.MIN_VALUE, Integer.MAX_VALUE));
            Collections.sort(l);
            emit("sorted", l.toString());

            List<String> s = new ArrayList<String>(Arrays.asList("banana", "Apple", "cherry", "apple"));
            Collections.sort(s);
            emit("sorted.strings", s.toString());

            List<String> s2 = new ArrayList<String>(Arrays.asList("banana", "Apple", "cherry", "apple"));
            Collections.sort(s2, String.CASE_INSENSITIVE_ORDER);
            emit("sorted.caseInsensitive", s2.toString());

            List<Integer> r = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4));
            Collections.sort(r, new Comparator<Integer>() {
                public int compare(Integer x, Integer y) { return y - x; }
            });
            emit("sorted.reverse", r.toString());

            List<Integer> stable = new ArrayList<Integer>();
            stable.add(1); stable.add(1); stable.add(1);
            Collections.sort(stable);
            emit("stable", stable.toString());

            ex("sort.nullList", () -> Collections.sort(null));
            List<Integer> withNull = new ArrayList<Integer>(Arrays.asList(1, null, 2));
            ex("sort.withNull", () -> Collections.sort(withNull));
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
