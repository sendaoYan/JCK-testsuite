/*
 * @test
 * @summary javax.naming.Reference conformance: full contract coverage.
 *          all constructors (className / factory / location / RefAddr), add /
 *          add(int) / get(String) / get(int) with wrong-type null and out-of-
 *          range ArrayIndexOutOfBoundsException, remove / clear, getAll
 *          Enumeration, clone (shallow addrs), equals/hashCode, toString, and
 *          serialization round-trip.
 * @run main ReferenceTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

public class ReferenceTest {
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

    static Reference roundTrip(Reference r) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(r); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (Reference) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        Reference r = new Reference("java.lang.String");
        check("java.lang.String".equals(r.getClassName()), "getClassName");
        check(r.getFactoryClassName() == null, "factory null default");
        r.add(new StringRefAddr("t", "v"));
        r.add(new StringRefAddr("u", "w"));
        check(r.size() == 2, "size");
        check("v".equals(((StringRefAddr) r.get("t")).getContent()), "get");
        check("v".equals(((StringRefAddr) r.get(0)).getContent()), "get(0)");
        check(r.getAll() != null, "getAll");
        Reference r2 = new Reference("java.lang.String", "com.Factory", null);
        check("com.Factory".equals(r2.getFactoryClassName()), "getFactoryClassName");
        check(r2.getFactoryClassLocation() == null, "getFactoryClassLocation null");
    }

    static void testConstructors() {
        Reference r0 = new Reference("C");
        check("C".equals(r0.getClassName()) && r0.getFactoryClassName() == null, "ctor(String)");

        RefAddr addr = new StringRefAddr("a", "1");
        Reference r1 = new Reference("C", addr);
        check(r1.size() == 1 && r1.get(0) == addr, "ctor(String, RefAddr)");

        Reference r2 = new Reference("C", "F", "L");
        check("F".equals(r2.getFactoryClassName()) && "L".equals(r2.getFactoryClassLocation()), "ctor(className, factory, location)");

        Reference r3 = new Reference("C", addr, "F", "L");
        check(r3.size() == 1 && r3.get(0) == addr
                && "F".equals(r3.getFactoryClassName())
                && "L".equals(r3.getFactoryClassLocation()), "ctor(className, addr, factory, location)");
    }

    static void testGetAddRemove() {
        Reference r = new Reference("C");
        r.add(new StringRefAddr("a", "1"));
        r.add(new StringRefAddr("b", "2"));
        check(r.get("zzz") == null, "get(missing type) null");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() { r.get(5); } },
                "get(out of range)");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() { r.get(-1); } },
                "get(negative)");

        r.add(1, new StringRefAddr("mid", "9"));
        check(r.size() == 3 && "mid".equals(r.get(1).getType()), "add(int, RefAddr)");

        Object removed = r.remove(1);
        check("mid".equals(((RefAddr) removed).getType()) && r.size() == 2, "remove returns removed");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() { r.remove(9); } },
                "remove out of range");

        r.clear();
        check(r.size() == 0, "clear");
    }

    static void testGetAll() {
        Reference r = new Reference("C");
        r.add(new StringRefAddr("a", "1"));
        r.add(new StringRefAddr("b", "2"));
        Enumeration<RefAddr> e = r.getAll();
        int n = 0;
        while (e.hasMoreElements()) { n++; e.nextElement(); }
        check(n == 2, "getAll enumerates all");
        checkThrows(java.util.NoSuchElementException.class,
                new ThrowingRunnable() { public void run() { e.nextElement(); } },
                "getAll exhausted");
    }

    static void testClone() {
        Reference r = new Reference("C");
        r.add(new StringRefAddr("a", "1"));
        Reference c = (Reference) r.clone();
        check(c != r && c.equals(r), "clone deep-equal");
        check(c.getAll() != r.getAll(), "clone copies addr list");
        check(c.get(0) == r.get(0), "clone shallow-copies addrs");
    }

    static void testEqualsHashCode() {
        Reference a = new Reference("C");
        a.add(new StringRefAddr("t", "v"));
        Reference b = new Reference("C");
        b.add(new StringRefAddr("t", "v"));
        check(a.equals(b) && b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode equal");
        Reference c = new Reference("C");
        c.add(new StringRefAddr("t", "w"));
        check(!a.equals(c), "different addr content");
        Reference d = new Reference("D");
        d.add(new StringRefAddr("t", "v"));
        check(!a.equals(d), "different className");
        check(!a.equals(null) && !a.equals("C"), "equals null/non-Reference");
    }

    static void testToString() {
        Reference r = new Reference("C");
        r.add(new StringRefAddr("t", "v"));
        String s = r.toString();
        check(s.contains("C"), "toString has className");
        check(s.contains("t") && s.contains("v"), "toString has addr");
    }

    static void testSerialization() throws Exception {
        Reference r = new Reference("C", "F", "L");
        r.add(new StringRefAddr("t", "v"));
        Reference back = roundTrip(r);
        check(back.equals(r) && r.equals(back), "round trip equal");
        check("v".equals(((StringRefAddr) back.get("t")).getContent()), "round trip addr");
        check("F".equals(back.getFactoryClassName()), "round trip factory");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testConstructors();
        testGetAddRemove();
        testGetAll();
        testClone();
        testEqualsHashCode();
        testToString();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
