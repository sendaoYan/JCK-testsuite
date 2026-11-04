/*
 * @test
 * @summary com.sun.corba.se.impl.encoding.TypeCodeInputStream conformance: type shape via reflection (headless-safe).
 * @run main TypeCodeInputStreamTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TypeCodeInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.encoding.TypeCodeInputStream", false, TypeCodeInputStreamTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.encoding.EncapsInputStream"), "superclass com.sun.corba.se.impl.encoding.EncapsInputStream");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.encoding.TypeCodeReader"), "interface com.sun.corba.se.impl.encoding.TypeCodeReader");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 7, "public declared method count");
        check(mn.contains("addTypeCodeAtPosition"), "method addTypeCodeAtPosition");
        check(mn.contains("getTopLevelPosition"), "method getTopLevelPosition");
        check(mn.contains("getTopLevelStream"), "method getTopLevelStream");
        check(mn.contains("getTypeCodeAtPosition"), "method getTypeCodeAtPosition");
        check(mn.contains("printTypeMap"), "method printTypeMap");
        check(mn.contains("readEncapsulation"), "method readEncapsulation");
        check(mn.contains("setEnclosingInputStream"), "method setEnclosingInputStream");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
