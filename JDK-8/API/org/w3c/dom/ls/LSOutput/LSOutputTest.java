/*
 * @test
 * @summary org.w3c.dom.ls.LSOutput conformance: reflection shape checks.
 * @run main LSOutputTest
 */

import java.lang.reflect.*;

public class LSOutputTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.ls.LSOutput");
        check(c != null, "org.w3c.dom.ls.LSOutput loads");
        check(c.getName().equals("org.w3c.dom.ls.LSOutput"), "binary name is org.w3c.dom.ls.LSOutput");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.ls.LSOutput is public");
        check(c.isInterface(), "org.w3c.dom.ls.LSOutput is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getCharacterStream"), "declares getCharacterStream");
        check(declares(c, "setCharacterStream"), "declares setCharacterStream");
        check(declares(c, "getByteStream"), "declares getByteStream");
        check(declares(c, "setByteStream"), "declares setByteStream");
        check(declares(c, "getSystemId"), "declares getSystemId");
        check(declares(c, "setSystemId"), "declares setSystemId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
