/*
 * @test
 * @summary JLS 9.1.1.3 (Java SE 8): a member interface is implicitly static.
 * @run main NestedInterfaceImplicitlyStatic
 */

import java.lang.reflect.Modifier;

public class NestedInterfaceImplicitlyStatic {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Outer {
        interface Nested { int v(); }
    }

    public static void main(String[] args) {
        check(Modifier.isStatic(Outer.Nested.class.getModifiers()),
              "member interface is implicitly static");
        Outer.Nested n = new Outer.Nested() {
            public int v() { return 7; }
        };
        check(n.v() == 7, "usable without enclosing instance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
