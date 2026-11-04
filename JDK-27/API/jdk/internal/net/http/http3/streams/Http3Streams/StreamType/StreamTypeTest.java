/*
 * @test
 * @summary jdk.internal.net.http.http3.streams.Http3Streams.StreamType conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.http3.streams:+open
 * @run main StreamTypeTest
 */
import java.lang.reflect.*;
import java.util.*;
public class StreamTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.http3.streams.Http3Streams$StreamType", false, StreamTypeTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.http3.streams.Http3Streams$StreamType"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("code"), "method:code");
        check(methods.contains("ofCode"), "method:ofCode");
        check(methods.contains("valueOf"), "method:valueOf");
        check(methods.contains("values"), "method:values");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("CONTROL"), "field:CONTROL");
        check(fields.contains("PUSH"), "field:PUSH");
        check(fields.contains("QPACK_DECODER"), "field:QPACK_DECODER");
        check(fields.contains("QPACK_ENCODER"), "field:QPACK_ENCODER");
        Set<String> enumConsts = new HashSet<>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) enumConsts.add(f.getName());
        check(enumConsts.contains("CONTROL"), "enum-const:CONTROL");
        check(enumConsts.contains("PUSH"), "enum-const:PUSH");
        check(enumConsts.contains("QPACK_DECODER"), "enum-const:QPACK_DECODER");
        check(enumConsts.contains("QPACK_ENCODER"), "enum-const:QPACK_ENCODER");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
