/*
 * @test
 * @summary JLS 6.3 (Java SE 8): the scope of a top-level class or interface is
 *          all type declarations in the package, so one top-level type may
 *          refer to another by simple name regardless of declaration order and
 *          across compilation units.
 * @compile Other.java TopLevelTypeScope.java
 * @run main TopLevelTypeScope
 */

public class TopLevelTypeScope {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        Other o = new Other();      // declared in another compilation unit
        check(o.value() == 42, "type in scope across compilation units");

        Later l = new Later();      // declared after this class in this unit
        check(l.value() == 7, "type declared later in same unit");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class Later {
    int value() { return 7; }
}
