/*
 * @test
 * @summary com.sun.java.util.jar.pack.Instruction.LookupSwitch conformance: type shape via reflection (headless-safe).
 * @run main LookupSwitchTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class LookupSwitchTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.util.jar.pack.Instruction$LookupSwitch", false, LookupSwitchTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.java.util.jar.pack.Instruction$LookupSwitch"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.java.util.jar.pack.Instruction$Switch"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getCaseCount"), "method getCaseCount");
        check(names.contains("getCaseValue"), "method getCaseValue");
        check(names.contains("getCaseLabel"), "method getCaseLabel");
        check(names.contains("setCaseCount"), "method setCaseCount");
        check(names.contains("setCaseValue"), "method setCaseValue");
        check(names.contains("setCaseLabel"), "method setCaseLabel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
