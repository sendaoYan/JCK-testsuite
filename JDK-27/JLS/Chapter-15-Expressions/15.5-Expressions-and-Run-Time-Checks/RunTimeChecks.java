/*
 * @test
 * @summary JLS 15.5: run-time checks - an array store check throws
 *          ArrayStoreException, a cast to an incompatible reference type throws
 *          ClassCastException, and instanceof performs a check without throwing.
 * @run main RunTimeChecks
 */

public class RunTimeChecks {
    public static void main(String[] args) {
        Object[] oa = new String[1];
        try {
            oa[0] = Integer.valueOf(1);
            throw new AssertionError("expected ArrayStoreException");
        } catch (ArrayStoreException e) {
            // expected
        }

        Object o = Integer.valueOf(1);
        try {
            String s = (String) o;
            throw new AssertionError("expected ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }

        if (o instanceof String) throw new AssertionError("instanceof should be false");

        System.out.println("PASS");
    }
}
