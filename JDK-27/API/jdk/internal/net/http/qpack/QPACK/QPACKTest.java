/*
 * @test
 * @summary jdk.internal.net.http.qpack.QPACK conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.qpack:+open
 * @run main QPACKTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QPACKTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.qpack.QPACK", false, QPACKTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.qpack.QPACK"), "name");
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
        check(methods.contains("getLogger"), "method:getLogger");
        check(methods.contains("updateDecoderSettings"), "method:updateDecoderSettings");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ALLOW_BLOCKING_ENCODING"), "field:ALLOW_BLOCKING_ENCODING");
        check(fields.contains("DECODER_BLOCKED_STREAMS"), "field:DECODER_BLOCKED_STREAMS");
        check(fields.contains("DECODER_MAX_FIELD_SECTION_SIZE"), "field:DECODER_MAX_FIELD_SECTION_SIZE");
        check(fields.contains("DECODER_MAX_TABLE_CAPACITY"), "field:DECODER_MAX_TABLE_CAPACITY");
        check(fields.contains("ENCODER_DRAINING_THRESHOLD"), "field:ENCODER_DRAINING_THRESHOLD");
        check(fields.contains("ENCODER_TABLE_CAPACITY_LIMIT"), "field:ENCODER_TABLE_CAPACITY_LIMIT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
