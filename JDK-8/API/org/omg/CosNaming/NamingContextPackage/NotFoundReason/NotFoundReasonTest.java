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
        // ---- deepened (batch C, jdk8u372 probed): 3-value enum table + boundary ----
        check(org.omg.CosNaming.NamingContextPackage.NotFoundReason._missing_node == 0
              && org.omg.CosNaming.NamingContextPackage.NotFoundReason._not_context == 1
              && org.omg.CosNaming.NamingContextPackage.NotFoundReason._not_object == 2,
              "int constants {missing_node=0, not_context=1, not_object=2}");
        check(org.omg.CosNaming.NamingContextPackage.NotFoundReason.missing_node.value() == 0, "missing_node.value()==0");
        check(org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_context.value() == 1, "not_context.value()==1");
        check(org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_object.value() == 2, "not_object.value()==2");
        check(org.omg.CosNaming.NamingContextPackage.NotFoundReason.from_int(0) == org.omg.CosNaming.NamingContextPackage.NotFoundReason.missing_node,
              "from_int(0) -> missing_node");
        check(org.omg.CosNaming.NamingContextPackage.NotFoundReason.from_int(1) == org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_context,
              "from_int(1) -> not_context");
        check(org.omg.CosNaming.NamingContextPackage.NotFoundReason.from_int(2) == org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_object,
              "from_int(2) -> not_object");
        check(org.omg.CosNaming.NamingContextPackage.NotFoundReason.missing_node != org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_object,
              "constants pairwise distinct");
        try {
            org.omg.CosNaming.NamingContextPackage.NotFoundReason.from_int(3);
            throw new AssertionError("from_int(3) must throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(3) -> org.omg.CORBA.BAD_PARAM");
        }
        try {
            org.omg.CosNaming.NamingContextPackage.NotFoundReason.from_int(-1);
            throw new AssertionError("from_int(-1) must throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "from_int(-1) -> org.omg.CORBA.BAD_PARAM");
        }
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.NotFoundReason.class),
              "NotFoundReason implements IDLEntity");
        check(org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_context.toString().startsWith("org.omg.CosNaming.NamingContextPackage.NotFoundReason@"),
              "toString not overridden (default Object pattern)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
