/*
 * @test
 * @summary jdk.internal.net.http.quic.streams.QuicReceiverStream.ReceivingStreamState conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.streams:+open
 * @run main ReceivingStreamStateTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ReceivingStreamStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.streams.QuicReceiverStream$ReceivingStreamState", false, ReceivingStreamStateTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.streams.QuicReceiverStream$ReceivingStreamState"), "name");
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
        check(methods.contains("isReset"), "method:isReset");
        check(methods.contains("isTerminal"), "method:isTerminal");
        check(methods.contains("valueOf"), "method:valueOf");
        check(methods.contains("values"), "method:values");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("DATA_READ"), "field:DATA_READ");
        check(fields.contains("DATA_RECVD"), "field:DATA_RECVD");
        check(fields.contains("RECV"), "field:RECV");
        check(fields.contains("RESET_READ"), "field:RESET_READ");
        check(fields.contains("RESET_RECVD"), "field:RESET_RECVD");
        check(fields.contains("SIZE_KNOWN"), "field:SIZE_KNOWN");
        Set<String> enumConsts = new HashSet<>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) enumConsts.add(f.getName());
        check(enumConsts.contains("DATA_READ"), "enum-const:DATA_READ");
        check(enumConsts.contains("DATA_RECVD"), "enum-const:DATA_RECVD");
        check(enumConsts.contains("RECV"), "enum-const:RECV");
        check(enumConsts.contains("RESET_READ"), "enum-const:RESET_READ");
        check(enumConsts.contains("RESET_RECVD"), "enum-const:RESET_RECVD");
        check(enumConsts.contains("SIZE_KNOWN"), "enum-const:SIZE_KNOWN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
