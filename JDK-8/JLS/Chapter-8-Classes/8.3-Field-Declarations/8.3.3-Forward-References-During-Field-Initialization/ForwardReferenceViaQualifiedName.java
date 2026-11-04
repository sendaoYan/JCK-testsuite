/*
 * @test
 * @summary JLS 8.3.3 (Java SE 8): the illegal-forward-reference restriction applies only to
 *          simple names; a qualified name (ClassName.x or this.x) bypasses the
 *          compile-time check and reads the field's default value before its
 *          initializer runs.
 * @run main ForwardReferenceViaQualifiedName
 */
public class ForwardReferenceViaQualifiedName {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static int a = ForwardReferenceViaQualifiedName.b * 2;
    static int b = 3;
    int c = this.d;
    int d = 5;
    public static void main(String[] args) {
        check(a == 0, "qualified static forward reference reads default value");
        check(b == 3, "static field later initialized");
        ForwardReferenceViaQualifiedName o = new ForwardReferenceViaQualifiedName();
        check(o.c == 0, "qualified instance forward reference reads default value");
        check(o.d == 5, "instance field later initialized");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
