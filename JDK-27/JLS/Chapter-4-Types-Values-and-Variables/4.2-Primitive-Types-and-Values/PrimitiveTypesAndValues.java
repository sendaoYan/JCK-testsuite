/*
 * @test
 * @summary JLS 4.2: primitive types are predefined and named by reserved
 *          keywords; the integral types are byte/short/int/long/char, the
 *          floating-point types are float/double, and boolean has two values.
 * @run main PrimitiveTypesAndValues
 */

public class PrimitiveTypesAndValues {
    public static void main(String[] args) {
        // The eight primitive type keywords map to the wrapper TYPE constants.
        if (Integer.TYPE != int.class) throw new AssertionError("int");
        if (Byte.TYPE != byte.class) throw new AssertionError("byte");
        if (Short.TYPE != short.class) throw new AssertionError("short");
        if (Long.TYPE != long.class) throw new AssertionError("long");
        if (Character.TYPE != char.class) throw new AssertionError("char");
        if (Float.TYPE != float.class) throw new AssertionError("float");
        if (Double.TYPE != double.class) throw new AssertionError("double");
        if (Boolean.TYPE != boolean.class) throw new AssertionError("boolean");

        // Primitive types are not reference types.
        if (int.class.equals(Object.class)) throw new AssertionError("int is not Object");

        // boolean has exactly two values.
        boolean b = true;
        b = false;
        b = !b;
        if (!b) throw new AssertionError("boolean must be true after toggle");

        System.out.println("PASS");
    }
}
