/*
 * @test
 * @summary com.sun.corba.se.spi.orb.OperationFactory conformance: type shape via reflection (headless-safe).
 * @run main OperationFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class OperationFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.orb.OperationFactory", false, OperationFactoryTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 18, "public declared method count");
        check(mn.contains("URLAction"), "method URLAction");
        check(mn.contains("booleanAction"), "method booleanAction");
        check(mn.contains("classAction"), "method classAction");
        check(mn.contains("compose"), "method compose");
        check(mn.contains("convertIntegerToShort"), "method convertIntegerToShort");
        check(mn.contains("identityAction"), "method identityAction");
        check(mn.contains("indexAction"), "method indexAction");
        check(mn.contains("integerAction"), "method integerAction");
        check(mn.contains("integerRangeAction"), "method integerRangeAction");
        check(mn.contains("listAction"), "method listAction");
        check(mn.contains("mapAction"), "method mapAction");
        check(mn.contains("mapSequenceAction"), "method mapSequenceAction");
        check(mn.contains("maskErrorAction"), "method maskErrorAction");
        check(mn.contains("sequenceAction"), "method sequenceAction");
        check(mn.contains("setFlagAction"), "method setFlagAction");
        check(mn.contains("stringAction"), "method stringAction");
        check(mn.contains("suffixAction"), "method suffixAction");
        check(mn.contains("valueAction"), "method valueAction");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
