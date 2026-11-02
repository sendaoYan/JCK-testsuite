/*
 * @test
 * @summary jdk.internal.net.http.http3.ConnectionSettings conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.http3:+open
 * @run main ConnectionSettingsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ConnectionSettingsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.http3.ConnectionSettings", false, ConnectionSettingsTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.http3.ConnectionSettings"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Record"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("createFrom"), "method:createFrom");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("maxFieldSectionSize"), "method:maxFieldSectionSize");
        check(methods.contains("qpackBlockedStreams"), "method:qpackBlockedStreams");
        check(methods.contains("qpackMaxTableCapacity"), "method:qpackMaxTableCapacity");
        check(methods.contains("toString"), "method:toString");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("UNLIMITED_MAX_FIELD_SECTION_SIZE"), "field:UNLIMITED_MAX_FIELD_SECTION_SIZE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
