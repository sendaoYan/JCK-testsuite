/*
 * @test
 * @summary JLS 4.3 (Java SE 8): reference types denote objects; the null type is
 *          a subtype of every reference type; strings may be interned so equal
 *          literals denote the same object; arrays are objects.
 * @run main ReferenceTypes
 */

public class ReferenceTypes {
    public static void main(String[] args) {
        Object o = new Object();
        if (o == null) throw new AssertionError("object not null");

        String s1 = "abc";
        String s2 = "abc";
        if (s1 != s2) throw new AssertionError("interning: identical literals share an object");
        if (s1 == new String("abc")) throw new AssertionError("new String is a distinct object");
        if (!s1.equals(new String("abc"))) throw new AssertionError("value equality");

        String n = null;
        if (n != null) throw new AssertionError("null reference");
        Object on = n;               // null is assignable to any reference type
        if (on != null) throw new AssertionError("null assignable to Object");

        int[] a = new int[3];
        if (!(a instanceof Object)) throw new AssertionError("array is an Object");
        if (a.getClass() != int[].class) throw new AssertionError("array class");

        System.out.println("PASS");
    }
}
