/*
 * @test
 * @summary javax.naming.directory.ModificationItem conformance: op/attribute.
 * @run main ModificationItemTest
 */


public class ModificationItemTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.BasicAttribute attr = new javax.naming.directory.BasicAttribute("cn", "John");
        javax.naming.directory.ModificationItem mi = new javax.naming.directory.ModificationItem(javax.naming.directory.DirContext.REPLACE_ATTRIBUTE, attr);
        check(mi.getModificationOp() == javax.naming.directory.DirContext.REPLACE_ATTRIBUTE, "getModificationOp");
        check(mi.getAttribute() == attr, "getAttribute");
        javax.naming.directory.ModificationItem add = new javax.naming.directory.ModificationItem(javax.naming.directory.DirContext.ADD_ATTRIBUTE, attr);
        check(add.getModificationOp() == javax.naming.directory.DirContext.ADD_ATTRIBUTE, "ADD op");        System.out.println("PASS (" + passed + " assertions)");
    }
}
