/*
 * @test
 * @summary com.sun.xml.internal.ws.message.source.PayloadSourceMessage conformance: reflection (kind, superclass, interfaces, methods).
 * @run main PayloadSourceMessageTest
 */

import java.lang.reflect.Modifier;


public class PayloadSourceMessageTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.message.source.PayloadSourceMessage", false, PayloadSourceMessageTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.message.source.PayloadSourceMessage"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.message.stream.PayloadStreamReaderMessage"), "superclass com.sun.xml.internal.ws.message.stream.PayloadStreamReaderMessage");
        check(c.getInterfaces().length == 0, "no interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
