/*
 * @test
 * @summary JLS 6.4.2: a variable obscures a type in an ambiguous context, but a
 *          fully qualified name still reaches the type.
 * @run main FieldObscuresType
 */

public class FieldObscuresType {
    static String System = "obscured";   // field obscures the type java.lang.System
    public static void main(String[] args) {
        java.lang.System.out.println("via fully qualified");

        // The simple name System now denotes the field, not the type.
        if (!System.equals("obscured")) throw new AssertionError("field System");

        java.lang.System.out.println("PASS");
    }
}
