/*
 * @test
 * @summary javax.management.remote.JMXPrincipal conformance: name/equals.
 * @run main JMXPrincipalTest
 */

import javax.management.remote.*;
public class JMXPrincipalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        JMXPrincipal p = new JMXPrincipal("alice");
        check("alice".equals(p.getName()), "name");
        check(p.equals(new JMXPrincipal("alice")), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
