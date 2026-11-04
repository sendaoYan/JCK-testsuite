/*
 * @test
 * @summary com.sun.corba.se.impl.protocol.giopmsgheaders.FragmentMessage_1_2 conformance: type shape via reflection (headless-safe).
 * @run main FragmentMessage_1_2Test
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class FragmentMessage_1_2Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.protocol.giopmsgheaders.FragmentMessage_1_2", false, FragmentMessage_1_2Test.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.protocol.giopmsgheaders.Message_1_2"), "superclass com.sun.corba.se.impl.protocol.giopmsgheaders.Message_1_2");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.protocol.giopmsgheaders.FragmentMessage"), "interface com.sun.corba.se.impl.protocol.giopmsgheaders.FragmentMessage");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 5, "public declared method count");
        check(mn.contains("callback"), "method callback");
        check(mn.contains("getHeaderLength"), "method getHeaderLength");
        check(mn.contains("getRequestId"), "method getRequestId");
        check(mn.contains("read"), "method read");
        check(mn.contains("write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
