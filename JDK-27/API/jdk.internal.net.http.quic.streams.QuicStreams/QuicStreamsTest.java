/*
 * @test
 * @summary jdk.internal.net.http.quic.streams.QuicStreams conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.streams:+open
 * @run main QuicStreamsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicStreamsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.streams.QuicStreams", false, QuicStreamsTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.streams.QuicStreams"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("createStream"), "method:createStream");
        check(methods.contains("isBidirectional"), "method:isBidirectional");
        check(methods.contains("isClientInitiated"), "method:isClientInitiated");
        check(methods.contains("isServerInitiated"), "method:isServerInitiated");
        check(methods.contains("isUnidirectional"), "method:isUnidirectional");
        check(methods.contains("streamType"), "method:streamType");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("SRV_MASK"), "field:SRV_MASK");
        check(fields.contains("TYPE_MASK"), "field:TYPE_MASK");
        check(fields.contains("UNI_MASK"), "field:UNI_MASK");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
