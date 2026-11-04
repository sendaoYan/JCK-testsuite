/*
 * @test
 * @summary JLS 8.7: when a class is initialized, the static initializers of its
 *          superclass run before those of the class itself.
 * @run main StaticInitializerSuperclassOrder
 */

public class StaticInitializerSuperclassOrder {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Parent {
        static int log;
        static { log = 1; }
    }
    static class Child extends Parent {
        static int marker;
        static { log = log * 10 + 2; }
    }

    public static void main(String[] args) {
        // Accessing Child's own static field triggers initialization of Parent,
        // then Child.
        check(Child.marker == 0, "child's own field is read");
        check(Child.log == 12, "parent static initializer runs before child's");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
