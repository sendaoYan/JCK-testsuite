/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder conformance: holder value field.
 * @run main NotFoundReasonHolderTest
 */

public class NotFoundReasonHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder h0 = new org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NamingContextPackage.NotFoundReason v = null;
        org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder h1 = new org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (batch C, jdk8u372 probed): enum holder wire channel ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder().value == null, "default ctor value == null");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder.class.getModifiers()), "holder final");
        org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder dpHv =
            new org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder(org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_context);
        check(dpHv.value == org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_context, "one-arg ctor stores constant");
        check(new org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder()._type().kind() == org.omg.CORBA.TCKind.tk_enum,
              "_type().kind() == tk_enum");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        dpHv._write(dpOs);
        org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder dpHr = new org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder();
        dpHr._read(dpOs.create_input_stream());
        check(dpHr.value == org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_context, "_write/_read round-trip SAME constant");
        for (org.omg.CosNaming.NamingContextPackage.NotFoundReason dpK : new org.omg.CosNaming.NamingContextPackage.NotFoundReason[]{
                org.omg.CosNaming.NamingContextPackage.NotFoundReason.missing_node,
                org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_context,
                org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_object}) {
            org.omg.CORBA.portable.OutputStream dpOsL = orb.create_output_stream();
            new org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder(dpK)._write(dpOsL);
            org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder dpHrL = new org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder();
            dpHrL._read(dpOsL.create_input_stream());
            check(dpHrL.value == dpK, "all three NotFoundReason constants survive the wire and normalize (value " + dpK.value() + ")");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
