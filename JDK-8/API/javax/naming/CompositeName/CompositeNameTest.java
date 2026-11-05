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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dCnQuotingAndEscaping();
        dCnNullComponent();
        dCnIndexBoundaries();
        dCnPrefixSuffixTable();
        dCnCompareToOrder();
        dCnCloneAndSerialization();
        dCnEmptyNameOps();
        dCnGetAllOrder();
        System.out.println("deep: CompositeName contracts OK");
    }

    static void dCnQuotingAndEscaping() throws Exception {
        CompositeName q = new CompositeName("a/\"b/c\"");
        check(q.size() == 2, "quoted comp does not split");
        check("b/c".equals(q.get(1)), "quoted comp content");
        check("a/\"b/c\"".equals(q.toString()), "toString re-quotes when needed");
        check(new CompositeName(q.toString()).equals(q), "quoted round trip");
        CompositeName plain = new CompositeName("j/x");
        check("\"j\"/x".equals(new CompositeName("\"j\"/x").toString())
                == false, "toString drops quotes when not needed");
        check(new CompositeName("\"j\"/x").get(0).equals("j"), "quoted single comp content");
        CompositeName esc = new CompositeName("a\\/b");
        check(esc.size() == 1 && "a/b".equals(esc.get(0)), "escaped separator stays one comp");
        check(new CompositeName(esc.toString()).equals(esc), "escape round trip");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception { new CompositeName("x").get(1); } },
                "get beyond size");
        boolean npe = false;
        try { new CompositeName((String) null); } catch (NullPointerException e) { npe = true; }
        check(npe, "null string -> NullPointerException");
    }

    static void dCnNullComponent() throws Exception {
        CompositeName n = new CompositeName("a/b");
        n.add(null);
        check(n.size() == 3, "add(null) accepted");
        check(n.get(2) == null, "get returns stored null");
        boolean npe = false;
        try { n.toString(); } catch (NullPointerException e) { npe = true; }
        check(npe, "toString with null component -> NPE");
        npe = false;
        try { n.equals(new CompositeName("a/b/c")); } catch (NullPointerException e) { npe = true; }
        check(npe, "equals with null component -> NPE");
        CompositeName m = new CompositeName("a/b");
        m.add(null);
        check(m.remove(2) == null, "remove returns the null component");
        check(m.size() == 2, "size after removing null");
        boolean npeAll = false;
        try { new CompositeName("a").addAll(null); } catch (NullPointerException e) { npeAll = true; }
        check(npeAll, "addAll(null) -> NPE");
    }

    static void dCnIndexBoundaries() throws Exception {
        final CompositeName n = new CompositeName("a/b");
        check(n.add(1, "mid") == n, "add(int,comp) returns this");
        check("a/mid/b".equals(n.toString()), "inserted at index");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception { n.add(4, "x"); } },
                "add index > size -> AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception { n.add(-1, "x"); } },
                "add negative index -> AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception { n.remove(3); } },
                "remove index == size -> AIOOBE");
        CompositeName all = new CompositeName("x/y");
        check(all.addAll(1, new CompositeName("m")) == all, "addAll(int,Name) returns this");
        check("x/m/y".equals(all.toString()), "addAll inserted at index");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception { all.addAll(4, new CompositeName("z")); } },
                "addAll index > size -> AIOOBE");
        check("a/b".equals(new CompositeName("a/b").getSuffix(0).toString()), "suffix(0) is whole");
        check(new CompositeName("a/b").getSuffix(2).isEmpty(), "suffix(size) empty");
    }

    static void dCnPrefixSuffixTable() throws Exception {
        CompositeName n = new CompositeName("a/b/c");
        check(n.startsWith(new CompositeName("a/b/c")), "startsWith whole");
        check(!n.startsWith(new CompositeName("a/b/c/d")), "startsWith longer -> false");
        check(!n.startsWith(new CompositeName("b/c")), "startsWith interior -> false");
        check(!n.startsWith(new CompositeName("a/c")), "startsWith non-contiguous -> false");
        check(n.endsWith(new CompositeName("b/c")), "endsWith suffix");
        check(!n.endsWith(new CompositeName("a/b")), "endsWith prefix -> false");
        check(!n.endsWith(new CompositeName("x/c")), "endsWith partial last -> false");
        check(n.getPrefix(0).isEmpty() && n.getSuffix(3).isEmpty(), "empty prefix/suffix at bounds");
        check("a".equals(n.getPrefix(1).toString()), "prefix(1)");
        check("c".equals(n.getSuffix(2).toString()), "suffix(2)");
    }

    static void dCnCompareToOrder() throws Exception {
        check(new CompositeName("").compareTo(new CompositeName("a")) < 0, "empty < one");
        check(new CompositeName("a").compareTo(new CompositeName("")) > 0, "one > empty");
        check(new CompositeName("a/b").compareTo(new CompositeName("a/b/c")) < 0, "prefix < longer");
        check(new CompositeName("a/b/c").compareTo(new CompositeName("a/b")) > 0, "longer > prefix");
        check(new CompositeName("a/y").compareTo(new CompositeName("a/z")) < 0, "lexicographic segment");
        check(new CompositeName("a/y").compareTo(new CompositeName("b")) < 0, "first segment dominates");
        check(new CompositeName("B").compareTo(new CompositeName("a")) < 0, "case-sensitive compare");
        check(new CompositeName("a/b").compareTo(new CompositeName("a/b")) == 0, "equal compare");
    }

    static void dCnCloneAndSerialization() throws Exception {
        CompositeName n = new CompositeName("a/b/c");
        CompositeName clone = (CompositeName) n.clone();
        n.remove(0);
        check(clone.size() == 3, "clone independent of later mutation");
        check(n.size() == 2, "original mutated");
        CompositeName tricky = new CompositeName("a\\/b/\"c/d\"");
        CompositeName back = roundTrip(tricky);
        check(back.equals(tricky), "serialization round trip with escapes/quotes");
        check(back.get(0).equals("a/b") && back.get(1).equals("c/d"), "deserialized components");
        check(back.hashCode() == tricky.hashCode(), "hashCode stable across serialization");
    }

    static void dCnEmptyNameOps() throws Exception {
        CompositeName e = new CompositeName("");
        check(e.isEmpty() && e.size() == 0 && "".equals(e.toString()), "empty name basics");
        check(e.equals(new CompositeName()), "empty equals no-arg constructed");
        check(e.hashCode() == new CompositeName().hashCode(), "empty hashCode equal");
        check(e.getPrefix(0).isEmpty() && e.getSuffix(0).isEmpty(), "empty prefix/suffix");
        check(e.startsWith(e) && e.endsWith(e), "empty starts/ends with itself");
        check(e.compareTo(e) == 0, "empty compareTo");
        check(!new CompositeName("a").equals(e), "non-empty != empty");
        check(e.add("x").toString().equals("x"), "add on empty");
        check(e.remove(0).equals("x") && e.isEmpty(), "remove back to empty");
    }

    static void dCnGetAllOrder() throws Exception {
        CompositeName n = new CompositeName("p/q/r");
        java.util.List<String> comps = new java.util.ArrayList<>();
        Enumeration<String> en = n.getAll();
        while (en.hasMoreElements()) comps.add(en.nextElement());
        check(comps.size() == 3, "getAll count");
        check("p".equals(comps.get(0)) && "q".equals(comps.get(1)) && "r".equals(comps.get(2)),
                "getAll preserves order");
        Enumeration<String> en2 = n.getAll();
        check(en2.hasMoreElements() && en2.nextElement().equals("p"), "fresh enumeration restarts");
    }

}
