/*
 * @test
 * @summary JLS 6.4.1 / 8.3 (Java SE 8): a field declared in a subclass hides a
 *          field with the same name in a superclass; the hidden field remains
 *          reachable through a superclass reference or via super.
 * @run main FieldHidesSuperclassField
 */

public class FieldHidesSuperclassField {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    static class Base { int x = 1; }
    static class Sub extends Base {
        int x = 2;                       // hides Base.x
        int baseViaSuper() { return super.x; }
    }

    public static void main(String[] args) {
        Sub s = new Sub();
        check(s.x == 2, "subclass field hides superclass field");
        check(((Base) s).x == 1, "hidden field via superclass reference");
        check(s.baseViaSuper() == 1, "hidden field via super");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
