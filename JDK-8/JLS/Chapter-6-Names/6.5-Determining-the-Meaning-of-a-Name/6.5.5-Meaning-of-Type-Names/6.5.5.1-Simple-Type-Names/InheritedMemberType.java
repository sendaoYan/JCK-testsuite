/*
 * @test
 * @summary JLS 6.5.5.1 (Java SE 8): a simple type name may denote a member type
 *          (declared or inherited) of the class in whose scope it appears, so
 *          an inherited member type is reachable by its simple name.
 * @run main InheritedMemberType
 */

public class InheritedMemberType {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    static class Base { static class Inner { static int k = 11; } }
    static class Sub extends Base {
        Inner i = new Inner();   // simple type name resolves to inherited Base.Inner
    }

    public static void main(String[] args) {
        check(Sub.Inner.k == 11, "inherited member type reachable by simple name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
