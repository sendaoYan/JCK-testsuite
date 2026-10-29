/*
 * @test
 * @summary JLS 4.12.2: a variable of class type T holds null or a reference to
 *          an instance of T or any subclass of T; an interface variable holds
 *          null or any instance of a class implementing it; an Object variable
 *          holds any object; an Object[] variable holds any reference array.
 * @run main VariablesOfReferenceType
 */

interface Iface {}

class Parent {}
class Child extends Parent implements Iface {}

public class VariablesOfReferenceType {
    public static void main(String[] args) {
        // Class type: null or instance of T or subclass of T.
        Parent p = null;
        if (p != null) throw new AssertionError("null in class variable");
        p = new Parent();
        p = new Child();   // subclass instance
        if (!(p instanceof Child)) throw new AssertionError("subclass instance in Parent var");

        // Interface type: null or instance of a class implementing it.
        Iface i = null;
        i = new Child();
        if (!(i instanceof Iface)) throw new AssertionError("interface variable holds implementor");

        // Object holds null or any object (class instance or array).
        Object o = new int[3];
        if (!(o instanceof int[])) throw new AssertionError("Object holds array");
        o = "str";
        if (!(o instanceof String)) throw new AssertionError("Object holds String");

        // Object[] holds null or an array of any reference type.
        Object[] oa = new String[0];
        if (!(oa instanceof String[])) throw new AssertionError("Object[] holds String[]");
        oa = new Child[0];
        if (!(oa instanceof Child[])) throw new AssertionError("Object[] holds Child[]");

        // Array of T (reference) holds arrays of S where S <: T.
        Parent[] pa = new Child[0];
        if (!(pa instanceof Child[])) throw new AssertionError("Parent[] holds Child[]");

        System.out.println("PASS");
    }
}
