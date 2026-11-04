/*
 * @test
 * @summary jdk.internal.net.http.http3.streams.Http3Streams conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.http3.streams:+open
 * @run main Http3StreamsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class Http3StreamsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.http3.streams.Http3Streams", false, Http3StreamsTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.http3.streams.Http3Streams"), "name");
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
        check(methods.contains("debugErrorCode"), "method:debugErrorCode");
        check(methods.contains("errorCodeAsString"), "method:errorCodeAsString");
        check(methods.contains("isReserved"), "method:isReserved");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("CONTROL_STREAM_CODE"), "field:CONTROL_STREAM_CODE");
        check(fields.contains("PUSH_STREAM_CODE"), "field:PUSH_STREAM_CODE");
        check(fields.contains("QPACK_DECODER_STREAM_CODE"), "field:QPACK_DECODER_STREAM_CODE");
        check(fields.contains("QPACK_ENCODER_STREAM_CODE"), "field:QPACK_ENCODER_STREAM_CODE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
