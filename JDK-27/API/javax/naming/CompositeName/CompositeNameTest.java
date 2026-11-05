/*
 * @test
 * @summary javax.naming.CompositeName conformance: white-box structure + black-box behavior.
 * @run main CompositeNameTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;

public class CompositeNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = CompositeName.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Name.class.isAssignableFrom(c), "implements Name");
        check(SerializableOK(c), "Serializable");
        check(c.getDeclaredConstructor(String.class).getExceptionTypes()[0] == InvalidNameException.class,
                "String ctor throws InvalidNameException");
        for (String m : new String[] {"toString", "equals", "hashCode", "compareTo", "clone",
                "size", "isEmpty", "getAll", "get", "getPrefix", "getSuffix", "startsWith",
                "endsWith", "addAll", "add", "remove"})
            check(hasMethod(c, m), "declares/overrides " + m);
    }
    static boolean SerializableOK(Class<?> c) { return java.io.Serializable.class.isAssignableFrom(c); }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // parsing: separators, escapes, quotes
        CompositeName empty = new CompositeName("");
        check(empty.size() == 0 && empty.isEmpty() && empty.toString().isEmpty(), "empty parse");
        check(new CompositeName().size() == 0, "default ctor empty");

        CompositeName esc = new CompositeName("a\\/b/c");
        check(esc.size() == 2, "escaped separator kept in one comp");
        check(esc.get(0).equals("a/b"), "unescaped value");
        check(esc.toString().equals("\"a/b\"/c"), "toString re-quotes");

        CompositeName q = new CompositeName("\"a.b\"/c");
        check(q.size() == 2 && q.get(0).equals("a.b"), "quoted comp keeps separator chars");

        try { new CompositeName("a\\"); throw new AssertionError("trailing escape"); }
        catch (InvalidNameException e) { check(e.getMessage().equals("a\\: unescaped \\ at end of component"), "exact msg"); }
        try { new CompositeName("\"x"); throw new AssertionError("unterminated quote"); }
        catch (InvalidNameException e) { check(e.getMessage().equals("\"x: no close quote"), "exact msg"); }

        // mutation chains
        CompositeName n = new CompositeName("a/b/c");
        check(n.add("d") == n, "add returns this");
        check(n.size() == 4, "size after add");
        n.add(1, "X");
        check(n.get(1).equals("X") && n.size() == 5, "insert at index");
        check(n.remove(1).equals("X"), "remove returns comp");
        n.addAll(new CompositeName("e/f"));
        check(n.size() == 6 && n.get(5).equals("f"), "addAll append");
        n.addAll(0, new CompositeName("s"));
        check(n.get(0).equals("s"), "addAll at 0");

        // prefixes / suffixes / match
        CompositeName m = new CompositeName("a/b/c");
        check(m.getPrefix(0).isEmpty(), "empty prefix");
        check(m.getPrefix(3).toString().equals("a/b/c"), "full prefix");
        check(m.getSuffix(0).toString().equals("a/b/c"), "full suffix");
        check(m.getSuffix(3).isEmpty(), "empty suffix");
        check(m.startsWith(new CompositeName()) && m.endsWith(new CompositeName()), "empty name matches");
        check(!m.startsWith(new CompositeName("c")), "wrong head");

        // equals/hashCode/compareTo/clone
        CompositeName m2 = new CompositeName("a/b/c");
        check(m.equals(m2) && m2.equals(m), "equals symmetric");
        check(m.hashCode() == m2.hashCode(), "hashCode contract");
        check(!m.equals(new CompositeName("a/b")), "different size not equal");
        check(m.compareTo(m2) == 0, "compareTo 0");
        check(new CompositeName("a/b").compareTo(new CompositeName("a/c")) < 0, "lexicographic");
        check(new CompositeName("a").compareTo(new CompositeName("a/b")) < 0, "prefix sorts first");
        try { m.compareTo("a/b/c"); throw new AssertionError("compareTo(String)"); }
        catch (ClassCastException e) { check(e.getMessage().equals("Not a CompositeName"), "CCE msg"); }

        CompositeName cl = (CompositeName) m.clone();
        check(cl.equals(m) && cl != m, "clone equals, distinct");
        cl.add("z");
        check(m.size() == 3, "clone is independent");

        // serialization round trip
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(m); }
        Object rt = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(rt.equals(m), "serialization round trip");

        Enumeration<String> en = m.getAll();
        int cnt = 0;
        while (en.hasMoreElements()) { en.nextElement(); cnt++; }
        check(cnt == 3, "getAll count");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static CompositeName roundTrip(CompositeName n) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(n); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (CompositeName) ois.readObject();
        }
    }


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
