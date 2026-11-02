/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicSelector conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicSelectorTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicSelectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicSelector", false, QuicSelectorTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicSelector"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.lang.AutoCloseable"), "iface:java.lang.AutoCloseable");
        check(ifaces.contains("java.lang.Runnable"), "iface:java.lang.Runnable");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("abort"), "method:abort");
        check(methods.contains("awaitTermination"), "method:awaitTermination");
        check(methods.contains("close"), "method:close");
        check(methods.contains("computeNextDeadLine"), "method:computeNextDeadLine");
        check(methods.contains("createQuicNioSelector"), "method:createQuicNioSelector");
        check(methods.contains("createQuicVirtualThreadPoller"), "method:createQuicVirtualThreadPoller");
        check(methods.contains("isSelectorThread"), "method:isSelectorThread");
        check(methods.contains("name"), "method:name");
        check(methods.contains("register"), "method:register");
        check(methods.contains("run"), "method:run");
        check(methods.contains("shutdown"), "method:shutdown");
        check(methods.contains("start"), "method:start");
        check(methods.contains("timer"), "method:timer");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("wakeup"), "method:wakeup");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("IDLE_PERIOD_MS"), "field:IDLE_PERIOD_MS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
