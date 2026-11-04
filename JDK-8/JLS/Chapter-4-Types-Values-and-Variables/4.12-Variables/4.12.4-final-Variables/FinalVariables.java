/*
 * @test
 * @summary JLS 4.12.4 (Java SE 8): a final variable may only be assigned once; a
 *          final reference still allows the referenced object's state to change.
 *          A blank final has no initializer and is assigned once later. A
 *          constant variable is a final primitive or String with a constant
 *          expression initializer. Interface fields, try-with-resources
 *          resources, and multi-catch exception parameters are implicitly
 *          final. A local variable or parameter that is never reassigned is
 *          effectively final.
 * @run main FinalVariables
 */

class Mutable { int x; }

public class FinalVariables {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        final int one = 1;
        check(one == 1, "final variable assigned once");

        final int blank;
        blank = 42;
        check(blank == 42, "blank final assigned once later");

        final Mutable m = new Mutable();
        m.x = 1;               // object state may change
        m.x = 2;
        check(m.x == 2, "final reference's object state is mutable");

        final int[] a = new int[1];
        a[0] = 7;
        check(a[0] == 7, "final array components are mutable");

        final String constant = "abc";
        check("abc".equals(constant), "constant variable");

        // Effectively final: not declared final, never reassigned.
        int effectivelyFinal = 10;
        Runnable r = () -> check(effectivelyFinal == 10, "effectively final captured by lambda");
        r.run();

        // Interface fields are implicitly public static final.
        check("const".equals(Iface.CONST), "interface field is implicitly final");

        // try-with-resources resource is implicitly final.
        try (java.io.StringWriter w = new java.io.StringWriter()) {
            w.write('x');
            check("x".equals(w.toString()), "try-with-resources resource is usable");
        }

        // multi-catch exception parameter is implicitly final.
        try {
            throw new java.io.IOException("m");
        } catch (java.io.IOException | RuntimeException ex) {
            check("m".equals(ex.getMessage()), "multi-catch exception parameter");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }

    interface Iface {
        String CONST = "const";
    }
}
