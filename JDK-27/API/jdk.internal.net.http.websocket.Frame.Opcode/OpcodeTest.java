/*
 * @test
 * @summary jdk.internal.net.http.websocket.Frame.Opcode conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.websocket:+open
 * @run main OpcodeTest
 */
import java.lang.reflect.*;
import java.util.*;
public class OpcodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.websocket.Frame$Opcode", false, OpcodeTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.websocket.Frame$Opcode"), "name");
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
        check(methods.contains("valueOf"), "method:valueOf");
        check(methods.contains("values"), "method:values");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("BINARY"), "field:BINARY");
        check(fields.contains("CLOSE"), "field:CLOSE");
        check(fields.contains("CONTINUATION"), "field:CONTINUATION");
        check(fields.contains("CONTROL_0xB"), "field:CONTROL_0xB");
        check(fields.contains("CONTROL_0xC"), "field:CONTROL_0xC");
        check(fields.contains("CONTROL_0xD"), "field:CONTROL_0xD");
        check(fields.contains("CONTROL_0xE"), "field:CONTROL_0xE");
        check(fields.contains("CONTROL_0xF"), "field:CONTROL_0xF");
        check(fields.contains("NON_CONTROL_0x3"), "field:NON_CONTROL_0x3");
        check(fields.contains("NON_CONTROL_0x4"), "field:NON_CONTROL_0x4");
        check(fields.contains("NON_CONTROL_0x5"), "field:NON_CONTROL_0x5");
        check(fields.contains("NON_CONTROL_0x6"), "field:NON_CONTROL_0x6");
        check(fields.contains("NON_CONTROL_0x7"), "field:NON_CONTROL_0x7");
        check(fields.contains("PING"), "field:PING");
        check(fields.contains("PONG"), "field:PONG");
        check(fields.contains("TEXT"), "field:TEXT");
        Set<String> enumConsts = new HashSet<>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) enumConsts.add(f.getName());
        check(enumConsts.contains("BINARY"), "enum-const:BINARY");
        check(enumConsts.contains("CLOSE"), "enum-const:CLOSE");
        check(enumConsts.contains("CONTINUATION"), "enum-const:CONTINUATION");
        check(enumConsts.contains("CONTROL_0xB"), "enum-const:CONTROL_0xB");
        check(enumConsts.contains("CONTROL_0xC"), "enum-const:CONTROL_0xC");
        check(enumConsts.contains("CONTROL_0xD"), "enum-const:CONTROL_0xD");
        check(enumConsts.contains("CONTROL_0xE"), "enum-const:CONTROL_0xE");
        check(enumConsts.contains("CONTROL_0xF"), "enum-const:CONTROL_0xF");
        check(enumConsts.contains("NON_CONTROL_0x3"), "enum-const:NON_CONTROL_0x3");
        check(enumConsts.contains("NON_CONTROL_0x4"), "enum-const:NON_CONTROL_0x4");
        check(enumConsts.contains("NON_CONTROL_0x5"), "enum-const:NON_CONTROL_0x5");
        check(enumConsts.contains("NON_CONTROL_0x6"), "enum-const:NON_CONTROL_0x6");
        check(enumConsts.contains("NON_CONTROL_0x7"), "enum-const:NON_CONTROL_0x7");
        check(enumConsts.contains("PING"), "enum-const:PING");
        check(enumConsts.contains("PONG"), "enum-const:PONG");
        check(enumConsts.contains("TEXT"), "enum-const:TEXT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
