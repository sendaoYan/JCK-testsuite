/*
 * @test
 * @summary java.util.logging.SocketHandler conformance: white-box structure + reflection (network-bound; no sockets opened).
 * @run main SocketHandlerTest
 */

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SocketHandler;
import java.util.logging.StreamHandler;

public class SocketHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.SocketHandler");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(StreamHandler.class.isAssignableFrom(c), "extends StreamHandler");
        check(c.getSuperclass() == StreamHandler.class, "direct superclass StreamHandler");
        check(Handler.class.isAssignableFrom(c), "is a Handler");
        check(c.getMethod("publish", LogRecord.class) != null, "publish(LogRecord)");
        check(c.getMethod("close") != null, "close()");
    }

    // ---- Part 2: black-box (reflection of network-bound constructor surface) ----
    // SocketHandler connects over TCP in its constructor, so we must not
    // instantiate it. We reflect on the constructor signatures only.
    static void testBlackBox() throws Exception {
        Class<?> c = SocketHandler.class;
        Constructor<?> noArg = c.getConstructor();
        check(noArg.getParameterTypes().length == 0, "no-arg constructor exists");
        check(noArg.getExceptionTypes().length == 1
                && noArg.getExceptionTypes()[0] == IOException.class,
                "no-arg constructor declares IOException");

        Constructor<?> hostPort = c.getConstructor(String.class, int.class);
        check(hostPort.getParameterTypes()[0] == String.class
                && hostPort.getParameterTypes()[1] == int.class,
                "(String,int) constructor parameter types");
        check(hostPort.getExceptionTypes().length == 1
                && hostPort.getExceptionTypes()[0] == IOException.class,
                "(String,int) constructor declares IOException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
