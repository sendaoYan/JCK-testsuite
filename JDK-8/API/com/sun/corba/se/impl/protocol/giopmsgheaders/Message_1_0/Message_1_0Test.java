/*
 * @test
 * @summary com.sun.corba.se.impl.protocol.giopmsgheaders.Message_1_0 conformance: type shape via reflection (headless-safe).
 * @run main Message_1_0Test
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class Message_1_0Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.protocol.giopmsgheaders.Message_1_0", false, Message_1_0Test.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.protocol.giopmsgheaders.MessageBase"), "superclass com.sun.corba.se.impl.protocol.giopmsgheaders.MessageBase");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 9, "public declared method count");
        check(mn.contains("createFragmentMessage"), "method createFragmentMessage");
        check(mn.contains("getGIOPVersion"), "method getGIOPVersion");
        check(mn.contains("getSize"), "method getSize");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("isLittleEndian"), "method isLittleEndian");
        check(mn.contains("moreFragmentsToFollow"), "method moreFragmentsToFollow");
        check(mn.contains("read"), "method read");
        check(mn.contains("setSize"), "method setSize");
        check(mn.contains("write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
