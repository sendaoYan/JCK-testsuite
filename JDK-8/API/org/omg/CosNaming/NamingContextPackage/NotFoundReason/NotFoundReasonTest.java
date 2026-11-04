/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.NotFoundReason conformance: enumeration values.
 * @run main NotFoundReasonTest
 */

public class NotFoundReasonTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NamingContextPackage.NotFoundReason.missing_node != null, "missing_node constant non-null");
        check(org.omg.CosNaming.NamingContextPackage.NotFoundReason.missing_node.value() == org.omg.CosNaming.NamingContextPackage.NotFoundReason._missing_node, "value() matches int constant");
        check(org.omg.CosNaming.NamingContextPackage.NotFoundReason.from_int(org.omg.CosNaming.NamingContextPackage.NotFoundReason._missing_node) == org.omg.CosNaming.NamingContextPackage.NotFoundReason.missing_node, "from_int round-trips");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
