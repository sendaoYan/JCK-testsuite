/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicEndpoint.ChannelType conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main ChannelTypeTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ChannelTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicEndpoint$ChannelType", false, ChannelTypeTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicEndpoint$ChannelType"), "name");
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
        check(methods.contains("isBlocking"), "method:isBlocking");
        check(methods.contains("valueOf"), "method:valueOf");
        check(methods.contains("values"), "method:values");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("BLOCKING_WITH_VIRTUAL_THREADS"), "field:BLOCKING_WITH_VIRTUAL_THREADS");
        check(fields.contains("NON_BLOCKING_WITH_SELECTOR"), "field:NON_BLOCKING_WITH_SELECTOR");
        Set<String> enumConsts = new HashSet<>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) enumConsts.add(f.getName());
        check(enumConsts.contains("BLOCKING_WITH_VIRTUAL_THREADS"), "enum-const:BLOCKING_WITH_VIRTUAL_THREADS");
        check(enumConsts.contains("NON_BLOCKING_WITH_SELECTOR"), "enum-const:NON_BLOCKING_WITH_SELECTOR");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
