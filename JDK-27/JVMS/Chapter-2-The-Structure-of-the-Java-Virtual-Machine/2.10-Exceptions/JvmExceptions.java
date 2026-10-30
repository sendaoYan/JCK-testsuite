/*
 * @test
 * @summary JVMS 2.10: the Java Virtual Machine throws the specified precise
 *          exceptions for abnormal conditions (null access, divide by zero, array
 *          bounds/store, cast, and negative array size).
 * @run main JvmExceptions
 */

public class JvmExceptions {
    public static void main(String[] args) {
        // NullPointerException
        try {
            String s = null;
            s.length();
            throw new AssertionError("no NullPointerException");
        } catch (NullPointerException e) { }

        // ArithmeticException (integer division by zero)
        try {
            int x = 1 / 0;
            throw new AssertionError("no ArithmeticException");
        } catch (ArithmeticException e) { }

        // ArrayIndexOutOfBoundsException
        try {
            int[] a = new int[1];
            int x = a[5];
            throw new AssertionError("no ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) { }

        // ArrayStoreException
        try {
            Object[] a = new String[1];
            a[0] = Integer.valueOf(1);
            throw new AssertionError("no ArrayStoreException");
        } catch (ArrayStoreException e) { }

        // ClassCastException
        try {
            Object o = "s";
            Integer i = (Integer) o;
            throw new AssertionError("no ClassCastException");
        } catch (ClassCastException e) { }

        // NegativeArraySizeException
        try {
            int[] a = new int[-1];
            throw new AssertionError("no NegativeArraySizeException");
        } catch (NegativeArraySizeException e) { }

        System.out.println("PASS");
    }
}
