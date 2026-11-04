/*
 * @test
 * @summary com.sun.jndi.toolkit.ctx.Continuation conformance: hierarchy and declared members via reflection.
 * @run main ContinuationTest
 */

public class ContinuationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.ctx.Continuation", false, null);
        check(c.getName().equals("com.sun.jndi.toolkit.ctx.Continuation"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.naming.spi.ResolveResult"), "superclass javax.naming.spi.ResolveResult");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("fillInException"), "declares fillInException");
        check(ms.contains("isContinue"), "declares isContinue");
        check(ms.contains("setContinue"), "declares setContinue");
        check(ms.contains("setContinueAux"), "declares setContinueAux");
        check(ms.contains("setContinueLink"), "declares setContinueLink");
        check(ms.contains("setContinueNNS"), "declares setContinueNNS");
        check(ms.contains("setError"), "declares setError");
        check(ms.contains("setErrorAux"), "declares setErrorAux");
        check(ms.contains("setErrorNNS"), "declares setErrorNNS");
        check(ms.contains("setSuccess"), "declares setSuccess");
        check(ms.contains("toString"), "declares toString");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
