/*
 * @test
 * @summary java.security.CodeSigner conformance: class hierarchy and methods.
 * @run main CodeSignerTest
 */

public class CodeSignerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.CodeSigner");
        check(!c.isInterface(), "is class");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getSignerCertPath"), "declares getSignerCertPath");
        check(ms.contains("getTimestamp"), "declares getTimestamp");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
