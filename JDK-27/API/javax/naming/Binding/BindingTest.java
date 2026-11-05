/*
 * @test
 * @summary javax.naming.Binding conformance: white-box structure + black-box behavior.
 * @run main BindingTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.Binding;
import javax.naming.NameClassPair;

public class BindingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Binding.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == javax.naming.NameClassPair.class, "extends NameClassPair");
        for (Class<?>[] sig : new Class<?>[][] {{String.class, Object.class},
                {String.class, Object.class, boolean.class}, {String.class, String.class, Object.class},
                {String.class, String.class, Object.class, boolean.class}})
            check(c.getDeclaredConstructor(sig) != null, "ctor " + java.util.Arrays.toString(sig));
        for (String m : new String[] {"getClassName", "getObject", "setObject"})
            check(method(c, m), "declares " + m);
    }
    static boolean method(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Binding b = new Binding("n", "val");
        check(b.getName().equals("n"), "name");
        check(b.getObject().equals("val"), "object");
        check(b.isRelative(), "default relative");
        check(b.getClassName().equals("java.lang.String"), "derived class name");

        b.setObject("v2");
        check(b.getObject().equals("v2"), "setObject");
        check(b.getClassName().equals("java.lang.String"), "class name updated");

        b.setObject(null);
        check(b.getObject() == null, "null object");
        check(b.getClassName() == null, "null object -> null class name");

        Binding withCls = new Binding("n", "com.example.Foo", "val");
        check(withCls.getClassName().equals("com.example.Foo"), "explicit class name");

        Binding abs = new Binding("n", "java.lang.String", "val", false);
        check(!abs.isRelative(), "isRelative flag");
        check(abs.toString().equals("(not relative)n: java.lang.String:val"), "toString");

        Binding full = new Binding("n", "com.example.Foo", "v", false);
        check(full.toString().equals("(not relative)n: com.example.Foo:v"), "full ctor toString");
        check(full.getName().equals("n") && full.getClassName().equals("com.example.Foo"), "inherited accessors");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dBindConstructorMatrix();
        dBindClassDerivation();
        dBindSetters();
        dBindToString();
        dBindSerialization();
        System.out.println("deep: Binding contracts OK");
    }

    static void dBindConstructorMatrix() {
        Binding b1 = new Binding("n", "v");
        check("n".equals(b1.getName()) && "v".equals(b1.getObject()), "(name, obj)");
        check("java.lang.String".equals(b1.getClassName()), "class derived from object");
        check(b1.isRelative(), "relative by default");
        Binding b2 = new Binding("n", "v", false);
        check(!b2.isRelative(), "relative flag false");
        Binding b3 = new Binding("n", "my.Clazz", "v");
        check("my.Clazz".equals(b3.getClassName()), "explicit class name");
        Binding b4 = new Binding("n", "my.Clazz", "v", false);
        check(!b4.isRelative() && "my.Clazz".equals(b4.getClassName()), "4-arg ctor");
        Binding bNull = new Binding("n", null);
        check(bNull.getObject() == null && bNull.getClassName() == null,
                "null object -> null class at construction");
    }

    static void dBindClassDerivation() {
        Binding b = new Binding("n", null);
        b.setObject("later");
        check("java.lang.String".equals(b.getClassName()), "class derived lazily after setObject");
        Binding prim = new Binding("n", 42);
        check("java.lang.Integer".equals(prim.getClassName()), "wrapper class derived");
        Binding typed = new Binding("n", "java.util.Date", null);
        check("java.util.Date".equals(typed.getClassName()), "explicit class survives null object");
        typed.setObject("x");
        check("java.util.Date".equals(typed.getClassName()), "explicit class not recomputed");
    }

    static void dBindSetters() {
        Binding b = new Binding("n", "v");
        b.setName("renamed");
        check("renamed".equals(b.getName()), "setName");
        b.setClassName("c.N");
        check("c.N".equals(b.getClassName()), "setClassName");
        b.setObject("w");
        check("w".equals(b.getObject()), "setObject");
        b.setRelative(false);
        check(!b.isRelative(), "setRelative");
        b.setNameInNamespace("full/ns");
        check("full/ns".equals(b.getNameInNamespace()), "setNameInNamespace inherited");
    }

    static void dBindToString() {
        Binding b = new Binding("nm", "v");
        check(b.toString().contains("nm"), "toString contains name");
        Binding notRel = new Binding("nm", "v", false);
        check(notRel.toString().contains("(not relative)"), "toString marks non-relative");
        Binding rel = new Binding("nm", "v", true);
        check(!rel.toString().contains("(not relative)"), "toString plain when relative");
    }

    static void dBindSerialization() throws Exception {
        Binding b = new Binding("nm", "my.Cls", "obj", false);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(b); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            Binding back = (Binding) oi.readObject();
            check("nm".equals(back.getName()) && "my.Cls".equals(back.getClassName()),
                    "name and class survive");
            check("obj".equals(back.getObject()) && !back.isRelative(), "object and relative flag survive");
        }
    }

}
