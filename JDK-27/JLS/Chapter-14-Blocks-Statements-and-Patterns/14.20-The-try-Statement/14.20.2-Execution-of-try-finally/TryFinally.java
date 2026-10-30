/*
 * @test
 * @summary JLS 14.20.2: a finally block runs after the try block and any catch
 *          block; its abrupt completion replaces a pending return value or
 *          thrown value.
 * @run main TryFinally
 */

public class TryFinally {
    static int normalFinally() {
        int r = 0;
        try { r = 1; } finally { r = 2; }
        return r;
    }

    static int finallyOverridesReturn() {
        try { return 1; } finally { return 2; }
    }

    static void finallyOverridesThrow() {
        try {
            throw new RuntimeException("orig");
        } finally {
            throw new IllegalStateException("fin");
        }
    }

    public static void main(String[] args) {
        if (normalFinally() != 2) throw new AssertionError("finally order");

        if (finallyOverridesReturn() != 2)
            throw new AssertionError("finally did not override return");

        try {
            finallyOverridesThrow();
            throw new AssertionError("should have thrown");
        } catch (IllegalStateException e) {
            if (!"fin".equals(e.getMessage()))
                throw new AssertionError("finally did not override throw: " + e.getMessage());
        }

        System.out.println("PASS");
    }
}
