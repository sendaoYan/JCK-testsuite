/*
 * @test
 * @summary JLS 6.5.7.1 Example 6.5.7.1-1: a simple method name in an anonymous
 *          class resolves in-scope methods via the "comb rule"; f1(0) resolves
 *          to Test.f1(int).
 * @run main MethodNameCombRule
 */

class Super {
    void f2(String s) {}
    void f3(String s) {}
    void f3(int i1, int i2) {}
}

public class MethodNameCombRule {
    static int result;
    void f1(int i) { result = i + 1; }
    void f2(int i) {}
    void f3(int i) {}
    void m() {
        new Super() {
            { f1(0); }   // resolves to MethodNameCombRule.f1(int)
        };
    }
    public static void main(String[] args) {
        new MethodNameCombRule().m();
        if (result != 1) throw new AssertionError("f1 not resolved to Test.f1(int)");

        System.out.println("PASS");
    }
}
