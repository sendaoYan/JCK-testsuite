/*
 * @test
 * @summary JLS 14.20.3.2: an extended try-with-resources statement lets its
 *          catch clauses catch exceptions from automatic resource close (the
 *          resources are placed inside the try statement by translation).
 * @run main ExtendedTryWithResources
 */

public class ExtendedTryWithResources {
    static class R implements AutoCloseable {
        public void close() {
            throw new IllegalStateException("close-fail");
        }
    }

    public static void main(String[] args) {
        boolean caught = false;
        try (R r = new R()) {
            // body completes normally; close throws
        } catch (IllegalStateException e) {
            caught = "close-fail".equals(e.getMessage());
        }
        if (!caught) throw new AssertionError("extended catch did not catch close exception");

        System.out.println("PASS");
    }
}
