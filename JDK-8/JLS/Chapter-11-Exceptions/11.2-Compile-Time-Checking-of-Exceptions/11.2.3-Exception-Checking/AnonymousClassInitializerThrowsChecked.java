/*
 * @test
 * @summary JLS 11.2.3 (Java SE 8, note): no compile-time error is due if an
 *          instance initializer of an anonymous class can throw a checked
 *          exception; the compiler generates a suitable throws clause.
 * @run main AnonymousClassInitializerThrowsChecked
 */

import java.io.IOException;

public class AnonymousClassInitializerThrowsChecked {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Runnable make() throws IOException {
        Runnable r = new Runnable() {
            {
                if (System.nanoTime() < 0) throw new IOException("from initializer");
            }
            public void run() {}
        };
        return r;
    }

    public static void main(String[] args) throws IOException {
        Runnable r = make();
        check(r != null, "anonymous class with throwing initializer compiles and runs");
        r.run();
        check(true, "run() invoked without error");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
