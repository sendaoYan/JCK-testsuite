/*
 * @test
 * @summary javax.security.auth.Policy conformance: abstract class hierarchy and key methods.
 * @run main PolicyTest
 */

public class PolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.security.auth.Policy");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getPermissions"), "declares getPermissions");
        check(ms.contains("getPolicy"), "declares getPolicy");
        check(ms.contains("refresh"), "declares refresh");
        check(ms.contains("setPolicy"), "declares setPolicy");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
