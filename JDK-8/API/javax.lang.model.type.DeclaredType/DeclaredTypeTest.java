/*
 * @test
 * @summary javax.lang.model.type.DeclaredType conformance: reflection shape checks.
 * @run main DeclaredTypeTest
 */

import java.lang.reflect.*;

public class DeclaredTypeTest {
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
        Class<?> c = Class.forName("javax.lang.model.type.DeclaredType");
        check(c != null, "javax.lang.model.type.DeclaredType loads");
        check(c.getName().equals("javax.lang.model.type.DeclaredType"), "binary name is javax.lang.model.type.DeclaredType");
        check(Modifier.isPublic(c.getModifiers()), "javax.lang.model.type.DeclaredType is public");
        check(c.isInterface(), "javax.lang.model.type.DeclaredType is an interface");
        check(ifaceNames(c).equals(set("javax.lang.model.type.ReferenceType")), "direct superinterfaces");
        check(declares(c, "asElement"), "declares asElement");
        check(declares(c, "getEnclosingType"), "declares getEnclosingType");
        check(declares(c, "getTypeArguments"), "declares getTypeArguments");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
