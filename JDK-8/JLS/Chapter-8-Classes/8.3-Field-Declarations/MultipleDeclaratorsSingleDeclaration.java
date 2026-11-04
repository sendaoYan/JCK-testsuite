/*
 * @test
 * @summary JLS 8.3 (Java SE 8): more than one field may be declared in a single
 *          FieldDeclaration; modifiers and type apply to all declarators.
 * @run main MultipleDeclaratorsSingleDeclaration
 */
public class MultipleDeclaratorsSingleDeclaration {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static int a = 1, b = 2, c;
    public static void main(String[] args) {
        check(a == 1 && b == 2, "multiple declarators share type and modifiers");
        check(c == 0, "declarator without initializer gets default value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
