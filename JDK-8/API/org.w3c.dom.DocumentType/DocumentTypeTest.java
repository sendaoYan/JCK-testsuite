/*
 * @test
 * @summary org.w3c.dom.DocumentType conformance: reflection shape checks.
 * @run main DocumentTypeTest
 */

import java.lang.reflect.*;

public class DocumentTypeTest {
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
        Class<?> c = Class.forName("org.w3c.dom.DocumentType");
        check(c != null, "org.w3c.dom.DocumentType loads");
        check(c.getName().equals("org.w3c.dom.DocumentType"), "binary name is org.w3c.dom.DocumentType");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.DocumentType is public");
        check(c.isInterface(), "org.w3c.dom.DocumentType is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Node")), "direct superinterfaces");
        check(declares(c, "getName"), "declares getName");
        check(declares(c, "getEntities"), "declares getEntities");
        check(declares(c, "getNotations"), "declares getNotations");
        check(declares(c, "getPublicId"), "declares getPublicId");
        check(declares(c, "getSystemId"), "declares getSystemId");
        check(declares(c, "getInternalSubset"), "declares getInternalSubset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
