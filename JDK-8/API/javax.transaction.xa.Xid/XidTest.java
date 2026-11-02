/*
 * @test
 * @summary javax.transaction.xa.Xid conformance: interface hierarchy and key methods.
 * @run main XidTest
 */

public class XidTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.transaction.xa.Xid");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getBranchQualifier"), "declares getBranchQualifier");
        check(ms.contains("getFormatId"), "declares getFormatId");
        check(ms.contains("getGlobalTransactionId"), "declares getGlobalTransactionId");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
