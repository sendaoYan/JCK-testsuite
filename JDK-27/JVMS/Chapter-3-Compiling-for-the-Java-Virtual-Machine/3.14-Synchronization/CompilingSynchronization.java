/*
 * @test
 * @summary JVMS 3.14: a synchronized block compiles to monitorenter/monitorexit,
 *          and the monitor is released on both normal and abrupt (exception)
 *          completion.
 * @run main CompilingSynchronization
 */

public class CompilingSynchronization {
    static final Object lock = new Object();
    static int counter = 0;

    public static void main(String[] args) {
        // normal completion releases the monitor
        synchronized (lock) { counter++; }

        // abrupt completion also releases the monitor
        try {
            synchronized (lock) {
                counter++;
                throw new RuntimeException();
            }
        } catch (RuntimeException e) { }

        // if the monitor had not been released, this acquisition would block
        synchronized (lock) { counter++; }

        if (counter != 3) throw new AssertionError("counter == " + counter);

        System.out.println("PASS");
    }
}
