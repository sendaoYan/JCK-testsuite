/*
 * @test
 * @summary JLS 18.5.2 (Java SE 8): the diamond operator infers nested type
 *          arguments (e.g. Map<String, List<Integer>>) from the target type.
 * @run main NestedGenericInference
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NestedGenericInference {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Map<String, List<Integer>> m = new HashMap<>();
        m.put("k", Arrays.asList(1, 2, 3));
        check(m.get("k").size() == 3 && m.get("k").get(0) == 1, "nested diamond infers full type");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
