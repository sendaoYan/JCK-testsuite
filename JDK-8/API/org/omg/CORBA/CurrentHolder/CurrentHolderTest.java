/*
 * @test
 * @summary org.omg.CORBA.CurrentHolder conformance: holder value field.
 * @run main CurrentHolderTest
 */

public class CurrentHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.CurrentHolder h0 = new org.omg.CORBA.CurrentHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.Current v = null;
        org.omg.CORBA.CurrentHolder h1 = new org.omg.CORBA.CurrentHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CORBA.CurrentHolder().value == null, "default value is null");
        check(new org.omg.CORBA.CurrentHolder(null).value == null, "ctor stores null");
        check(new org.omg.CORBA.CurrentHolder()._type().kind() == org.omg.CORBA.TCKind.tk_objref,
              "CurrentHolder._type().kind() == tk_objref");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.CurrentHolder.class),
              "CurrentHolder implements org.omg.CORBA.portable.Streamable");
        try {
            org.omg.CORBA.portable.OutputStream osCh = orb.create_output_stream(); new org.omg.CORBA.CurrentHolder(null)._write(osCh);
            throw new AssertionError("CurrentHolder(null)._write (abstract valuetype unmarshalable offline, jdk8u372) should throw MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "CurrentHolder(null)._write (abstract valuetype unmarshalable offline, jdk8u372) -> MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
