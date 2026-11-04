/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.util.ClassQueue conformance: behavioral construction.
 * @run main ClassQueueTest
 */

public class ClassQueueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        com.sun.org.apache.bcel.internal.util.ClassQueue q = new com.sun.org.apache.bcel.internal.util.ClassQueue();
        check(q.empty(), "initially empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
