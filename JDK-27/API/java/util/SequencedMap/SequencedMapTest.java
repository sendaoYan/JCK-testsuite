/*
 * @test
 * @summary java.util.SequencedMap conformance: white-box structure + black-box behavior.
 * @run main SequencedMapTest
 */

import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

public class SequencedMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.SequencedMap");
        check(c.isInterface(), "is an interface");
        check(Map.class.isAssignableFrom(c), "extends Map");
        check(Modifier.isAbstract(c.getDeclaredMethod("reversed").getModifiers()), "reversed abstract");
        for (String m : new String[] {"firstEntry", "lastEntry", "pollFirstEntry",
                "pollLastEntry"})
            check(!Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " default");
        for (String m : new String[] {"putFirst", "putLast"})
            check(!Modifier.isAbstract(c.getDeclaredMethod(m, Object.class, Object.class)
                    .getModifiers()), m + " default");
    }

    static void testBlackBox() {
        SequencedMap<String, Integer> m = new LinkedHashMap<>();
        m.putFirst("a", 1);
        m.putLast("c", 3);
        m.putFirst("z", 26);
        check(new java.util.ArrayList<>(m.keySet()).equals(java.util.List.of("z", "a", "c")),
                "putFirst/putLast order");
        check("z".equals(m.firstEntry().getKey()) && Integer.valueOf(26).equals(m.firstEntry().getValue()),
                "firstEntry");
        check("c".equals(m.lastEntry().getKey()), "lastEntry");
        check("z".equals(m.pollFirstEntry().getKey()) && "c".equals(m.pollLastEntry().getKey()),
                "pollFirst/pollLast");
        check(m.size() == 1 && m.firstEntry().getKey().equals("a"), "size after polls");
        check(m.reversed().firstEntry().getKey().equals("a"), "reversed view");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
