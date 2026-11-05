/*
 * @test
 * @summary org.omg.CORBA.ObjectHolder conformance: holder value field.
 * @run main ObjectHolderTest
 */

public class ObjectHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ObjectHolder h0 = new org.omg.CORBA.ObjectHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.Object v = null;
        org.omg.CORBA.ObjectHolder h1 = new org.omg.CORBA.ObjectHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(org.omg.CORBA.ObjectHolder.class),
              "ObjectHolder implements org.omg.CORBA.portable.Streamable");
        check(new org.omg.CORBA.ObjectHolder(null).value == null, "ctor stores null reference");
        check(new org.omg.CORBA.ObjectHolder()._type().kind() == org.omg.CORBA.TCKind.tk_objref,
              "ObjectHolder._type().kind() == tk_objref");
        check(new org.omg.CORBA.ObjectHolder()._type().equal(org.omg.CORBA.ObjectHelper.type()),
              "ObjectHolder._type().equal(ObjectHelper.type())");
        org.omg.CORBA.portable.OutputStream osOb = orb.create_output_stream();
        new org.omg.CORBA.ObjectHolder(null)._write(osOb);
        org.omg.CORBA.ObjectHolder whOb = new org.omg.CORBA.ObjectHolder();
        whOb._read(osOb.create_input_stream());
        check(whOb.value == null, "nil objref CDR wire round-trip reads back null");
        org.omg.CORBA.Any anyOb = orb.create_any();
        org.omg.CORBA.ObjectHelper.insert(anyOb, null);
        check(org.omg.CORBA.ObjectHelper.extract(anyOb) == null,
              "ObjectHelper.insert(null)/extract double channel returns null");
        check(anyOb.type().kind() == org.omg.CORBA.TCKind.tk_objref,
              "any kind is tk_objref after ObjectHelper.insert(null)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
