/*
 * @test
 * @summary org.w3c.dom.CharacterData conformance: reflection shape checks.
 * @run main CharacterDataTest
 */

import java.lang.reflect.*;

public class CharacterDataTest {
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
        Class<?> c = Class.forName("org.w3c.dom.CharacterData");
        check(c != null, "org.w3c.dom.CharacterData loads");
        check(c.getName().equals("org.w3c.dom.CharacterData"), "binary name is org.w3c.dom.CharacterData");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.CharacterData is public");
        check(c.isInterface(), "org.w3c.dom.CharacterData is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Node")), "direct superinterfaces");
        check(declares(c, "getData"), "declares getData");
        check(declares(c, "setData"), "declares setData");
        check(declares(c, "getLength"), "declares getLength");
        check(declares(c, "substringData"), "declares substringData");
        check(declares(c, "appendData"), "declares appendData");
        check(declares(c, "insertData"), "declares insertData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
