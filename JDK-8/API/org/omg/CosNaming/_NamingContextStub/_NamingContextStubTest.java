/*
 * @test
 * @summary org.omg.CosNaming._NamingContextStub conformance: stub/tie class shape.
 * @run main _NamingContextStubTest
 */

public class _NamingContextStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming._NamingContextStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.CosNaming.NamingContext.class.isAssignableFrom(c), "implements org.omg.CosNaming.NamingContext");
        // ---- deepened (batch C, jdk8u372 probed): ids table + offline local behavior ----
        org.omg.CosNaming._NamingContextStub stub = new org.omg.CosNaming._NamingContextStub();
        check(stub instanceof org.omg.CORBA.portable.ObjectImpl, "extends ObjectImpl");
        check(stub instanceof org.omg.CosNaming.NamingContext, "implements org.omg.CosNaming.NamingContext");
        String[] ids = stub._ids();
        check(ids != null && ids.length == 1, "_ids() returns exactly one repository id");
        check(ids[0].equals("IDL:omg.org/CosNaming/NamingContext:1.0"), "_ids()[0] exact repository id");
        check(java.util.Arrays.asList(ids).toString().equals("[IDL:omg.org/CosNaming/NamingContext:1.0]"), "_ids() full array exact");
        try {
            stub.list(5, new org.omg.CosNaming.BindingListHolder(), new org.omg.CosNaming.BindingIteratorHolder());
            throw new AssertionError("list must throw offline");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "offline list -> org.omg.CORBA.BAD_OPERATION (no delegate; no connection needed)");
        }
        
        System.out.println("PASS (" + passed + " assertions)");
    }
}
