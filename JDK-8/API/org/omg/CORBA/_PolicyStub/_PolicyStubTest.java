/*
 * @test
 * @summary org.omg.CORBA._PolicyStub conformance: stub/tie class shape.
 * @run main _PolicyStubTest
 */

public class _PolicyStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA._PolicyStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.CORBA.Policy.class.isAssignableFrom(c), "implements org.omg.CORBA.Policy");

        // ---- construction without a delegate (jdk8u372 measured) ----
        org.omg.CORBA._PolicyStub stub = new org.omg.CORBA._PolicyStub();
        check(stub != null, "no-arg construction works offline");
        String[] ids = stub._ids();
        check(ids != null && ids.length == 1, "_ids() has 1 repository id");
        check("IDL:omg.org/CORBA/Policy:1.0".equals(ids[0]), "_ids()[0] == IDL:omg.org/CORBA/Policy:1.0");

        check(stub.toString().startsWith("org.omg.CORBA._PolicyStub"), "toString names the stub class");
        check(stub.toString().contains("no delegate set"), "toString mentions missing delegate");
        check(stub.equals(stub), "equals(self) true");
        check(!stub.equals(new org.omg.CORBA._PolicyStub()), "equals(other stub) false");

        try {
            stub._hash(0);
            throw new AssertionError("_hash without delegate should throw BAD_OPERATION");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "_hash(0) without delegate -> BAD_OPERATION");
        }
        try {
            stub._request("policy_type");
            throw new AssertionError("_request without delegate should throw BAD_OPERATION");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "_request without delegate -> BAD_OPERATION");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
