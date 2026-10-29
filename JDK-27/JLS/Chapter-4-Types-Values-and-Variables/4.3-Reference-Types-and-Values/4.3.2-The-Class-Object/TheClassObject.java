/*
 * @test
 * @summary JLS 4.3.2: class Object is a superclass of all other classes; all
 *          class and array types inherit the methods of Object. getClass
 *          returns the Class object representing the class of the object.
 * @run main TheClassObject
 */

public class TheClassObject {
    public static void main(String[] args) {
        // Object is a superclass of every other class.
        if (!(new String() instanceof Object)) throw new AssertionError("String must be an Object");
        if (!(new int[3] instanceof Object)) throw new AssertionError("array must be an Object");
        if (!(Integer.valueOf(1) instanceof Object)) throw new AssertionError("Integer must be an Object");

        // getClass returns the Class object of the object's class.
        if (!("x".getClass() == String.class)) throw new AssertionError("getClass of String");
        int[] a = new int[0];
        if (a.getClass() != int[].class) throw new AssertionError("getClass of int[]");
        if (!(new TheClassObject().getClass() == TheClassObject.class)) {
            throw new AssertionError("getClass of TheClassObject");
        }

        // All objects inherit toString, hashCode, equals, and getClass.
        Object o = new Object();
        if (o.getClass() == null) throw new AssertionError("getClass must be non-null");
        if (o.toString() == null) throw new AssertionError("toString must be non-null");
        if (!o.equals(o)) throw new AssertionError("equals must be reflexive");

        System.out.println("PASS");
    }
}
