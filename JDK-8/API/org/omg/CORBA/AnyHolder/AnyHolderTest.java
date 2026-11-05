/*
 * @test
 * @summary org.omg.CORBA.AnyHolder conformance: holder value field.
 * @run main AnyHolderTest
 */

public class AnyHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.AnyHolder h0 = new org.omg.CORBA.AnyHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.Any v = null;
        org.omg.CORBA.AnyHolder h1 = new org.omg.CORBA.AnyHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.AnyHolder.class),
              "AnyHolder implements org.omg.CORBA.portable.Streamable");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.AnyHolder.class.getModifiers()),
              "AnyHolder class is final");
        org.omg.CORBA.Any anyV = orb.create_any();
        check(new org.omg.CORBA.AnyHolder(anyV).value == anyV, "ctor stores same Any reference");
        check(new org.omg.CORBA.AnyHolder()._type().kind() == org.omg.CORBA.TCKind.tk_any,
              "AnyHolder._type().kind() == tk_any");
        check(new org.omg.CORBA.AnyHolder()._type().equal(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_any)),
              "AnyHolder._type().equal(get_primitive_tc(tk_any))");
        org.omg.CORBA.portable.OutputStream osAn = orb.create_output_stream();
        org.omg.CORBA.Any any77 = orb.create_any();
        any77.insert_long(77);
        new org.omg.CORBA.AnyHolder(any77)._write(osAn);
        org.omg.CORBA.AnyHolder whAn = new org.omg.CORBA.AnyHolder();
        whAn._read(osAn.create_input_stream());
        check(whAn.value != null && whAn.value.extract_long() == 77,
              "nested Any CDR wire round-trip (Any inside Any)");
        org.omg.CORBA.portable.OutputStream osEa = orb.create_output_stream();
        new org.omg.CORBA.AnyHolder(orb.create_any())._write(osEa);
        org.omg.CORBA.AnyHolder whEa = new org.omg.CORBA.AnyHolder();
        whEa._read(osEa.create_input_stream());
        check(whEa.value.type().kind() == org.omg.CORBA.TCKind.tk_null,
              "empty Any round-trips as tk_null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
