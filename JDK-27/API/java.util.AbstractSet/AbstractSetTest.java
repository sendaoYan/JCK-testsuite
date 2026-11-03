/*
 * @test
 * @summary java.util.AbstractSet conformance: white-box structure + black-box behavior.
 * @run main AbstractSetTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AbstractSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Minimal concrete subclass: iterator + size + add. */
    static class S extends AbstractSet<String> {
        final Set<String> backing = new LinkedHashSet<>();
        @Override public Iterator<String> iterator() { return backing.iterator(); }
        @Override public int size() { return backing.size(); }
        @Override public boolean add(String e) { return backing.add(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.AbstractSet");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.util.AbstractCollection.class.isAssignableFrom(c), "extends AbstractCollection");
        check(Set.class.isAssignableFrom(c), "implements Set");
        Method eq = c.getDeclaredMethod("equals", Object.class);
        check(!Modifier.isAbstract(eq.getModifiers()), "equals concrete");
        Method hc = c.getDeclaredMethod("hashCode");
        check(!Modifier.isAbstract(hc.getModifiers()), "hashCode concrete");
        check(!Modifier.isAbstract(c.getDeclaredMethod("removeAll", java.util.Collection.class).getModifiers()),
                "removeAll concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. add/contains/size/isEmpty/remove inherited
        S s = new S();
        check(s.add("a") && s.add("b") && !s.add("a"), "add returns false on duplicate");
        check(s.size() == 2 && s.contains("a") && !s.contains("z"), "size/contains");
        check(!s.isEmpty(), "not empty");
        check(s.remove("a") && !s.contains("a") && s.size() == 1, "remove");

        // 2. equals: size + containsAll (order-independent)
        S s1 = new S(); s1.add("x"); s1.add("y");
        S s2 = new S(); s2.add("y"); s2.add("x");
        check(s1.equals(s2) && s2.equals(s1), "AbstractSet.equals is order-independent");
        check(s1.hashCode() == s2.hashCode(), "hashCode equals for equal sets");
        S s3 = new S(); s3.add("x"); s3.add("z");
        check(!s1.equals(s3), "different elements -> not equal");

        // 3. hashCode is the sum of element hashCodes
        S nums = new S(); nums.add("1"); nums.add("2"); nums.add("3");
        check(nums.hashCode() == ("1".hashCode() + "2".hashCode() + "3".hashCode()),
                "hashCode is sum of element hashCodes");

        // 4. equals against a non-Set / null / self
        check(!s1.equals(null), "equals(null) false");
        check(!s1.equals("xy"), "equals(other type) false");
        check(s1.equals(s1), "equals(self) true");

        // 5. removeAll removes common elements
        S base = new S(); base.add("a"); base.add("b"); base.add("c");
        List<String> other = new ArrayList<>(Arrays.asList("b", "c", "d"));
        check(base.removeAll(other), "removeAll returns true");
        check(base.equals(new LinkedHashSet<>(Arrays.asList("a"))), "removeAll removes intersection");

        // 6. toArray / iteration inherited from AbstractCollection
        S ord = new S(); ord.add("p"); ord.add("q");
        check(Arrays.equals(ord.toArray(), new Object[] {"p", "q"}), "toArray via iterator order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
