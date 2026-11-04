/*
 * @test
 * @summary com.sun.corba.se.impl.orbutil.ObjectStreamClassUtil_1_3 conformance: type shape via reflection (headless-safe).
 * @run main ObjectStreamClassUtil_1_3Test
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ObjectStreamClassUtil_1_3Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.orbutil.ObjectStreamClassUtil_1_3", false, ObjectStreamClassUtil_1_3Test.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 2, "public declared method count");
        check(mn.contains("computeSerialVersionUID"), "method computeSerialVersionUID");
        check(mn.contains("computeStructuralUID"), "method computeStructuralUID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
