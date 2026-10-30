/*
 * @test
 * @summary JLS 18 (Java SE 8): the diamond operator infers type arguments from the
 *          context of a class instance creation expression.
 * @run main DiamondInferenceTest
 */
import java.util.*;

public class DiamondInferenceTest {
    public static void main(String[] args) {
        List<String> l = new ArrayList<>();   // diamond infers String
        l.add("x");
        if (!l.get(0).equals("x")) throw new AssertionError("diamond infers type");

        Map<String, Integer> m = new HashMap<>();
        m.put("k", 1);
        if (m.get("k") != 1) throw new AssertionError("diamond map");

        System.out.println("PASS");
    }
}
