/*
 * @test
 * @summary JLS 8.5.1 (Java SE 8): a member interface is implicitly static, so it can
 *          be referenced without an instance of its enclosing class.
 * @run main MemberInterfaceImplicitlyStatic
 */

import java.lang.reflect.Modifier;

public class MemberInterfaceImplicitlyStatic {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Outer {
        interface Nested {
            int v();
        }
    }

    public static void main(String[] args) {
        check(Modifier.isStatic(Outer.Nested.class.getModifiers()),
              "member interface is implicitly static");
        Outer.Nested n = new Outer.Nested() {
            public int v() { return 7; }
        };
        check(n.v() == 7, "member interface usable without enclosing instance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
