/*
 * @test
 * @summary JLS 4.12.2 (Java SE 8): a variable of class type T can hold a null
 *          reference or a reference to an instance of T or any subclass; a
 *          variable of interface type can hold any instance implementing it; a
 *          variable of type Object can hold any object or array.
 * @run main VariablesOfReferenceType
 */

class A {}
class B extends A {}
interface Iface {}
class Impl implements Iface {}

public class VariablesOfReferenceType {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        A a = new A();
        a = new B();               // subclass instance into superclass variable
        check(a instanceof B, "class variable holds subclass instance");
        a = null;
        check(a == null, "class variable holds null");

        Iface i = new Impl();
        check(i instanceof Impl, "interface variable holds implementing instance");

        Object o = new int[3];     // any array is an Object
        check(o instanceof int[], "Object variable holds an array");
        o = "str";
        check(o instanceof String, "Object variable holds any object");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
