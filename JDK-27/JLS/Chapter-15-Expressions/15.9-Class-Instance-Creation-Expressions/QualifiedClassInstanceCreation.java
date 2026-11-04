/*
 * @test
 * @summary JLS 15.9.2: a qualified class instance creation expression
 *          Primary.new Identifier() binds the new inner instance to the
 *          enclosing instance denoted by the primary.
 * @run main QualifiedClassInstanceCreation
 */

public class QualifiedClassInstanceCreation {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    int outerValue = 7;

    class Inner {
        int read() { return outerValue; }
    }

    public static void main(String[] args) {
        QualifiedClassInstanceCreation o = new QualifiedClassInstanceCreation();
        Inner i = o.new Inner();
        check(i.read() == 7, "qualified creation binds the enclosing instance");
        check(i != null, "result is non-null");
        // a different enclosing instance yields a different inner instance
        QualifiedClassInstanceCreation o2 = new QualifiedClassInstanceCreation();
        o2.outerValue = 9;
        check(o2.new Inner().read() == 9, "distinct enclosing instances are independent");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
