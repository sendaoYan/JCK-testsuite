/*
 * @test
 * @summary JLS 10.5 (Java SE 8): storing into an array a value whose type is not
 *          assignable to the component type throws ArrayStoreException.
 * @run main ArrayStoreExceptionTest
 */

public class ArrayStoreExceptionTest {
    public static void main(String[] args) {
        Object[] oa = new String[2];
        boolean caught = false;
        try {
            oa[0] = new Integer(5);   // Integer is not assignable to String
        } catch (ArrayStoreException expected) {
            caught = true;
        }
        if (!caught) throw new AssertionError("ArrayStoreException expected");

        Object[] oa2 = new Object[1];
        oa2[0] = new String[1];       // String[] is assignable to Object
        if (!(oa2[0] instanceof String[])) throw new AssertionError("array store ok");

        System.out.println("PASS");
    }
}
