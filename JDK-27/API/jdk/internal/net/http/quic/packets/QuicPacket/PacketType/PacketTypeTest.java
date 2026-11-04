/*
 * @test
 * @summary jdk.internal.net.http.quic.packets.QuicPacket.PacketType conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.packets:+open
 * @run main PacketTypeTest
 */
import java.lang.reflect.*;
import java.util.*;
public class PacketTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.packets.QuicPacket$PacketType", false, PacketTypeTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.packets.QuicPacket$PacketType"), "name");
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
        check(methods.contains("isLongHeaderType"), "method:isLongHeaderType");
        check(methods.contains("isShortHeaderType"), "method:isShortHeaderType");
        check(methods.contains("keySpace"), "method:keySpace");
        check(methods.contains("valueOf"), "method:valueOf");
        check(methods.contains("values"), "method:values");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("HANDSHAKE"), "field:HANDSHAKE");
        check(fields.contains("INITIAL"), "field:INITIAL");
        check(fields.contains("NONE"), "field:NONE");
        check(fields.contains("ONERTT"), "field:ONERTT");
        check(fields.contains("RETRY"), "field:RETRY");
        check(fields.contains("VERSIONS"), "field:VERSIONS");
        check(fields.contains("ZERORTT"), "field:ZERORTT");
        Set<String> enumConsts = new HashSet<>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) enumConsts.add(f.getName());
        check(enumConsts.contains("HANDSHAKE"), "enum-const:HANDSHAKE");
        check(enumConsts.contains("INITIAL"), "enum-const:INITIAL");
        check(enumConsts.contains("NONE"), "enum-const:NONE");
        check(enumConsts.contains("ONERTT"), "enum-const:ONERTT");
        check(enumConsts.contains("RETRY"), "enum-const:RETRY");
        check(enumConsts.contains("VERSIONS"), "enum-const:VERSIONS");
        check(enumConsts.contains("ZERORTT"), "enum-const:ZERORTT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
