/*
 * @test
 * @summary JLS 6.5 (Java SE 8): a name is classified as a package name, type
 *          name, expression name, or method name; a field and a method may share
 *          a name because they live in different namespaces.
 * @run main NameResolutionTest
 */

public class NameResolutionTest {
    static int value = 1;
    int instValue = 2;

    int value(int x) { return x + 1; }   // method named value, distinct from the field

    public static void main(String[] args) {
        NameResolutionTest t = new NameResolutionTest();
        if (value != 1) throw new AssertionError("static field name");
        if (t.instValue != 2) throw new AssertionError("instance field name");
        if (t.value(4) != 5) throw new AssertionError("method name vs field name");

        if (java.lang.Math.max(3, 5) != 5) throw new AssertionError("fully qualified name");
        String s = "abc";
        if (!s.equals("abc")) throw new AssertionError("method via expression name");

        System.out.println("PASS");
    }
}
