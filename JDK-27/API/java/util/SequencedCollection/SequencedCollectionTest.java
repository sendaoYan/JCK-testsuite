/*
 * @test
 * @summary java.util.SequencedCollection conformance: white-box structure + black-box behavior.
 * @run main SequencedCollectionTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.SequencedCollection;

public class SequencedCollectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.SequencedCollection");
        check(c.isInterface(), "is an interface");
        check(java.util.Collection.class.isAssignableFrom(c), "extends Collection");
        check(Modifier.isAbstract(c.getDeclaredMethod("reversed").getModifiers()), "reversed abstract");
        for (String m : new String[] {"addFirst", "addLast"})
            check(!Modifier.isAbstract(c.getDeclaredMethod(m, Object.class).getModifiers()),
                    m + " default");
        for (String m : new String[] {"getFirst", "getLast", "removeFirst", "removeLast"})
            check(!Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " default");
    }

    static void testBlackBox() {
        SequencedCollection<String> c = new ArrayList<>();
        c.addFirst("a");
        c.addLast("c");
        c.addFirst("z");
        check(c.equals(List.of("z", "a", "c")), "addFirst/addLast order");
        check("z".equals(c.getFirst()) && "c".equals(c.getLast()), "getFirst/getLast");
        check("z".equals(c.removeFirst()) && "c".equals(c.removeLast()), "removeFirst/removeLast");
        check(c.equals(List.of("a")), "remaining after removes");
        check(c.reversed().getFirst().equals("a"), "reversed view");
        check(c.reversed() instanceof SequencedCollection, "reversed is SequencedCollection");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
