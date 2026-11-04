/*
 * @test
 * @summary javax.activity.ActivityCompletedException conformance: exception hierarchy and constructors.
 * @run main ActivityCompletedExceptionTest
 */

public class ActivityCompletedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(javax.activity.ActivityCompletedException.class), "extends RemoteException");
        javax.activity.ActivityCompletedException e = new javax.activity.ActivityCompletedException("m");
        check("m".equals(e.getMessage()), "getMessage");
        javax.activity.ActivityCompletedException e2 = new javax.activity.ActivityCompletedException(new RuntimeException("c"));
        check(e2.getCause() != null, "cause");
        javax.activity.ActivityCompletedException e3 = new javax.activity.ActivityCompletedException("m2", new RuntimeException("c2"));
        check(e3.getMessage().startsWith("m2"), "message with cause");
        check(e3.getCause() != null, "cause 2");
        check(new javax.activity.ActivityCompletedException() != null, "no-arg ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
