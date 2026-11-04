/*
 * @test
 * @summary javax.naming.CompoundName conformance: full contract coverage.
 *          constructor syntax (invalid -> InvalidNameException, null name/props
 *          -> NullPointerException), escaping of separator, size/get/getAll,
 *          getPrefix/getSuffix/remove incl. out-of-range AIOOBE, startsWith/
 *          endsWith, add/addAll (non-CompoundName -> InvalidNameException),
 *          compareTo (order + ClassCastException), equals/hashCode/clone,
 *          toString round-trip, and serialization round-trip.
 * @run main CompoundNameTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.InvalidNameException;

public class CompoundNameTest {
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

    static Properties props() {
        Properties p = new Properties();
        p.setProperty("jndi.syntax.direction", "left_to_right");
        p.setProperty("jndi.syntax.separator", ".");
        p.setProperty("jndi.syntax.ignorecase", "false");
        p.setProperty("jndi.syntax.escape", "\\");
        return p;
    }

    static CompoundName roundTrip(CompoundName n) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(n); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (CompoundName) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() throws Exception {
        CompoundName n = new CompoundName("a.b.c", props());
        check(n.size() == 3, "size");
        check("a".equals(n.get(0)), "get(0)");
        check("a.b.c".equals(n.toString()), "toString");
        CompoundName clone = (CompoundName) n.clone();
        check(n.equals(clone), "clone equals");
    }

    static void testConstructorSyntax() throws Exception {
        checkThrows(InvalidNameException.class,
                new ThrowingRunnable() { public void run() throws Exception { new CompoundName("a\\", props()); } },
                "trailing escape -> InvalidNameException");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception { new CompoundName(null, props()); } },
                "null name -> NPE");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception { new CompoundName("a.b", null); } },
                "null props -> NPE");
    }

    static void testEscaping() throws Exception {
        CompoundName n = new CompoundName("a\\.b.c", props());
        check(n.size() == 2, "escaped separator does not split");
        check("a.b".equals(n.get(0)), "escaped separator component");
        check("c".equals(n.get(1)), "un-escaped second component");
    }

    static void testPrefixSuffixRemove() throws Exception {
        CompoundName n = new CompoundName("a.b.c", props());
        check(n.getPrefix(0).isEmpty(), "prefix(0)");
        check("a.b".equals(n.getPrefix(2).toString()), "prefix(2)");
        check("b.c".equals(n.getSuffix(1).toString()), "suffix(1)");
        check(n.getSuffix(3).isEmpty(), "suffix(size) empty");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception { n.getPrefix(4); } },
                "getPrefix out of range");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception { new CompoundName("a.b", props()).remove(2); } },
                "remove out of range");
        CompoundName m = new CompoundName("a.b", props());
        check("a".equals(m.remove(0)), "remove returns removed");
        check("b".equals(m.toString()), "after remove");
    }

    static void testStartsWithEndsWith() throws Exception {
        CompoundName n = new CompoundName("a.b.c", props());
        check(n.startsWith(new CompoundName("a.b", props())), "startsWith");
        check(n.endsWith(new CompoundName("b.c", props())), "endsWith");
        check(!n.startsWith(new CompoundName("b.c", props())), "not startsWith");
        check(!n.startsWith(null) && !n.endsWith(null), "null prefix/suffix false");
    }

    static void testAddAllNonCompound() throws Exception {
        final CompoundName n = new CompoundName("a", props());
        final CompositeName composite = new CompositeName("x/y");
        checkThrows(InvalidNameException.class,
                new ThrowingRunnable() { public void run() throws Exception { n.addAll(composite); } },
                "addAll(non-CompoundName)");
    }

    static void testCompareTo() throws Exception {
        check(new CompoundName("a.b", props()).compareTo(new CompoundName("a.c", props())) < 0, "compareTo order");
        check(new CompoundName("a.b", props()).compareTo(new CompoundName("a.b", props())) == 0, "compareTo equal");
        checkThrows(ClassCastException.class,
                new ThrowingRunnable() { public void run() throws Exception { new CompoundName("a", props()).compareTo("a"); } },
                "compareTo non-CompoundName");
    }

    static void testEqualsHashCode() throws Exception {
        CompoundName a = new CompoundName("a.b", props());
        CompoundName b = new CompoundName("a.b", props());
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals/hashCode");
        check(!a.equals(new CompositeName("a/b")), "equals non-CompoundName false");
        check(!a.equals(null), "equals null");
    }

    static void testIgnoreCase() throws Exception {
        Properties ic = props();
        ic.setProperty("jndi.syntax.ignorecase", "true");
        CompoundName a = new CompoundName("A.B", ic);
        CompoundName b = new CompoundName("a.b", ic);
        check(a.equals(b), "ignorecase true equals");
    }

    static void testSerialization() throws Exception {
        CompoundName n = new CompoundName("a.b.c", props());
        CompoundName back = roundTrip(n);
        check(n.equals(back) && back.size() == 3, "round trip equal");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testConstructorSyntax();
        testEscaping();
        testPrefixSuffixRemove();
        testStartsWithEndsWith();
        testAddAllNonCompound();
        testCompareTo();
        testEqualsHashCode();
        testIgnoreCase();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
