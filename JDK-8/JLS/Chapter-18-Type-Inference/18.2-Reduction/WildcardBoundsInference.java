/*
 * @test
 * @summary JLS 18.2 / 18.4 (Java SE 8): type arguments are inferred from wildcard
 *          bounds (? super T and ? extends T) arising from argument types.
 * @run main WildcardBoundsInference
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WildcardBoundsInference {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static <T> void copyInto(List<? super T> dest, List<? extends T> src) {
        dest.addAll(src);
    }

    public static void main(String[] args) {
        List<Object> dest = new ArrayList<>();
        List<String> src = new ArrayList<>(Arrays.asList("a", "b"));
        copyInto(dest, src);
        check(dest.size() == 2 && "a".equals(dest.get(0)), "? super T / ? extends T bounds infer T=String");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
