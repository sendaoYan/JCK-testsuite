/*
 * @test
 * @summary JLS 6.4.1 (Java SE 8): a type declaration (here a local class)
 *          shadows another type with the same name in an enclosing scope.
 * @run main LocalClassShadowsType
 */

public class LocalClassShadowsType {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    static class Thing { int v() { return 1; } }

    public static void main(String[] args) {
        class Thing { int v() { return 2; } }   // local class shadows the member type
        check(new Thing().v() == 2, "local class shadows the enclosing member type");
        check(new LocalClassShadowsType.Thing().v() == 1, "member type reachable by qualified name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
