/*
 * @test
 * @summary javax.naming.NameClassPair conformance: white-box structure + black-box behavior.
 * @run main NameClassPairTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.NameClassPair;

public class NameClassPairTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = NameClassPair.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(c.getDeclaredConstructor(String.class, String.class) != null, "ctor(name,cls)");
        check(c.getDeclaredConstructor(String.class, String.class, boolean.class) != null, "ctor(name,cls,relative)");
        for (String m : new String[] {"getClassName", "getName", "setName", "isRelative",
                "setRelative", "getNameInNamespace", "setNameInNamespace", "toString"})
            check(method(c, m), "declares " + m);
    }
    static boolean method(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        NameClassPair p = new NameClassPair("n", "java.lang.String");
        check(p.getName().equals("n"), "name");
        check(p.getClassName().equals("java.lang.String"), "className");
        check(p.isRelative(), "default relative");
        check(p.toString().equals("n: java.lang.String"), "toString");

        p.setRelative(false);
        check(!p.isRelative(), "setRelative");
        check(p.toString().equals("(not relative)n: java.lang.String"), "toString not relative");

        p.setName("n2");
        check(p.getName().equals("n2"), "setName");
        p.setClassName("java.lang.Integer");
        check(p.getClassName().equals("java.lang.Integer"), "setClassName");

        p.setNameInNamespace("root/n2");
        check(p.getNameInNamespace().equals("root/n2"), "getNameInNamespace after set");

        NameClassPair abs = new NameClassPair("x", "X", false);
        check(!abs.isRelative(), "3-arg ctor relative flag");
        check(abs.toString().startsWith("(not relative)"), "3-arg toString");

        try {
            new NameClassPair("n", "c").getNameInNamespace();
            throw new AssertionError("expected UOE");
        } catch (UnsupportedOperationException expected) { check(true, "UOE when not set"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dNcpConstructorMatrix();
        dNcpSetters();
        dNcpNameInNamespace();
        dNcpToString();
        dNcpSerialization();
        System.out.println("deep: NameClassPair contracts OK");
    }

    static void dNcpConstructorMatrix() {
        NameClassPair p = new NameClassPair("n", "c");
        check("n".equals(p.getName()) && "c".equals(p.getClassName()), "(name, class)");
        check(p.isRelative(), "relative by default");
        NameClassPair p2 = new NameClassPair("n", "c", false);
        check(!p2.isRelative(), "relative false");
        boolean uoe = false;
        try { p2.getNameInNamespace(); } catch (UnsupportedOperationException x) { uoe = true; }
        check(uoe, "nameInNamespace unset -> UnsupportedOperationException");
    }

    static void dNcpSetters() {
        NameClassPair p = new NameClassPair("n", "c");
        p.setName("n2");
        check("n2".equals(p.getName()), "setName");
        p.setClassName("c2");
        check("c2".equals(p.getClassName()), "setClassName");
        p.setRelative(false);
        check(!p.isRelative(), "setRelative");
    }

    static void dNcpNameInNamespace() {
        NameClassPair p = new NameClassPair("n", "c");
        boolean unset = false;
        try { p.getNameInNamespace(); } catch (UnsupportedOperationException x) { unset = true; }
        check(unset, "unset name-in-namespace -> UnsupportedOperationException");
        p.setNameInNamespace("a/b/n");
        check("a/b/n".equals(p.getNameInNamespace()), "set/read round trip");
        p.setRelative(false);
        check("a/b/n".equals(p.getNameInNamespace()), "fields independent");
    }

    static void dNcpToString() {
        NameClassPair rel = new NameClassPair("nm", "cls", true);
        check(rel.toString().contains("nm") && rel.toString().contains("cls"), "toString has name+class");
        check(!rel.toString().contains("(not relative)"), "no marker when relative");
        NameClassPair notRel = new NameClassPair("nm", "cls", false);
        check(notRel.toString().contains("(not relative)"), "marker when not relative");
    }

    static void dNcpSerialization() throws Exception {
        NameClassPair p = new NameClassPair("nm", "cls", false);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(p); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            NameClassPair back = (NameClassPair) oi.readObject();
            check("nm".equals(back.getName()) && "cls".equals(back.getClassName()), "fields survive");
            check(!back.isRelative(), "relative flag survives");
        }
    }

}
