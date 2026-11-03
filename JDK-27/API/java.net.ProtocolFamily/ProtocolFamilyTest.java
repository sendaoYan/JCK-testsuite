/*
 * @test
 * @summary java.net.ProtocolFamily conformance: white-box structure + black-box behavior.
 * @run main ProtocolFamilyTest
 */

import java.lang.reflect.*;
import java.net.*;

public class ProtocolFamilyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ProtocolFamily");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        check(c.getMethod("name") != null, "name()");
    }

    static void testBlackBox() throws Exception {
        ProtocolFamily inet = StandardProtocolFamily.INET;
        check(inet instanceof ProtocolFamily, "INET implements ProtocolFamily");
        check(inet.name().equals("INET"), "INET name");
        check(StandardProtocolFamily.INET6 instanceof ProtocolFamily, "INET6 implements ProtocolFamily");
        check(StandardProtocolFamily.UNIX instanceof ProtocolFamily, "UNIX implements ProtocolFamily");
        check(((ProtocolFamily) StandardProtocolFamily.UNIX).name().equals("UNIX"), "UNIX name");
        check(StandardProtocolFamily.INET != StandardProtocolFamily.INET6, "distinct families");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
