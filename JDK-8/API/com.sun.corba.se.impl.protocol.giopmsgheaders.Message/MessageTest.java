/*
 * @test
 * @summary com.sun.corba.se.impl.protocol.giopmsgheaders.Message conformance: type shape via reflection (headless-safe).
 * @run main MessageTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MessageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.protocol.giopmsgheaders.Message", false, MessageTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 15, "public declared method count");
        check(mn.contains("callback"), "method callback");
        check(mn.contains("createFragmentMessage"), "method createFragmentMessage");
        check(mn.contains("getByteBuffer"), "method getByteBuffer");
        check(mn.contains("getEncodingVersion"), "method getEncodingVersion");
        check(mn.contains("getGIOPVersion"), "method getGIOPVersion");
        check(mn.contains("getSize"), "method getSize");
        check(mn.contains("getThreadPoolToUse"), "method getThreadPoolToUse");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("isLittleEndian"), "method isLittleEndian");
        check(mn.contains("moreFragmentsToFollow"), "method moreFragmentsToFollow");
        check(mn.contains("read"), "method read");
        check(mn.contains("setByteBuffer"), "method setByteBuffer");
        check(mn.contains("setEncodingVersion"), "method setEncodingVersion");
        check(mn.contains("setSize"), "method setSize");
        check(mn.contains("write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
