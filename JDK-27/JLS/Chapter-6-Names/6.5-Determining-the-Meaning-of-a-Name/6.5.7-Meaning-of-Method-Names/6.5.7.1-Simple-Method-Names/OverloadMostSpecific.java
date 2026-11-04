/*
 * @test
 * @summary JLS 6.5.7.1 / 15.12.2 (Java SE 27): overload resolution selects the
 *          most specific applicable method; a narrower argument type selects the
 *          more specific overload, and varargs is chosen only when needed.
 * @run main OverloadMostSpecific
 */

public class OverloadMostSpecific {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    static String pick(Object o) { return "Object"; }
    static String pick(String s) { return "String"; }
    static String pick(int... xs) { return "varargs"; }

    public static void main(String[] args) {
        check(pick("x").equals("String"), "String overload more specific than Object");
        check(pick(new Integer(1)).equals("Object"), "Object overload for non-String");
        check(pick(1, 2).equals("varargs"), "varargs overload chosen when no fixed arity matches");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
