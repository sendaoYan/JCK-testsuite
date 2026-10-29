/*
 * @test
 * @summary JLS 3.10.8: the null literal is the sole value of the null type; it
 *          may be assigned or cast to any reference type; null == null.
 * @run main NullLiteral
 */

public class NullLiteral {
    public static void main(String[] args) {
        // The null literal is the null reference.
        Object o = null;
        if (o != null) throw new AssertionError("null must be the null reference");

        // Assignable to any reference type.
        String s = null;
        int[] arr = null;
        Runnable r = null;
        if (s != null || arr != null || r != null) {
            throw new AssertionError("null must be assignable to every reference type");
        }

        // Castable to any reference type.
        Object co = (Object) null;
        String cs = (String) null;
        Runnable cr = (Runnable) null;
        if (co != null || cs != null || cr != null) {
            throw new AssertionError("null must be castable to every reference type");
        }

        // The null reference is equal to itself.
        if (!(null == null)) throw new AssertionError("null == null must be true");
        if (null != null) throw new AssertionError("null != null must be false");

        System.out.println("PASS");
    }
}
