/*
 * @test
 * @summary jdk.internal.net.http.quic.streams.QuicBidiStream.BidiStreamState conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.streams:+open
 * @run main BidiStreamStateTest
 */
import java.lang.reflect.*;
import java.util.*;
public class BidiStreamStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.streams.QuicBidiStream$BidiStreamState", false, BidiStreamStateTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.streams.QuicBidiStream$BidiStreamState"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.quic.streams.QuicStream$StreamState"), "iface:jdk.internal.net.http.quic.streams.QuicStream$StreamState");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("isTerminal"), "method:isTerminal");
        check(methods.contains("valueOf"), "method:valueOf");
        check(methods.contains("values"), "method:values");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("CLOSED"), "field:CLOSED");
        check(fields.contains("HALF_CLOSED_LOCAL"), "field:HALF_CLOSED_LOCAL");
        check(fields.contains("HALF_CLOSED_REMOTE"), "field:HALF_CLOSED_REMOTE");
        check(fields.contains("IDLE"), "field:IDLE");
        check(fields.contains("OPENED"), "field:OPENED");
        Set<String> enumConsts = new HashSet<>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) enumConsts.add(f.getName());
        check(enumConsts.contains("CLOSED"), "enum-const:CLOSED");
        check(enumConsts.contains("HALF_CLOSED_LOCAL"), "enum-const:HALF_CLOSED_LOCAL");
        check(enumConsts.contains("HALF_CLOSED_REMOTE"), "enum-const:HALF_CLOSED_REMOTE");
        check(enumConsts.contains("IDLE"), "enum-const:IDLE");
        check(enumConsts.contains("OPENED"), "enum-const:OPENED");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
