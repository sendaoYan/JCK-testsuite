/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder conformance: holder value field.
 * @run main InvalidAddressHolderTest
 */

public class InvalidAddressHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder h0 = new org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NamingContextExtPackage.InvalidAddress v = null;
        org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder h1 = new org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (batch C, jdk8u372 probed): holder default + wire channel ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder().value == null, "default ctor value == null");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder.class.getModifiers()), "holder final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder.class), "implements Streamable");
        check(new org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder()._type().kind() == org.omg.CORBA.TCKind.tk_except, "_type().kind() == tk_except");
        check(new org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder()._type().id().equals("IDL:omg.org/CosNaming/NamingContextExt/InvalidAddress:1.0"), "_type().id() exact repository id");
        org.omg.CosNaming.NamingContextExtPackage.InvalidAddress dpVal = new org.omg.CosNaming.NamingContextExtPackage.InvalidAddress("wire-reason");
        org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder dpHv = new org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder(dpVal);
        check(dpHv.value == dpVal, "one-arg ctor stores reference");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        dpHv._write(dpOs);
        org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder dpHr = new org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder();
        dpHr._read(dpOs.create_input_stream());
        check(dpHr.value instanceof org.omg.CosNaming.NamingContextExtPackage.InvalidAddress, "_write/_read round-trip preserves exception class");
        check(dpHr.value.getMessage().startsWith("IDL:omg.org/CosNaming/NamingContextExt/InvalidAddress:1.0"), "round-trip message is repository id form (reason not carried over CDR)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
