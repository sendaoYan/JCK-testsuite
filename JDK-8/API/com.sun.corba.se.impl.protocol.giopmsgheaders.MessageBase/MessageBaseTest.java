/*
 * @test
 * @summary com.sun.corba.se.impl.protocol.giopmsgheaders.MessageBase conformance: type shape via reflection (headless-safe).
 * @run main MessageBaseTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MessageBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.protocol.giopmsgheaders.MessageBase", false, MessageBaseTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.protocol.giopmsgheaders.Message"), "interface com.sun.corba.se.impl.protocol.giopmsgheaders.Message");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 21, "public declared method count");
        check(mn.contains("callback"), "method callback");
        check(mn.contains("clearFlag"), "method clearFlag");
        check(mn.contains("createCancelRequest"), "method createCancelRequest");
        check(mn.contains("createCloseConnection"), "method createCloseConnection");
        check(mn.contains("createFragmentMessage"), "method createFragmentMessage");
        check(mn.contains("createLocateReply"), "method createLocateReply");
        check(mn.contains("createLocateRequest"), "method createLocateRequest");
        check(mn.contains("createMessageError"), "method createMessageError");
        check(mn.contains("createReply"), "method createReply");
        check(mn.contains("createRequest"), "method createRequest");
        check(mn.contains("getByteBuffer"), "method getByteBuffer");
        check(mn.contains("getEncodingVersion"), "method getEncodingVersion");
        check(mn.contains("getRequestId"), "method getRequestId");
        check(mn.contains("getThreadPoolToUse"), "method getThreadPoolToUse");
        check(mn.contains("readGIOPBody"), "method readGIOPBody");
        check(mn.contains("readGIOPHeader"), "method readGIOPHeader");
        check(mn.contains("readGIOPMessage"), "method readGIOPMessage");
        check(mn.contains("setByteBuffer"), "method setByteBuffer");
        check(mn.contains("setEncodingVersion"), "method setEncodingVersion");
        check(mn.contains("setFlag"), "method setFlag");
        check(mn.contains("typeToString"), "method typeToString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
