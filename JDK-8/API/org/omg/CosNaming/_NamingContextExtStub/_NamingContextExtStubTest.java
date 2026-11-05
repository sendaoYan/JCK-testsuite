/*
 * @test
 * @summary org.omg.CosNaming._NamingContextExtStub conformance: stub/tie class shape.
 * @run main _NamingContextExtStubTest
 */

public class _NamingContextExtStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming._NamingContextExtStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.CosNaming.NamingContextExt.class.isAssignableFrom(c), "implements org.omg.CosNaming.NamingContextExt");
        // ---- deepened (batch C, jdk8u372 probed): ids table + offline local behavior ----
        org.omg.CosNaming._NamingContextExtStub stub = new org.omg.CosNaming._NamingContextExtStub();
        check(stub instanceof org.omg.CORBA.portable.ObjectImpl, "extends ObjectImpl");
        check(stub instanceof org.omg.CosNaming.NamingContextExt, "implements org.omg.CosNaming.NamingContextExt");
        String[] ids = stub._ids();
        check(ids != null && ids.length == 2, "_ids() returns exactly two repository ids (Ext + inherited NamingContext)");
        check(ids[0].equals("IDL:omg.org/CosNaming/NamingContextExt:1.0"), "_ids()[0] exact repository id");
        check(java.util.Arrays.asList(ids).toString().equals("[IDL:omg.org/CosNaming/NamingContextExt:1.0, IDL:omg.org/CosNaming/NamingContext:1.0]"), "_ids() full array exact (derived id first, base id second)");
        try {
            stub.to_string(new org.omg.CosNaming.NameComponent[]{new org.omg.CosNaming.NameComponent("a", "b")});
            throw new AssertionError("to_string must throw offline");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "offline to_string -> org.omg.CORBA.BAD_OPERATION (no delegate; no connection needed)");
        }
        try {
            stub.to_name("a.b");
            throw new AssertionError("to_name must throw offline");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "offline to_name -> org.omg.CORBA.BAD_OPERATION (no delegate; no connection needed)");
        }
        
        System.out.println("PASS (" + passed + " assertions)");
    }
}
