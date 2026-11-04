/*
 * @test
 * @summary java.util.SequencedSet conformance: white-box structure + black-box behavior.
 * @run main SequencedSetTest
 */

import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.Set;

public class SequencedSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.SequencedSet");
        check(c.isInterface(), "is an interface");
        check(Set.class.isAssignableFrom(c), "extends Set");
        check(Modifier.isAbstract(c.getDeclaredMethod("reversed").getModifiers()), "reversed abstract");
    }

    static void testBlackBox() {
        SequencedSet<String> s = new LinkedHashSet<>();
        s.add("b");
        s.add("a");
        s.add("c");
        s.add("a");
        check(s.size() == 3, "no duplicates");
        check(s.equals(Set.of("a", "b", "c")), "set equality");
        check(s.getFirst().equals("b") && s.getLast().equals("c"), "insertion order first/last");
        SequencedSet<String> rev = s.reversed();
        check(rev.getFirst().equals("c") && rev.getLast().equals("b"), "reversed order");
        check(rev instanceof SequencedSet, "reversed is SequencedSet");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
