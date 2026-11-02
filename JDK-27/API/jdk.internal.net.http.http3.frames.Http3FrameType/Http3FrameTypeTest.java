/*
 * @test
 * @summary jdk.internal.net.http.http3.frames.Http3FrameType conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.http3.frames:+open
 * @run main Http3FrameTypeTest
 */
import java.lang.reflect.*;
import java.util.*;
public class Http3FrameTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.http3.frames.Http3FrameType", false, Http3FrameTypeTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.http3.frames.Http3FrameType"), "name");
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
        check(methods.contains("asString"), "method:asString");
        check(methods.contains("forType"), "method:forType");
        check(methods.contains("intType"), "method:intType");
        check(methods.contains("isIllegalType"), "method:isIllegalType");
        check(methods.contains("isReservedType"), "method:isReservedType");
        check(methods.contains("maxLength"), "method:maxLength");
        check(methods.contains("type"), "method:type");
        check(methods.contains("valueOf"), "method:valueOf");
        check(methods.contains("values"), "method:values");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("CANCEL_PUSH"), "field:CANCEL_PUSH");
        check(fields.contains("DATA"), "field:DATA");
        check(fields.contains("GOAWAY"), "field:GOAWAY");
        check(fields.contains("HEADERS"), "field:HEADERS");
        check(fields.contains("MAX_PUSH_ID"), "field:MAX_PUSH_ID");
        check(fields.contains("PUSH_PROMISE"), "field:PUSH_PROMISE");
        check(fields.contains("SETTINGS"), "field:SETTINGS");
        check(fields.contains("UNKNOWN"), "field:UNKNOWN");
        Set<String> enumConsts = new HashSet<>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) enumConsts.add(f.getName());
        check(enumConsts.contains("CANCEL_PUSH"), "enum-const:CANCEL_PUSH");
        check(enumConsts.contains("DATA"), "enum-const:DATA");
        check(enumConsts.contains("GOAWAY"), "enum-const:GOAWAY");
        check(enumConsts.contains("HEADERS"), "enum-const:HEADERS");
        check(enumConsts.contains("MAX_PUSH_ID"), "enum-const:MAX_PUSH_ID");
        check(enumConsts.contains("PUSH_PROMISE"), "enum-const:PUSH_PROMISE");
        check(enumConsts.contains("SETTINGS"), "enum-const:SETTINGS");
        check(enumConsts.contains("UNKNOWN"), "enum-const:UNKNOWN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
