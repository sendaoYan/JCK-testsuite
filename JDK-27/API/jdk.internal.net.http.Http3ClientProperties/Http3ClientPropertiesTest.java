/*
 * @test
 * @summary jdk.internal.net.http.Http3ClientProperties conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main Http3ClientPropertiesTest
 */
import java.lang.reflect.*;
import java.util.*;
public class Http3ClientPropertiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.Http3ClientProperties", false, Http3ClientPropertiesTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.Http3ClientProperties"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ALTSVC_ALLOW_LOCAL_HOST_ORIGIN"), "field:ALTSVC_ALLOW_LOCAL_HOST_ORIGIN");
        check(fields.contains("MAX_DIRECT_CONNECTION_TIMEOUT"), "field:MAX_DIRECT_CONNECTION_TIMEOUT");
        check(fields.contains("MAX_HTTP3_PUSH_STREAMS"), "field:MAX_HTTP3_PUSH_STREAMS");
        check(fields.contains("MAX_STREAM_LIMIT_WAIT_TIMEOUT"), "field:MAX_STREAM_LIMIT_WAIT_TIMEOUT");
        check(fields.contains("QPACK_ALLOW_BLOCKING_ENCODING"), "field:QPACK_ALLOW_BLOCKING_ENCODING");
        check(fields.contains("QPACK_DECODER_BLOCKED_STREAMS"), "field:QPACK_DECODER_BLOCKED_STREAMS");
        check(fields.contains("QPACK_DECODER_MAX_FIELD_SECTION_SIZE"), "field:QPACK_DECODER_MAX_FIELD_SECTION_SIZE");
        check(fields.contains("QPACK_DECODER_MAX_TABLE_CAPACITY"), "field:QPACK_DECODER_MAX_TABLE_CAPACITY");
        check(fields.contains("QPACK_ENCODER_DRAINING_THRESHOLD"), "field:QPACK_ENCODER_DRAINING_THRESHOLD");
        check(fields.contains("QPACK_ENCODER_TABLE_CAPACITY_LIMIT"), "field:QPACK_ENCODER_TABLE_CAPACITY_LIMIT");
        check(fields.contains("WAIT_FOR_PENDING_CONNECT"), "field:WAIT_FOR_PENDING_CONNECT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
