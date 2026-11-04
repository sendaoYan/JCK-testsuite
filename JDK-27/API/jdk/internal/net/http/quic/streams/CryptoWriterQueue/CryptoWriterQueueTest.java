/*
 * @test
 * @summary jdk.internal.net.http.quic.streams.CryptoWriterQueue conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.streams:+open
 * @run main CryptoWriterQueueTest
 */
import java.lang.reflect.*;
import java.util.*;
public class CryptoWriterQueueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.streams.CryptoWriterQueue", false, CryptoWriterQueueTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.streams.CryptoWriterQueue"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("discardReplayData"), "method:discardReplayData");
        check(methods.contains("enqueue"), "method:enqueue");
        check(methods.contains("keepReplayData"), "method:keepReplayData");
        check(methods.contains("produceFrame"), "method:produceFrame");
        check(methods.contains("remaining"), "method:remaining");
        check(methods.contains("replayData"), "method:replayData");
        check(methods.contains("reset"), "method:reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
