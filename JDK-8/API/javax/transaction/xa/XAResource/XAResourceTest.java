/*
 * @test
 * @summary javax.transaction.xa.XAResource conformance: interface hierarchy and key methods.
 * @run main XAResourceTest
 */

public class XAResourceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.transaction.xa.XAResource");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("commit"), "declares commit");
        check(ms.contains("end"), "declares end");
        check(ms.contains("forget"), "declares forget");
        check(ms.contains("getTransactionTimeout"), "declares getTransactionTimeout");
        check(ms.contains("isSameRM"), "declares isSameRM");
        check(ms.contains("prepare"), "declares prepare");
        check(ms.contains("recover"), "declares recover");
        check(ms.contains("rollback"), "declares rollback");
        check(ms.contains("setTransactionTimeout"), "declares setTransactionTimeout");
        check(ms.contains("start"), "declares start");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
