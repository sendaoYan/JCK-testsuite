/*
 * @test
 * @summary java.security.SignatureSpi conformance: abstract class hierarchy and key methods.
 * @run main SignatureSpiTest
 */

public class SignatureSpiTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.SignatureSpi");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("clone"), "declares clone");
        check(ms.contains("engineGetParameter"), "declares engineGetParameter");
        check(ms.contains("engineGetParameters"), "declares engineGetParameters");
        check(ms.contains("engineInitSign"), "declares engineInitSign");
        check(ms.contains("engineInitVerify"), "declares engineInitVerify");
        check(ms.contains("engineSetParameter"), "declares engineSetParameter");
        check(ms.contains("engineSign"), "declares engineSign");
        check(ms.contains("engineUpdate"), "declares engineUpdate");
        check(ms.contains("engineVerify"), "declares engineVerify");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
