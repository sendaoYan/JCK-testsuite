/*
 * @test
 * @summary JLS 12.8: a program exits when all non-daemon threads have terminated;
 *          a daemon thread does not keep the JVM alive.
 * @run main DaemonThreadExit
 */

public class DaemonThreadExit {
    public static void main(String[] args) {
        Thread daemon = new Thread(() -> {
            while (true) {
                try { Thread.sleep(1000); }
                catch (InterruptedException e) { break; }
            }
        });
        daemon.setDaemon(true);
        daemon.start();
        if (!daemon.isDaemon()) throw new AssertionError("daemon status");
        // Reaching the end of main lets the program exit despite the daemon thread.
        System.out.println("PASS");
    }
}
