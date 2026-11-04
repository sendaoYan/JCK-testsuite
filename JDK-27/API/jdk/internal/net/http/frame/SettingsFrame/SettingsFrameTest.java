/*
 * @test
 * @summary jdk.internal.net.http.frame.SettingsFrame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.frame:+open
 * @run main SettingsFrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class SettingsFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.frame.SettingsFrame", false, SettingsFrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.frame.SettingsFrame"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.frame.Http2Frame"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("defaultRFCSettings"), "method:defaultRFCSettings");
        check(methods.contains("flagAsString"), "method:flagAsString");
        check(methods.contains("getParameter"), "method:getParameter");
        check(methods.contains("setParameter"), "method:setParameter");
        check(methods.contains("toByteArray"), "method:toByteArray");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("type"), "method:type");
        check(methods.contains("update"), "method:update");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ACK"), "field:ACK");
        check(fields.contains("DEFAULT_ENABLE_PUSH"), "field:DEFAULT_ENABLE_PUSH");
        check(fields.contains("DEFAULT_HEADER_TABLE_SIZE"), "field:DEFAULT_HEADER_TABLE_SIZE");
        check(fields.contains("DEFAULT_INITIAL_WINDOW_SIZE"), "field:DEFAULT_INITIAL_WINDOW_SIZE");
        check(fields.contains("DEFAULT_MAX_CONCURRENT_STREAMS"), "field:DEFAULT_MAX_CONCURRENT_STREAMS");
        check(fields.contains("DEFAULT_MAX_FRAME_SIZE"), "field:DEFAULT_MAX_FRAME_SIZE");
        check(fields.contains("ENABLE_PUSH"), "field:ENABLE_PUSH");
        check(fields.contains("HEADER_TABLE_SIZE"), "field:HEADER_TABLE_SIZE");
        check(fields.contains("INITIAL_CONNECTION_WINDOW_SIZE"), "field:INITIAL_CONNECTION_WINDOW_SIZE");
        check(fields.contains("INITIAL_WINDOW_SIZE"), "field:INITIAL_WINDOW_SIZE");
        check(fields.contains("MAX_CONCURRENT_STREAMS"), "field:MAX_CONCURRENT_STREAMS");
        check(fields.contains("MAX_FRAME_SIZE"), "field:MAX_FRAME_SIZE");
        check(fields.contains("MAX_HEADER_LIST_SIZE"), "field:MAX_HEADER_LIST_SIZE");
        check(fields.contains("MAX_PARAM"), "field:MAX_PARAM");
        check(fields.contains("TYPE"), "field:TYPE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
