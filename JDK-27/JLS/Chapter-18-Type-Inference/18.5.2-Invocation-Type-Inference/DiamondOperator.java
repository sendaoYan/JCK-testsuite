/*
 * @test
 * @summary JLS 18.5.2: the diamond operator infers the type arguments of a class
 *          instance creation expression from the target type.
 * @run main DiamondOperator
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiamondOperator {
    public static void main(String[] args) {
        List<String> l = new ArrayList<>();       // infers String
        l.add("a");
        if (!"a".equals(l.get(0))) throw new AssertionError("diamond list");

        Map<String, List<Integer>> m = new HashMap<>();   // infers nested types
        m.put("k", Arrays.asList(1, 2));
        if (m.get("k").size() != 2) throw new AssertionError("diamond map");

        System.out.println("PASS");
    }
}
