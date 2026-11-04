/*
 * @test
 * @summary jdk.internal.net.http.frame.ErrorFrame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.frame:+open
 * @run main ErrorFrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ErrorFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.frame.ErrorFrame", false, ErrorFrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.frame.ErrorFrame"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.frame.Http2Frame"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getErrorCode"), "method:getErrorCode");
        check(methods.contains("stringForCode"), "method:stringForCode");
        check(methods.contains("toString"), "method:toString");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("CANCEL"), "field:CANCEL");
        check(fields.contains("COMPRESSION_ERROR"), "field:COMPRESSION_ERROR");
        check(fields.contains("CONNECT_ERROR"), "field:CONNECT_ERROR");
        check(fields.contains("ENHANCE_YOUR_CALM"), "field:ENHANCE_YOUR_CALM");
        check(fields.contains("FLOW_CONTROL_ERROR"), "field:FLOW_CONTROL_ERROR");
        check(fields.contains("FRAME_SIZE_ERROR"), "field:FRAME_SIZE_ERROR");
        check(fields.contains("HTTP_1_1_REQUIRED"), "field:HTTP_1_1_REQUIRED");
        check(fields.contains("INADEQUATE_SECURITY"), "field:INADEQUATE_SECURITY");
        check(fields.contains("INTERNAL_ERROR"), "field:INTERNAL_ERROR");
        check(fields.contains("NO_ERROR"), "field:NO_ERROR");
        check(fields.contains("PROTOCOL_ERROR"), "field:PROTOCOL_ERROR");
        check(fields.contains("REFUSED_STREAM"), "field:REFUSED_STREAM");
        check(fields.contains("SETTINGS_TIMEOUT"), "field:SETTINGS_TIMEOUT");
        check(fields.contains("STREAM_CLOSED"), "field:STREAM_CLOSED");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
