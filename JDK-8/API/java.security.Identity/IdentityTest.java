/*
 * @test
 * @summary java.security.Identity conformance: abstract class hierarchy and key methods.
 * @run main IdentityTest
 */

public class IdentityTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.Identity");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.security.Principal"), "implements java.security.Principal");
        check(in.contains("java.io.Serializable"), "implements java.io.Serializable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addCertificate"), "declares addCertificate");
        check(ms.contains("certificates"), "declares certificates");
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("getInfo"), "declares getInfo");
        check(ms.contains("getName"), "declares getName");
        check(ms.contains("getPublicKey"), "declares getPublicKey");
        check(ms.contains("getScope"), "declares getScope");
        check(ms.contains("hashCode"), "declares hashCode");
        check(ms.contains("identityEquals"), "declares identityEquals");
        check(ms.contains("removeCertificate"), "declares removeCertificate");
        check(ms.contains("setInfo"), "declares setInfo");
        check(ms.contains("setPublicKey"), "declares setPublicKey");
        check(ms.contains("toString"), "declares toString");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
