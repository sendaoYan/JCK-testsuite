/*
 * @test
 * @summary org.omg.IOP.ServiceContext conformance: value type fields and constructors.
 * @run main ServiceContextTest
 */

public class ServiceContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.ServiceContext s0 = new org.omg.IOP.ServiceContext();
        check(s0.context_id == 0, "no-arg context_id default");
        check(s0.context_data == null, "no-arg context_data default");
        org.omg.IOP.ServiceContext s1 = new org.omg.IOP.ServiceContext(7, null);
        check(s1.context_id == 7, "ctor context_id stored");
        // ---- deepened (jdk8u372-b07 probed): aliasing / defensive-copy probe ----
        byte[] srcD = {4};
        org.omg.IOP.ServiceContext alias = new org.omg.IOP.ServiceContext(11, srcD);
        srcD[0] = 55;
        check(alias.context_data[0] == 55, "ctor stores array reference without copy (source mutation visible)");
        check(alias.context_data == srcD, "context_data is reference-identical to the ctor argument");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.ServiceContextHelper.write(os, new org.omg.IOP.ServiceContext(0xEF, new byte[]{(byte) 0xAA}));
        org.omg.IOP.ServiceContext rt = org.omg.IOP.ServiceContextHelper.read(os.create_input_stream());
        check(rt.context_id == 0xEF && rt.context_data.length == 1 && rt.context_data[0] == (byte) 0xAA,
              "CDR wire round-trip id+data");
        check(java.lang.reflect.Modifier.isFinal(org.omg.IOP.ServiceContext.class.getModifiers()), "class is final");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.IOP.ServiceContext.class),
              "implements IDLEntity");
        check(org.omg.IOP.ServiceContext.class.getDeclaredFields().length == 2, "exactly two public fields (context_id, context_data)");
        boolean npe = false;
        try {
            org.omg.CORBA.portable.OutputStream osN = orb.create_output_stream();
            org.omg.IOP.ServiceContextHelper.write(osN, new org.omg.IOP.ServiceContext(1, null));
        } catch (NullPointerException npeX) { npe = true; }
        check(npe, "ServiceContextHelper.write with null context_data -> NullPointerException");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
