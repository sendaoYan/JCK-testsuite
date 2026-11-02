/*
 * @test
 * @summary com.sun.corba.se.impl.encoding.CDROutputObject conformance: type shape via reflection (headless-safe).
 * @run main CDROutputObjectTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CDROutputObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.encoding.CDROutputObject", false, CDROutputObjectTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.spi.encoding.CorbaOutputObject"), "superclass com.sun.corba.se.spi.encoding.CorbaOutputObject");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 7, "public declared method count");
        check(mn.contains("create_input_stream"), "method create_input_stream");
        check(mn.contains("finishSendingMessage"), "method finishSendingMessage");
        check(mn.contains("getByteBufferWithInfo"), "method getByteBufferWithInfo");
        check(mn.contains("getConnection"), "method getConnection");
        check(mn.contains("getMessageHeader"), "method getMessageHeader");
        check(mn.contains("setByteBufferWithInfo"), "method setByteBufferWithInfo");
        check(mn.contains("writeTo"), "method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
