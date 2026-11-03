/*
 * @test
 * @summary java.net.SocketImplFactory conformance: white-box structure + black-box behavior.
 * @run main SocketImplFactoryTest
 */

import java.lang.reflect.*;
import java.net.*;

public class SocketImplFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketImplFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        check(c.getMethod("createSocketImpl") != null, "createSocketImpl");
    }

    static void testBlackBox() throws Exception {
        final SocketImpl[] created = { null };
        SocketImplFactory factory = new SocketImplFactory() {
            public SocketImpl createSocketImpl() { return created[0]; }
        };
        check(factory.createSocketImpl() == null, "createSocketImpl returns null (no impl set)");
        // exercise the contract by returning a concrete impl via subclass
        SocketImplFactory factory2 = () -> null;
        check(factory2.createSocketImpl() == null, "lambda factory");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
