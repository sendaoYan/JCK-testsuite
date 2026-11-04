/*
 * @test
 * @summary javax.naming.NameClassPair conformance: full contract coverage.
 *          constructors (2-arg / 3-arg), name/class/relative accessors and
 *          mutators, nameInNamespace, exact toString format (relative vs not),
 *          null name/class, identity equals, and serialization round-trip.
 * @run main NameClassPairTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.naming.NameClassPair;

public class NameClassPairTest {
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

    static NameClassPair roundTrip(NameClassPair p) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(p); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (NameClassPair) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        NameClassPair p = new NameClassPair("foo", "java.lang.String");
        check("foo".equals(p.getName()), "getName");
        check("java.lang.String".equals(p.getClassName()), "getClassName");
        check(p.isRelative(), "isRelative true default");
        p.setName("bar"); p.setClassName("java.lang.Integer"); p.setRelative(false);
        check("bar".equals(p.getName()), "setName");
        check("java.lang.Integer".equals(p.getClassName()), "setClassName");
        check(!p.isRelative(), "setRelative false");
        p.setRelative(true);
        check(p.isRelative(), "setRelative true");
        p.setNameInNamespace("ns/bar");
        check("ns/bar".equals(p.getNameInNamespace()), "getNameInNamespace");
    }

    static void testConstructors() {
        NameClassPair p = new NameClassPair("foo", "C", false);
        check("foo".equals(p.getName()) && "C".equals(p.getClassName()), "ctor(String, String, boolean)");
        check(!p.isRelative(), "ctor relative flag");
        checkThrows(UnsupportedOperationException.class,
                new ThrowingRunnable() { public void run() { p.getNameInNamespace(); } },
                "getNameInNamespace default -> UnsupportedOperationException");
    }

    static void testToString() {
        NameClassPair rel = new NameClassPair("foo", "java.lang.String");
        check("foo: java.lang.String".equals(rel.toString()), "toString relative");
        NameClassPair abs = new NameClassPair("foo", "java.lang.String", false);
        check("(not relative)foo: java.lang.String".equals(abs.toString()), "toString not relative");
        NameClassPair n = new NameClassPair(null, null);
        check("null: null".equals(n.toString()), "toString null name/class");
    }

    static void testNulls() {
        NameClassPair p = new NameClassPair(null, null);
        check(p.getName() == null && p.getClassName() == null, "null ctor accessors");
        p.setName("x"); p.setClassName("y");
        check("x".equals(p.getName()) && "y".equals(p.getClassName()), "set after null");
    }

    static void testIdentityEquals() {
        NameClassPair a = new NameClassPair("a", "b");
        NameClassPair b = new NameClassPair("a", "b");
        check(!a.equals(b), "identity equals (no override)");
        check(a.equals(a), "self equals");
    }

    static void testSerialization() throws Exception {
        NameClassPair p = new NameClassPair("foo", "java.lang.String");
        p.setNameInNamespace("ns/foo");
        NameClassPair back = roundTrip(p);
        check("foo".equals(back.getName()) && "java.lang.String".equals(back.getClassName()), "round trip fields");
        check(back.isRelative(), "round trip relative");
        check("ns/foo".equals(back.getNameInNamespace()), "round trip nameInNamespace");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testConstructors();
        testToString();
        testNulls();
        testIdentityEquals();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
