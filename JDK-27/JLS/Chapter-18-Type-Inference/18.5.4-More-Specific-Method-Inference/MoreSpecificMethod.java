/*
 * @test
 * @summary JLS 18.5.4: among applicable overloads, the more specific method is
 *          selected; a non-generic method is more specific than a generic one for
 *          a matching argument type.
 * @run main MoreSpecificMethod
 */

public class MoreSpecificMethod {
    static <T> String f(T t) { return "generic"; }
    static String f(String s) { return "String"; }

    public static void main(String[] args) {
        if (!"String".equals(f("x")))
            throw new AssertionError("non-generic overload not chosen");
        if (!"generic".equals(f(1)))
            throw new AssertionError("generic overload not chosen");

        System.out.println("PASS");
    }
}
