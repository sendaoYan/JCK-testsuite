/*
 * @test
 * @summary javax.naming.Binding conformance: white-box structure + black-box behavior.
 * @run main BindingTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.Binding;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
