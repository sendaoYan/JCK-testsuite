/*
 * @test
 * @summary com.sun.corba.se.impl.ior.EncapsulationUtility conformance: type shape via reflection (headless-safe).
 * @run main EncapsulationUtilityTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class EncapsulationUtilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.ior.EncapsulationUtility", false, EncapsulationUtilityTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("getEncapsulationStream"), "method getEncapsulationStream");
        check(mn.contains("readIdentifiableSequence"), "method readIdentifiableSequence");
        check(mn.contains("readOctets"), "method readOctets");
        check(mn.contains("writeEncapsulation"), "method writeEncapsulation");
        check(mn.contains("writeIdentifiableSequence"), "method writeIdentifiableSequence");
        check(mn.contains("writeOutputStream"), "method writeOutputStream");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
