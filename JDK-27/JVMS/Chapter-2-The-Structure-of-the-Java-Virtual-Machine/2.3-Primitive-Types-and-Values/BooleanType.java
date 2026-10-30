/*
 * @test
 * @summary JVMS 2.3.4: the boolean type has the values true and false with default
 *          value false; boolean arrays are supported directly.
 * @run main BooleanType
 */

public class BooleanType {
    public static void main(String[] args) {
        boolean[] a = new boolean[2];   // default value is false
        if (a[0] || a[1]) throw new AssertionError("boolean default must be false");

        a[0] = true;
        if (!a[0]) throw new AssertionError("boolean true");
        if (a[1]) throw new AssertionError("boolean false");

        boolean b = a[0] && !a[1];
        if (!b) throw new AssertionError("boolean expression");

        System.out.println("PASS");
    }
}
