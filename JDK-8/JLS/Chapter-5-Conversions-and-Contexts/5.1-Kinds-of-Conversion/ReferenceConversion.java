/*
 * @test
 * @summary JLS 5.1.5/5.1.6 (Java SE 8): widening reference conversion is implicit
 *          (subtype -> supertype); narrowing reference conversion requires a cast.
 * @run main ReferenceConversion
 */

public class ReferenceConversion {
    static class A {}
    static class B extends A {}

    public static void main(String[] args) {
        A a = new B();                 // widening reference conversion
        if (!(a instanceof B)) throw new AssertionError("widening reference");

        Object o = new String("hi");
        String s = (String) o;         // narrowing reference conversion
        if (!s.equals("hi")) throw new AssertionError("narrowing reference");

        System.out.println("PASS");
    }
}
