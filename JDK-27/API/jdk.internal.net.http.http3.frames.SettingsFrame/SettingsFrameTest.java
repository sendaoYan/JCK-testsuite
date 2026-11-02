/*
 * @test
 * @summary jdk.internal.net.http.http3.frames.SettingsFrame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.http3.frames:+open
 * @run main SettingsFrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class SettingsFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.http3.frames.SettingsFrame", false, SettingsFrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.http3.frames.SettingsFrame"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.http3.frames.AbstractHttp3Frame"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("decodeFrame"), "method:decodeFrame");
        check(methods.contains("defaultRFCSettings"), "method:defaultRFCSettings");
        check(methods.contains("getParameter"), "method:getParameter");
        check(methods.contains("isIllegal"), "method:isIllegal");
        check(methods.contains("isReservedId"), "method:isReservedId");
        check(methods.contains("length"), "method:length");
        check(methods.contains("nextRandomParameterValue"), "method:nextRandomParameterValue");
        check(methods.contains("nextRandomReservedParameterId"), "method:nextRandomReservedParameterId");
        check(methods.contains("setParameter"), "method:setParameter");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("writeFrame"), "method:writeFrame");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("DEFAULT_SETTINGS_MAX_FIELD_SECTION_SIZE"), "field:DEFAULT_SETTINGS_MAX_FIELD_SECTION_SIZE");
        check(fields.contains("DEFAULT_SETTINGS_QPACK_BLOCKED_STREAMS"), "field:DEFAULT_SETTINGS_QPACK_BLOCKED_STREAMS");
        check(fields.contains("DEFAULT_SETTINGS_QPACK_MAX_TABLE_CAPACITY"), "field:DEFAULT_SETTINGS_QPACK_MAX_TABLE_CAPACITY");
        check(fields.contains("ENABLE_PUSH"), "field:ENABLE_PUSH");
        check(fields.contains("INITIAL_WINDOW_SIZE"), "field:INITIAL_WINDOW_SIZE");
        check(fields.contains("MAX_CONCURRENT_STREAMS"), "field:MAX_CONCURRENT_STREAMS");
        check(fields.contains("MAX_FRAME_SIZE"), "field:MAX_FRAME_SIZE");
        check(fields.contains("MAX_PARAM"), "field:MAX_PARAM");
        check(fields.contains("SETTINGS_MAX_FIELD_SECTION_SIZE"), "field:SETTINGS_MAX_FIELD_SECTION_SIZE");
        check(fields.contains("SETTINGS_QPACK_BLOCKED_STREAMS"), "field:SETTINGS_QPACK_BLOCKED_STREAMS");
        check(fields.contains("SETTINGS_QPACK_MAX_TABLE_CAPACITY"), "field:SETTINGS_QPACK_MAX_TABLE_CAPACITY");
        check(fields.contains("TYPE"), "field:TYPE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
