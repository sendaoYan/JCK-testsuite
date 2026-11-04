/*
 * @test
 * @summary jdk.jfr.FlightRecorderPermission conformance: white-box structure + black-box permission semantics.
 * @run main FlightRecorderPermissionTest
 */

import java.lang.reflect.*;
import jdk.jfr.FlightRecorderPermission;

public class FlightRecorderPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.FlightRecorderPermission");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == java.security.BasicPermission.class, "extends BasicPermission");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        FlightRecorderPermission p = new FlightRecorderPermission("accessFlightRecorder");
        check("accessFlightRecorder".equals(p.getName()), "getName");
        check(p.getActions().length() == 0, "empty actions");
        check(p.implies(new FlightRecorderPermission("accessFlightRecorder")), "implies same");
        check(!p.implies(new FlightRecorderPermission("registerEvent")), "not implies other");
        check(p.equals(new FlightRecorderPermission("accessFlightRecorder")), "equals");
        check(p.hashCode() == new FlightRecorderPermission("accessFlightRecorder").hashCode(), "hashCode consistent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
