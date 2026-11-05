/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.CannotProceedHolder conformance: holder value field.
 * @run main CannotProceedHolderTest
 */

public class CannotProceedHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CosNaming.NamingContextPackage.CannotProceedHolder h0 = new org.omg.CosNaming.NamingContextPackage.CannotProceedHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CosNaming.NamingContextPackage.CannotProceed v = null;
        org.omg.CosNaming.NamingContextPackage.CannotProceedHolder h1 = new org.omg.CosNaming.NamingContextPackage.CannotProceedHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (batch C, jdk8u372 probed): holder default + wire channel ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CosNaming.NamingContextPackage.CannotProceedHolder().value == null, "default ctor value == null");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CosNaming.NamingContextPackage.CannotProceedHolder.class.getModifiers()), "holder final");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.CannotProceedHolder.class), "implements Streamable");
        check(new org.omg.CosNaming.NamingContextPackage.CannotProceedHolder()._type().kind() == org.omg.CORBA.TCKind.tk_except, "_type().kind() == tk_except");
        check(new org.omg.CosNaming.NamingContextPackage.CannotProceedHolder()._type().id().equals("IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0"), "_type().id() exact repository id");
        org.omg.CosNaming.NamingContextPackage.CannotProceed dpVal = new org.omg.CosNaming.NamingContextPackage.CannotProceed("wire-reason", null, new org.omg.CosNaming.NameComponent[0]);
        org.omg.CosNaming.NamingContextPackage.CannotProceedHolder dpHv = new org.omg.CosNaming.NamingContextPackage.CannotProceedHolder(dpVal);
        check(dpHv.value == dpVal, "one-arg ctor stores reference");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        dpHv._write(dpOs);
        org.omg.CosNaming.NamingContextPackage.CannotProceedHolder dpHr = new org.omg.CosNaming.NamingContextPackage.CannotProceedHolder();
        dpHr._read(dpOs.create_input_stream());
        check(dpHr.value instanceof org.omg.CosNaming.NamingContextPackage.CannotProceed, "_write/_read round-trip preserves exception class");
        check(dpHr.value.getMessage().startsWith("IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0"), "round-trip message is repository id form (reason not carried over CDR)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
