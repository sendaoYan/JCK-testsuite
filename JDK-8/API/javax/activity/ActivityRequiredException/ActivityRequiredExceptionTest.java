/*
 * @test
 * @summary javax.activity.ActivityRequiredException conformance: exception hierarchy and constructors.
 * @run main ActivityRequiredExceptionTest
 */

public class ActivityRequiredExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(javax.activity.ActivityRequiredException.class), "extends RemoteException");
        javax.activity.ActivityRequiredException e = new javax.activity.ActivityRequiredException("m");
        check("m".equals(e.getMessage()), "getMessage");
        javax.activity.ActivityRequiredException e2 = new javax.activity.ActivityRequiredException(new RuntimeException("c"));
        check(e2.getCause() != null, "cause");
        javax.activity.ActivityRequiredException e3 = new javax.activity.ActivityRequiredException("m2", new RuntimeException("c2"));
        check(e3.getMessage().startsWith("m2"), "message with cause");
        check(e3.getCause() != null, "cause 2");
        check(new javax.activity.ActivityRequiredException() != null, "no-arg ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
