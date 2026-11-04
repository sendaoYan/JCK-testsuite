/*
 * @test
 * @summary com.sun.corba.se.impl.encoding.CodeSetConversion.BTCConverter conformance: type shape via reflection (headless-safe).
 * @run main BTCConverterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class BTCConverterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.encoding.CodeSetConversion$BTCConverter", false, BTCConverterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 4, "public declared method count");
        check(mn.contains("getChars"), "method getChars");
        check(mn.contains("getFixedCharWidth"), "method getFixedCharWidth");
        check(mn.contains("getNumChars"), "method getNumChars");
        check(mn.contains("isFixedWidthEncoding"), "method isFixedWidthEncoding");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
