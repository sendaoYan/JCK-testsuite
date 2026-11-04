/*
 * @test
 * @summary javax.naming.spi.DirectoryManager conformance: getObjectInstance.
 * @run main DirectoryManagerTest
 */


public class DirectoryManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.Reference ref = new javax.naming.Reference("java.lang.String");
        Object o = javax.naming.spi.DirectoryManager.getObjectInstance(ref, null, null, null);
        check(o != null, "getObjectInstance returns ref info");        System.out.println("PASS (" + passed + " assertions)");
    }
}
