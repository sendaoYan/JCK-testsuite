/*
 * @test
 * @summary JLS 14.20.3 / 14.20.3.1: a basic try-with-resources statement closes
 *          non-null resources in reverse order; a close exception thrown while
 *          the try block is completing abruptly is added as a suppressed
 *          exception.
 * @run main TryWithResources
 */

public class TryWithResources {
    static StringBuilder log = new StringBuilder();

    static class R implements AutoCloseable {
        final String id;
        final boolean failClose;
        R(String id) { this(id, false); }
        R(String id, boolean failClose) { this.id = id; this.failClose = failClose; }
        public void close() {
            log.append("close").append(id).append(';');
            if (failClose) throw new IllegalStateException("close-" + id);
        }
    }

    public static void main(String[] args) {
        try (R a = new R("A"); R b = new R("B")) {
            log.append("body;");
        }
        if (!"body;closeB;closeA;".equals(log.toString()))
            throw new AssertionError("reverse close order: " + log);

        log.setLength(0);
        try (R a = null) {   // null resource is not closed
            log.append("body;");
        }
        if (!"body;".equals(log.toString()))
            throw new AssertionError("null resource closed: " + log);

        try (R a = new R("A", true)) {
            throw new RuntimeException("body-exc");
        } catch (RuntimeException e) {
            if (!"body-exc".equals(e.getMessage()))
                throw new AssertionError("primary exception: " + e.getMessage());
            if (e.getSuppressed().length != 1)
                throw new AssertionError("expected one suppressed exception");
            if (!"close-A".equals(e.getSuppressed()[0].getMessage()))
                throw new AssertionError("suppressed: " + e.getSuppressed()[0].getMessage());
        }

        System.out.println("PASS");
    }
}
