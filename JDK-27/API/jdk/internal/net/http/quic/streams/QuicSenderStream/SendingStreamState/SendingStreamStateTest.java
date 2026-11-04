/*
 * @test
 * @summary jdk.internal.net.http.quic.streams.QuicSenderStream.SendingStreamState conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.streams:+open
 * @run main SendingStreamStateTest
 */
import java.lang.reflect.*;
import java.util.*;
public class SendingStreamStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.streams.QuicSenderStream$SendingStreamState", false, SendingStreamStateTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.streams.QuicSenderStream$SendingStreamState"), "name");
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
        check(methods.contains("isSending"), "method:isSending");
        check(methods.contains("isTerminal"), "method:isTerminal");
        check(methods.contains("valueOf"), "method:valueOf");
        check(methods.contains("values"), "method:values");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("DATA_RECVD"), "field:DATA_RECVD");
        check(fields.contains("DATA_SENT"), "field:DATA_SENT");
        check(fields.contains("READY"), "field:READY");
        check(fields.contains("RESET_RECVD"), "field:RESET_RECVD");
        check(fields.contains("RESET_SENT"), "field:RESET_SENT");
        check(fields.contains("SEND"), "field:SEND");
        Set<String> enumConsts = new HashSet<>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) enumConsts.add(f.getName());
        check(enumConsts.contains("DATA_RECVD"), "enum-const:DATA_RECVD");
        check(enumConsts.contains("DATA_SENT"), "enum-const:DATA_SENT");
        check(enumConsts.contains("READY"), "enum-const:READY");
        check(enumConsts.contains("RESET_RECVD"), "enum-const:RESET_RECVD");
        check(enumConsts.contains("RESET_SENT"), "enum-const:RESET_SENT");
        check(enumConsts.contains("SEND"), "enum-const:SEND");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
