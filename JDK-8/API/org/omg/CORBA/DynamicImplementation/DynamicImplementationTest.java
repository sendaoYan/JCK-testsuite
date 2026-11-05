/*
 * @test
 * @summary org.omg.CORBA.DynamicImplementation conformance: concrete class behavior.
 * @run main DynamicImplementationTest
 */

public class DynamicImplementationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.DynamicImplementation di = new org.omg.CORBA.DynamicImplementation();
        check(di instanceof org.omg.CORBA.portable.ObjectImpl, "extends ObjectImpl");
        check(di instanceof org.omg.CORBA.Object, "implements org.omg.CORBA.Object");
        check(!java.lang.reflect.Modifier.isAbstract(org.omg.CORBA.DynamicImplementation.class.getModifiers()),
              "concrete class (deprecated DSI skeleton)");

        // ---- inherited behavior without a delegate (jdk8u372 measured) ----
        try {
            di._ids();
            throw new AssertionError("_ids() should throw NO_IMPLEMENT");
        } catch (org.omg.CORBA.NO_IMPLEMENT expected) {
            check(true, "inherited _ids() -> NO_IMPLEMENT (unimplemented base)");
        }
        try {
            di.invoke(null);
            throw new AssertionError("base invoke should throw NO_IMPLEMENT");
        } catch (org.omg.CORBA.NO_IMPLEMENT expected) {
            check(true, "base invoke(null) -> NO_IMPLEMENT (unimplemented DSI, jdk8u372)");
        }
        try {
            di._hash(0);
            throw new AssertionError("_hash without delegate should throw BAD_OPERATION");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "_hash without delegate -> BAD_OPERATION");
        }
        check(di.toString().contains("no delegate set"), "toString reports missing delegate");
        // ServerRequest base has concrete deprecated wrappers; only ctx() is abstract
        check(java.lang.reflect.Modifier.isAbstract(org.omg.CORBA.ServerRequest.class.getModifiers()),
              "ServerRequest remains abstract");

        // subclass channel: invoke is overridable and receives the ServerRequest
        final org.omg.CORBA.ServerRequest[] got = new org.omg.CORBA.ServerRequest[1];
        org.omg.CORBA.DynamicImplementation sub = new org.omg.CORBA.DynamicImplementation() {
            @Override
            public void invoke(org.omg.CORBA.ServerRequest request) { got[0] = request; }
            @Override
            public String[] _ids() { return new String[]{"IDL:Mine:1.0"}; }
        };
        org.omg.CORBA.ServerRequest sr = new org.omg.CORBA.ServerRequest() {
            public org.omg.CORBA.Context ctx() { return null; }
        };
        sub.invoke(sr);
        check(got[0] == sr, "overridden invoke receives the exact ServerRequest");
        check(java.util.Arrays.asList(sub._ids()).contains("IDL:Mine:1.0"), "overridden _ids readback");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
