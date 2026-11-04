/*
 * @test
 * @summary JLS 8.4.3.6: a synchronized instance method uses the monitor of this
 *          and a synchronized static method uses the Class object monitor;
 *          synchronized methods are re-entrant and mutually exclusive.
 * @run main SynchronizedMethodMonitor
 */
public class SynchronizedMethodMonitor {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    int count;
    synchronized void instanceBump() { count++; }
    synchronized void instanceReenter() { instanceBump(); }
    static int classCount;
    static synchronized void classBump() { classCount++; }
    public static void main(String[] args) throws Exception {
        SynchronizedMethodMonitor o = new SynchronizedMethodMonitor();
        Thread[] ts = new Thread[4];
        for (int t = 0; t < ts.length; t++) {
            ts[t] = new Thread(() -> { for (int i = 0; i < 1000; i++) { o.instanceReenter(); classBump(); } });
            ts[t].start();
        }
        for (Thread t : ts) t.join();
        check(o.count == 4000, "synchronized instance methods mutually exclusive and re-entrant");
        check(classCount == 4000, "synchronized static method uses Class monitor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
