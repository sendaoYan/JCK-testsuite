/*
 * @test
 * @summary com.sun.corba.se.impl.encoding.TypeCodeReader conformance: type shape via reflection (headless-safe).
 * @run main TypeCodeReaderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TypeCodeReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.encoding.TypeCodeReader", false, TypeCodeReaderTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.encoding.MarshalInputStream"), "interface com.sun.corba.se.impl.encoding.MarshalInputStream");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 7, "public declared method count");
        check(mn.contains("addTypeCodeAtPosition"), "method addTypeCodeAtPosition");
        check(mn.contains("getPosition"), "method getPosition");
        check(mn.contains("getTopLevelPosition"), "method getTopLevelPosition");
        check(mn.contains("getTopLevelStream"), "method getTopLevelStream");
        check(mn.contains("getTypeCodeAtPosition"), "method getTypeCodeAtPosition");
        check(mn.contains("printTypeMap"), "method printTypeMap");
        check(mn.contains("setEnclosingInputStream"), "method setEnclosingInputStream");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
