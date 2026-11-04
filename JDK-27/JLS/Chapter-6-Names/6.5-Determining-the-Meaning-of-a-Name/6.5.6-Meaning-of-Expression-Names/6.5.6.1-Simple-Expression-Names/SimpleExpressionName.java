/*
 * @test
 * @summary JLS 6.5.6.1 (Java SE 27): a simple expression name refers to a local
 *          variable, parameter, or field in scope.
 * @run main SimpleExpressionName
 */

public class SimpleExpressionName {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    static int sField = 1;
    int iField = 2;

    int m(int param) {
        int local = param + 1;   // param and local as simple expression names
        return local;
    }

    public static void main(String[] args) {
        SimpleExpressionName t = new SimpleExpressionName();
        check(sField == 1, "static field");
        check(t.iField == 2, "instance field");
        check(t.m(4) == 5, "parameter and local variable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
