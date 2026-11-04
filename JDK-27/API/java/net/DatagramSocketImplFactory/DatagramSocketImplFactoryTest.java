/*
 * @test
 * @summary java.net.DatagramSocketImplFactory conformance: white-box structure + black-box behavior.
 * @run main DatagramSocketImplFactoryTest
 */

import java.lang.reflect.*;
import java.net.*;

public class DatagramSocketImplFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.DatagramSocketImplFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        check(c.getMethod("createDatagramSocketImpl") != null, "createDatagramSocketImpl");
    }

    static void testBlackBox() throws Exception {
        DatagramSocketImplFactory factory = new DatagramSocketImplFactory() {
            public DatagramSocketImpl createDatagramSocketImpl() { return null; }
        };
        check(factory.createDatagramSocketImpl() == null, "createDatagramSocketImpl returns null");
        DatagramSocketImplFactory lambda = () -> null;
        check(lambda.createDatagramSocketImpl() == null, "lambda factory");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
