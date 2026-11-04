/*
 * @test
 * @summary java.lang.Comparable API conformance: compareTo sign (negative/zero/positive),
 *          antisymmetry of Integer/String natural ordering, self-comparison == 0, and
 *          consistency with Collections.sort.
 * @run main ComparableTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ComparableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInteger() {
        Integer a = Integer.valueOf(3), b = Integer.valueOf(7), c = Integer.valueOf(3);
        check(a.compareTo(b) < 0, "3.compareTo(7) < 0");
        check(b.compareTo(a) > 0, "7.compareTo(3) > 0");
        check(a.compareTo(c) == 0, "3.compareTo(3) == 0");
        check(a.compareTo(a) == 0, "self compareTo == 0");
        check(Integer.compare(a, b) == a.compareTo(b), "Integer.compare consistent");
    }

    static void testString() {
        check("apple".compareTo("banana") < 0, "\"apple\".compareTo(\"banana\") < 0");
        check("banana".compareTo("apple") > 0, "\"banana\".compareTo(\"apple\") > 0");
        check("same".compareTo("same") == 0, "equal strings compareTo == 0");
    }

    static void testSort() {
        List<Integer> xs = new ArrayList<>(Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6));
        Collections.sort(xs);
        check(xs.equals(Arrays.asList(1, 1, 2, 3, 4, 5, 6, 9)), "Collections.sort natural order");
    }

    static final class Version implements Comparable<Version> {
        final int major, minor;
        Version(int m, int n) { major = m; minor = n; }
        public int compareTo(Version o) {
            int d = major - o.major;
            return d != 0 ? d : minor - o.minor;
        }
        public String toString() { return major + "." + minor; }
    }

    static void testCustomComparable() {
        List<Version> vs = new ArrayList<>(Arrays.asList(
            new Version(2, 1), new Version(1, 9), new Version(2, 0), new Version(1, 0)));
        Collections.sort(vs);
        check(vs.toString().equals("[1.0, 1.9, 2.0, 2.1]"), "Version natural sort");
        check(new Version(1, 9).compareTo(new Version(2, 0)) < 0, "1.9 < 2.0");
        check(new Version(2, 0).compareTo(new Version(2, 1)) < 0, "2.0 < 2.1");
        check(new Version(2, 1).compareTo(new Version(2, 1)) == 0, "equal versions");
    }

    static void testNullAndClassCast() {
        check("a".compareTo("b") == -1, "String compareTo exact -1");
        try {
            "a".compareTo(null);
            throw new AssertionError("String.compareTo(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        Comparable c = Integer.valueOf(1);
        try {
            c.compareTo("x");
            throw new AssertionError("Integer.compareTo(String): expected ClassCastException");
        } catch (ClassCastException expected) {
            passed++;
        }
    }

    static void testSignProperties() {
        String a = "apple", b = "banana", c = "cherry";
        check(a.compareTo(b) < 0 && b.compareTo(c) < 0, "transitive order");
        check(a.compareTo(c) < 0, "transitive a<c");
        check(Integer.signum(a.compareTo(b)) == -Integer.signum(b.compareTo(a)), "antisymmetry");
        check(a.compareTo(b) == java.util.Comparator.<String>naturalOrder().compare(a, b),
              "naturalOrder consistent");
        check("10".compareTo("9") < 0, "lexicographic (not numeric)");
        check("a".compareTo("A") > 0, "case-sensitive: 'a'(97) > 'A'(65)");
    }

    // ---------- white-box: structural / reflection ----------
    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try {
            c.getMethod(name, params);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Comparable");
        check(c.isInterface(), "Comparable is an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "Comparable abstract");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Comparable not final");
        check(c.getTypeParameters().length == 1, "Comparable<T> has one type parameter");
        check(hasMethod(c, "compareTo", Object.class), "abstract compareTo(Object)");
    }

    // ---------- black-box: behavioral composition ----------
    static void testBlackBox() {
        testInteger();
        testString();
        testSort();
        testCustomComparable();
        testNullAndClassCast();
        testSignProperties();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
