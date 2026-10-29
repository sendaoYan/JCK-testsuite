/*
 * @test
 * @summary JLS 5.1.10: capture conversion opens a wildcard type argument to a
 *          fresh type variable. It enables e.g. reverse(List<?>) to call
 *          rev(List<T>). The spec's example: reverse(List<?>) { rev(list); }.
 * @run main CaptureConversion
 */

import java.util.ArrayList;
import java.util.List;

public class CaptureConversion {
    // The public-facing method uses the wildcard List<?>.
    static void reverse(List<?> list) {
        rev(list);   // capture conversion: List<?> -> List<CAP> passed to List<T>
    }

    // The private helper names the element type T (spec Example in 5.1.10).
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

        if (ls.size() != 3) throw new AssertionError("size");
        if (!ls.get(0).equals("c") || !ls.get(1).equals("b") || !ls.get(2).equals("a")) {
            throw new AssertionError("reverse via capture conversion");
        }

        System.out.println("PASS");
    }
}
