/*
 * @test
 * @summary jdk.internal.net.http.websocket.Frame.HeaderWriter conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.websocket:+open
 * @run main HeaderWriterTest
 */
import java.lang.reflect.*;
import java.util.*;
public class HeaderWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.websocket.Frame$HeaderWriter", false, HeaderWriterTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.websocket.Frame$HeaderWriter"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("fin"), "method:fin");
        check(methods.contains("noMask"), "method:noMask");
        check(methods.contains("opcode"), "method:opcode");
        check(methods.contains("payloadLen"), "method:payloadLen");
        check(methods.contains("write"), "method:write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
