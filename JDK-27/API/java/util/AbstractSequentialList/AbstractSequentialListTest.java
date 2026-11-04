/*
 * @test
 * @summary java.util.AbstractSequentialList conformance: white-box structure + black-box behavior.
 * @run main AbstractSequentialListTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class AbstractSequentialListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Minimal concrete subclass: implements only size() + listIterator(int). */
    static class SL extends AbstractSequentialList<String> {
        final List<String> backing = new ArrayList<>();
        @Override public int size() { return backing.size(); }
        @Override public ListIterator<String> listIterator(int index) { return backing.listIterator(index); }
        void fill(String... items) { backing.addAll(Arrays.asList(items)); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.AbstractSequentialList");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.util.AbstractList.class.isAssignableFrom(c), "extends AbstractList");
        Method li = c.getDeclaredMethod("listIterator", int.class);
        check(Modifier.isAbstract(li.getModifiers()), "listIterator(int) abstract");
        check(!Modifier.isAbstract(c.getDeclaredMethod("get", int.class).getModifiers()), "get concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("set", int.class, Object.class).getModifiers()),
                "set concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("add", int.class, Object.class).getModifiers()),
                "add(int) concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("remove", int.class).getModifiers()),
                "remove(int) concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        SL sl = new SL();
        sl.fill("a", "b", "c", "d");

        // 1. get via listIterator(i).next(); set via listIterator(i).set
        check("a".equals(sl.get(0)) && "d".equals(sl.get(3)), "get by index");
        check("b".equals(sl.set(1, "B")), "set returns old");
        check("B".equals(sl.get(1)), "set updates value");

        // 2. add(int) inserts; remove(int) removes
        sl.add(0, "head");
        check("head".equals(sl.get(0)) && sl.size() == 5, "add(int) inserts");
        check("head".equals(sl.remove(0)), "remove(int) returns removed");
        check(sl.size() == 4 && "a".equals(sl.get(0)), "remove(int) shrinks");

        // 3. addAll(int, collection) inserts in order
        sl.addAll(1, Arrays.asList("x", "y"));
        check(sl.equals(Arrays.asList("a", "x", "y", "B", "c", "d")), "addAll(int, collection)");

        // 4. iterator() and full listIterator() both drive the sequence
        SL sl2 = new SL();
        sl2.fill("1", "2", "3");
        StringBuilder sb = new StringBuilder();
        for (String s : sl2) sb.append(s);
        check("123".contentEquals(sb), "iterator() via listIterator");
        ListIterator<String> li = sl2.listIterator();
        check(li.next().equals("1") && li.next().equals("2") && li.previous().equals("2"),
                "listIterator bidirectional");
        check(li.nextIndex() == 1 && li.previousIndex() == 0, "listIterator indexes");

        // 5. inherited List methods (indexOf/subList) work
        SL sl3 = new SL();
        sl3.fill("p", "q", "p");
        check(sl3.indexOf("p") == 0 && sl3.lastIndexOf("p") == 2, "indexOf/lastIndexOf");
        check(sl3.subList(1, 3).equals(Arrays.asList("q", "p")), "subList");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
