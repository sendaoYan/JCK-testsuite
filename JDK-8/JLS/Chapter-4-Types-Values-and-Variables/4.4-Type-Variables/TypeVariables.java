/*
 * @test
 * @summary JLS 4.4 (Java SE 8): every type variable has a bound; if no bound is
 *          declared, Object is assumed. The members of a type variable with
 *          bound T & I1 & ... & In are the members of the intersection type.
 *          The erasure of a type variable is the erasure of the leftmost bound.
 * @run main TypeVariables
 */

class C {
    public void pub() {}
    protected void prot() {}
}

interface I {
    int val();
}

public class TypeVariables {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Members of T are those of C & I: pub, prot, val.
    <T extends C & I> int use(T t) {
        t.pub();       // from C
        t.prot();      // from C (accessible in same package)
        return t.val(); // from I
    }

    static class Impl extends C implements I {
        public int val() { return 42; }
    }

    public static void main(String[] args) {
        TypeVariables tv = new TypeVariables();
        check(tv.use(new Impl()) == 42, "type variable members come from all bounds");

        // No explicit bound: Object is assumed (T accepts any reference).
        check(noBound("x").equals("x"), "unbounded type variable assumes Object");
        check(noBound(123).equals(123), "unbounded type variable accepts any reference");

        System.out.println("PASS (" + passed + " assertions)");
    }

    static <T> T noBound(T t) { return t; }
}
