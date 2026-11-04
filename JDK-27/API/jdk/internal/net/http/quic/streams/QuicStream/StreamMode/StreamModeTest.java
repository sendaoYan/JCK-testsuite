/*
 * @test
 * @summary jdk.internal.net.http.quic.streams.QuicStream.StreamMode conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.streams:+open
 * @run main StreamModeTest
 */
import java.lang.reflect.*;
import java.util.*;
public class StreamModeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.streams.QuicStream$StreamMode", false, StreamModeTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.streams.QuicStream$StreamMode"), "name");
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
        check(methods.contains("isReadable"), "method:isReadable");
        check(methods.contains("isWritable"), "method:isWritable");
        check(methods.contains("valueOf"), "method:valueOf");
        check(methods.contains("values"), "method:values");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("READ_ONLY"), "field:READ_ONLY");
        check(fields.contains("READ_WRITE"), "field:READ_WRITE");
        check(fields.contains("WRITE_ONLY"), "field:WRITE_ONLY");
        Set<String> enumConsts = new HashSet<>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) enumConsts.add(f.getName());
        check(enumConsts.contains("READ_ONLY"), "enum-const:READ_ONLY");
        check(enumConsts.contains("READ_WRITE"), "enum-const:READ_WRITE");
        check(enumConsts.contains("WRITE_ONLY"), "enum-const:WRITE_ONLY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
