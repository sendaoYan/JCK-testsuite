/*
 * @test
 * @summary java.rmi.activation.ActivationDesc conformance: group/class/location/restart.
 * @run main ActivationDescTest
 */

import java.rmi.activation.*;
public class ActivationDescTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        ActivationGroupID gid = new ActivationGroupID(null);
        ActivationDesc ad = new ActivationDesc(gid, "java.lang.String", "file:///tmp", null);
        check(ad.getGroupID() == gid, "group id");
        check("java.lang.String".equals(ad.getClassName()), "class name");
        check("file:///tmp".equals(ad.getLocation()), "location");
        check(!ad.getRestartMode(), "restart mode");
        check(ad.getData() == null, "data null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
