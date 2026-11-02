/*
 * @test
 * @summary java.rmi.server.RMISocketFactory conformance: static factory state.
 * @run main RMISocketFactoryTest
 */

import java.rmi.server.RMISocketFactory;

public class RMISocketFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        RMISocketFactory def = RMISocketFactory.getDefaultSocketFactory();
        check(def != null, "default factory");
        check(RMISocketFactory.getSocketFactory() == null, "no socket factory set");
        check(RMISocketFactory.getFailureHandler() == null, "failure handler default null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
