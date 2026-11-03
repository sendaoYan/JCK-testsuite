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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
