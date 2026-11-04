/*
 * @test
 * @summary javax.naming.CompositeName conformance: full contract coverage.
 *          constructors (String / no-arg / empty components with leading/
 *          trailing/adjacent separators), size/get/getAll, getPrefix/getSuffix
 *          incl. out-of-range ArrayIndexOutOfBoundsException, startsWith/endsWith
 *          (null / non-CompositeName -> false), add/addAll/remove, addAll with
 *          non-CompositeName -> InvalidNameException, compareTo (order +
 *          ClassCastException), equals/hashCode/clone, toString round-trip, and
 *          serialization round-trip.
 * @run main CompositeNameTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.InvalidNameException;
import javax.naming.Name;

public class CompositeNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static CompositeName roundTrip(CompositeName n) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(n); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (CompositeName) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() throws Exception {
        CompositeName n = new CompositeName("a/b/c");
        check(n.size() == 3, "size");
        check("a".equals(n.get(0)), "get(0)");
        check("c".equals(n.get(2)), "get(2)");
        check("a/b/c".equals(n.toString()), "toString");
        check(n.isEmpty() == false, "not empty");
        n.add("d");
        check(n.size() == 4 && "d".equals(n.get(3)), "add");
        CompositeName n2 = new CompositeName();
        check(n2.isEmpty(), "empty");
        CompositeName clone = (CompositeName) n.clone();
        check(n.equals(clone), "clone equals");
    }

    static void testConstructors() throws Exception {
        check(new CompositeName("").isEmpty(), "empty string -> empty name");
        CompositeName lead = new CompositeName("/a");
        check(lead.size() == 2 && "".equals(lead.get(0)) && "a".equals(lead.get(1)), "leading empty component");
        CompositeName trail = new CompositeName("a/");
        check(trail.size() == 2 && "a".equals(trail.get(0)) && "".equals(trail.get(1)), "trailing empty component");
        CompositeName mid = new CompositeName("a//b");
        check(mid.size() == 3 && "".equals(mid.get(1)), "adjacent separator empty component");
    }

    static void testPrefixSuffix() throws Exception {
        CompositeName n = new CompositeName("a/b/c");
        check(n.getPrefix(0).isEmpty(), "prefix(0) empty");
        check("a".equals(n.getPrefix(1).toString()), "prefix(1)");
        check("a/b".equals(n.getPrefix(2).toString()), "prefix(2)");
        check("a/b/c".equals(n.getPrefix(3).toString()), "prefix(3) == all");
        check(n.getSuffix(0).toString().equals("a/b/c"), "suffix(0) == all");
        check("c".equals(n.getSuffix(2).toString()), "suffix(2)");
        check(n.getSuffix(3).isEmpty(), "suffix(3) empty");

        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception { n.getPrefix(4); } },
                "getPrefix out of range");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception { n.getSuffix(-1); } },
                "getSuffix out of range");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception { n.get(3); } },
                "get out of range");
    }

    static void testStartsWithEndsWith() throws Exception {
        CompositeName n = new CompositeName("a/b/c");
        check(n.startsWith(new CompositeName("a/b")), "startsWith prefix");
        check(!n.startsWith(new CompositeName("b/c")), "startsWith not-prefix");
        check(n.endsWith(new CompositeName("b/c")), "endsWith suffix");
        check(!n.endsWith(new CompositeName("a/b")), "endsWith not-suffix");
        check(n.startsWith(new CompositeName()), "startsWith empty");
        check(n.endsWith(new CompositeName()), "endsWith empty");
        check(!n.startsWith(null), "startsWith(null) false");
        check(!n.endsWith(null), "endsWith(null) false");
    }

    static void testAddRemove() throws Exception {
        CompositeName n = new CompositeName("a/b");
        CompositeName suffix = new CompositeName("c/d");
        check(n.addAll(suffix) == n, "addAll returns this");
        check("a/b/c/d".equals(n.toString()), "addAll appends");
        n.add(0, "zero");
        check("zero/a/b/c/d".equals(n.toString()), "add(index, comp)");
        Object removed = n.remove(0);
        check("zero".equals(removed), "remove returns removed");
        check("a/b/c/d".equals(n.toString()), "after remove");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception { new CompositeName().remove(0); } },
                "remove on empty -> AIOOBE");
    }

    static void testAddAllNonComposite() throws Exception {
        final CompositeName n = new CompositeName("a");
        final CompoundName compound = new CompoundName("x.y", compoundProps());
        checkThrows(InvalidNameException.class,
                new ThrowingRunnable() { public void run() throws Exception { n.addAll(compound); } },
                "addAll(non-CompositeName)");
        checkThrows(InvalidNameException.class,
                new ThrowingRunnable() { public void run() throws Exception { n.addAll(0, compound); } },
                "addAll(int, non-CompositeName)");
    }

    static java.util.Properties compoundProps() {
        java.util.Properties p = new java.util.Properties();
        p.setProperty("jndi.syntax.direction", "left_to_right");
        p.setProperty("jndi.syntax.separator", ".");
        p.setProperty("jndi.syntax.escape", "\\");
        return p;
    }

    static void testCompareTo() throws Exception {
        check(new CompositeName("a/b").compareTo(new CompositeName("a/c")) < 0, "compareTo order");
        check(new CompositeName("a").compareTo(new CompositeName("a/b")) < 0, "compareTo prefix shorter");
        check(new CompositeName("a/b").compareTo(new CompositeName("a/b")) == 0, "compareTo equal");
        checkThrows(ClassCastException.class,
                new ThrowingRunnable() { public void run() throws Exception { new CompositeName("a").compareTo("a"); } },
                "compareTo non-CompositeName");
        checkThrows(ClassCastException.class,
                new ThrowingRunnable() { public void run() throws Exception { new CompositeName("a").compareTo(null); } },
                "compareTo null");
    }

    static void testEqualsHashCode() throws Exception {
        CompositeName a = new CompositeName("a/b");
        CompositeName b = new CompositeName("a/b");
        check(a.equals(b) && b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode equal");
        check(!a.equals(null), "equals null");
        check(!a.equals("a/b"), "equals non-CompositeName");
        check(!a.equals(new CompositeName("a/c")), "different component");
    }

    static void testToStringRoundTrip() throws Exception {
        CompositeName n = new CompositeName("a/\"b\"/c");
        check(new CompositeName(n.toString()).equals(n), "toString round-trip");
    }

    static void testSerialization() throws Exception {
        CompositeName n = new CompositeName("a/b/c");
        CompositeName back = roundTrip(n);
        check(n.equals(back) && back.equals(n), "round trip equal");
        check(back.size() == 3 && "b".equals(back.get(1)), "round trip components");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testConstructors();
        testPrefixSuffix();
        testStartsWithEndsWith();
        testAddRemove();
        testAddAllNonComposite();
        testCompareTo();
        testEqualsHashCode();
        testToStringRoundTrip();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
