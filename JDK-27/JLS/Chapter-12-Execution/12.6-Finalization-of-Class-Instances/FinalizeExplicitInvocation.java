/*
 * @test
 * @summary JLS 12.6: a finalizer may be invoked explicitly, like any other method;
 *          Object declares a protected finalize method.
 * @run main FinalizeExplicitInvocation
 */

public class FinalizeExplicitInvocation {
    static class HasFinalizer {
        boolean finalized = false;
        protected void finalize() throws Throwable {
            super.finalize();
            finalized = true;
        }
    }

    public static void main(String[] args) throws Throwable {
        HasFinalizer h = new HasFinalizer();
        h.finalize();   // explicit invocation
        if (!h.finalized) throw new AssertionError("finalize not invoked");
        System.out.println("PASS");
    }
}
