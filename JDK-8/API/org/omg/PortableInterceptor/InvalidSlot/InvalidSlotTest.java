/*
 * @test
 * @summary org.omg.PortableInterceptor.InvalidSlot conformance: UserException hierarchy and constructors.
 * @run main InvalidSlotTest
 */

public class InvalidSlotTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableInterceptor.InvalidSlot.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableInterceptor.InvalidSlot.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableInterceptor.InvalidSlot.class), "implements IDLEntity");
        org.omg.PortableInterceptor.InvalidSlot e = new org.omg.PortableInterceptor.InvalidSlot();
        org.omg.PortableInterceptor.InvalidSlot em = new org.omg.PortableInterceptor.InvalidSlot("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        // ---- deepened (jdk8u372-b07 probed): ctor matrix / toString / helper wire ----
        check(java.lang.reflect.Modifier.isFinal(org.omg.PortableInterceptor.InvalidSlot.class.getModifiers()), "final");
        check(org.omg.PortableInterceptor.InvalidSlot.class.getDeclaredConstructors().length == 2,
              "exactly 2 ctors: () and (String)");
        check(new org.omg.PortableInterceptor.InvalidSlot().toString().equals(
                  "org.omg.PortableInterceptor.InvalidSlot: IDL:omg.org/PortableInterceptor/InvalidSlot:1.0"),
              "no-arg toString exact");
        check(new org.omg.PortableInterceptor.InvalidSlot("bad slot").toString().equals(
                  "org.omg.PortableInterceptor.InvalidSlot: IDL:omg.org/PortableInterceptor/InvalidSlot:1.0  bad slot"),
              "message ctor toString exact");
        check(new org.omg.PortableInterceptor.InvalidSlot("bad slot").getMessage().equals(
                  "IDL:omg.org/PortableInterceptor/InvalidSlot:1.0  bad slot"),
              "getMessage = repo id + two spaces + user message");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.InvalidSlotHelper.write(os, new org.omg.PortableInterceptor.InvalidSlot("is"));
        org.omg.PortableInterceptor.InvalidSlot rt = org.omg.PortableInterceptor.InvalidSlotHelper.read(os.create_input_stream());
        check(rt.getClass() == org.omg.PortableInterceptor.InvalidSlot.class, "wire round-trip preserves class");
        check(!rt.toString().endsWith("is"), "wire round-trip drops message");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.InvalidSlotHelper.insert(a, new org.omg.PortableInterceptor.InvalidSlot("isa"));
        check(org.omg.PortableInterceptor.InvalidSlotHelper.extract(a) != null, "Any insert/extract round-trip");
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_except, "Any type after insert is tk_except");
        check(org.omg.PortableInterceptor.InvalidSlotHelper.id().equals("IDL:omg.org/PortableInterceptor/InvalidSlot:1.0"),
              "helper id exact");
        check(org.omg.PortableInterceptor.InvalidSlotHelper.type().kind() == org.omg.CORBA.TCKind.tk_except,
              "helper type().kind() == tk_except");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
