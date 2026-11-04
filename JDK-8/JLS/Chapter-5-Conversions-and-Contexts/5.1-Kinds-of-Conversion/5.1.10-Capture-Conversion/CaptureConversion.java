/*
 * @test
 * @summary JLS 5.1.10 (Java SE 8): capture conversion opens a wildcard type
 *          argument to a fresh type variable. It enables a wildcard method to
 *          delegate to a generic helper naming the element type (the spec's
 *          reverse(List<?>) { rev(list); } example).
 * @run main CaptureConversion
 */

import java.util.ArrayList;
import java.util.List;

public class CaptureConversion {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void reverse(List<?> list) {
        rev(list);   // capture conversion: List<?> -> List<CAP> passed to List<T>
    }

    private static <T> void rev(List<T> list) {
        List<T> tmp = new ArrayList<T>(list);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, tmp.get(list.size() - i - 1));
        }
    }

    public static void main(String[] args) {
        List<String> ls = new ArrayList<String>();
        ls.add("a");
        ls.add("b");
        ls.add("c");

        reverse(ls);

        check(ls.size() == 3, "size preserved");
        check("c".equals(ls.get(0)) && "b".equals(ls.get(1)) && "a".equals(ls.get(2)),
              "reverse via capture conversion");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
