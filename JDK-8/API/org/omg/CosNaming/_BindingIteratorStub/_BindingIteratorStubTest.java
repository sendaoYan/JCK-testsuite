/*
 * @test
 * @summary org.omg.CosNaming._BindingIteratorStub conformance: stub/tie class shape.
 * @run main _BindingIteratorStubTest
 */

public class _BindingIteratorStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming._BindingIteratorStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.CosNaming.BindingIterator.class.isAssignableFrom(c), "implements org.omg.CosNaming.BindingIterator");
        // ---- deepened (batch C, jdk8u372 probed): ids table + offline local behavior ----
        org.omg.CosNaming._BindingIteratorStub stub = new org.omg.CosNaming._BindingIteratorStub();
        check(stub instanceof org.omg.CORBA.portable.ObjectImpl, "extends ObjectImpl");
        check(stub instanceof org.omg.CosNaming.BindingIterator, "implements org.omg.CosNaming.BindingIterator");
        String[] ids = stub._ids();
        check(ids != null && ids.length == 1, "_ids() returns exactly one repository id");
        check(ids[0].equals("IDL:omg.org/CosNaming/BindingIterator:1.0"), "_ids()[0] exact repository id");
        check(java.util.Arrays.asList(ids).toString().equals("[IDL:omg.org/CosNaming/BindingIterator:1.0]"), "_ids() full array exact");
        try {
            stub.next_one(new org.omg.CosNaming.BindingHolder());
            throw new AssertionError("next_one must throw offline");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "offline next_one -> org.omg.CORBA.BAD_OPERATION (no delegate; no connection needed)");
        }
        try {
            stub.next_n(3, new org.omg.CosNaming.BindingListHolder());
            throw new AssertionError("next_n must throw offline");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "offline next_n -> org.omg.CORBA.BAD_OPERATION (no delegate; no connection needed)");
        }
        try {
            stub.destroy();
            throw new AssertionError("destroy must throw offline");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "offline destroy -> org.omg.CORBA.BAD_OPERATION (no delegate; no connection needed)");
        }
        
        System.out.println("PASS (" + passed + " assertions)");
    }
}
