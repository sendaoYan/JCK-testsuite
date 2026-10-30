/*
 * @test
 * @summary JLS 14.18: a throw statement throws its (non-null) value; if the
 *          expression evaluates to null a NullPointerException is thrown instead.
 * @run main ThrowStatement
 */

public class ThrowStatement {
    public static void main(String[] args) {
        try {
            throw new IllegalStateException("boom");
        } catch (IllegalStateException e) {
            if (!"boom".equals(e.getMessage()))
                throw new AssertionError("thrown value not preserved");
        }

        try {
            throwNull();
            throw new AssertionError("throw null did not throw");
        } catch (NullPointerException e) {
            // expected
        }

        System.out.println("PASS");
    }

    static void throwNull() {
        throw null;
    }
}
