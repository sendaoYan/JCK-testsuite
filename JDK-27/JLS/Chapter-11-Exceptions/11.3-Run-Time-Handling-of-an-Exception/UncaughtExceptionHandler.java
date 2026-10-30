/*
 * @test
 * @summary JLS 11.3: if no catch clause handles an exception, the current thread
 *          terminates after invoking its uncaught exception handler.
 * @run main UncaughtExceptionHandler
 */

public class UncaughtExceptionHandler {
    public static void main(String[] args) throws Exception {
        final RuntimeException[] caught = new RuntimeException[1];
        Thread t = new Thread(() -> { throw new RuntimeException("boom"); });
        t.setUncaughtExceptionHandler((thread, e) -> { caught[0] = (RuntimeException) e; });
        t.start();
        t.join();
        if (caught[0] == null || !"boom".equals(caught[0].getMessage()))
            throw new AssertionError("uncaught exception handler not invoked");
        System.out.println("PASS");
    }
}
