/*
 * @test
 * @summary jdk.internal.net.http.websocket.Frame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.websocket:+open
 * @run main FrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class FrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.websocket.Frame", false, FrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.websocket.Frame"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("MAX_CONTROL_FRAME_PAYLOAD_LENGTH"), "field:MAX_CONTROL_FRAME_PAYLOAD_LENGTH");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
