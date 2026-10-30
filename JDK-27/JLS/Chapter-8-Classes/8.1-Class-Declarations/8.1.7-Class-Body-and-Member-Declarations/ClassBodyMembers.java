/*
 * @test
 * @summary JLS 8.1.7: a class body may contain member declarations (fields,
 *          methods, classes, interfaces), instance and static initializers, and
 *          constructor declarations.
 * @run main ClassBodyMembers
 */

public class ClassBodyMembers {
    // field declaration
    int field = 1;

    // instance initializer
    { field = 2; }

    // static initializer
    static int s;
    static { s = 3; }

    // constructor declaration
    ClassBodyMembers() { field = field + 1; }

    // method declaration
    int m() { return field; }

    // member class and member interface
    class Nested {}
    interface Iface {}

    public static void main(String[] args) {
        ClassBodyMembers c = new ClassBodyMembers();
        if (c.m() != 3) throw new AssertionError("field/instance-init/constructor");
        if (s != 3) throw new AssertionError("static init");
        System.out.println("PASS");
    }
}
