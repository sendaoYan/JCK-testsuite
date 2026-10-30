/*
 * @test
 * @summary JLS 8.4.9 (Java SE 8): overloaded methods are distinguished by their
 *          signatures; the most specific applicable method is chosen.
 * @run main OverloadingTest
 */

public class OverloadingTest {
    static String m(int x) { return "int"; }
    static String m(long x) { return "long"; }
    static String m(double x) { return "double"; }
    static String m(String s) { return "String"; }

    public static void main(String[] args) {
        if (!m(1).equals("int")) throw new AssertionError("int overload");
        if (!m(1L).equals("long")) throw new AssertionError("long overload");
        if (!m(1.0).equals("double")) throw new AssertionError("double overload");
        if (!m("x").equals("String")) throw new AssertionError("String overload");
        if (!m('a').equals("int")) throw new AssertionError("char widens to int overload");
        System.out.println("PASS");
    }
}
