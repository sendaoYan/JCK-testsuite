/*
 * @test
 * @summary com.sun.jmx.mbeanserver.Introspector conformance: hierarchy and declared members via reflection.
 * @run main IntrospectorTest
 */

public class IntrospectorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.mbeanserver.Introspector", false, null);
        check(c.getName().equals("com.sun.jmx.mbeanserver.Introspector"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("annotationToField"), "declares annotationToField");
        check(ms.contains("checkCompliance"), "declares checkCompliance");
        check(ms.contains("descriptorForAnnotations"), "declares descriptorForAnnotations");
        check(ms.contains("descriptorForElement"), "declares descriptorForElement");
        check(ms.contains("elementFromComplex"), "declares elementFromComplex");
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("findMBeanInterface"), "declares findMBeanInterface");
        check(ms.contains("getClassMBeanInfo"), "declares getClassMBeanInfo");
        check(ms.contains("getMBeanInterface"), "declares getMBeanInterface");
        check(ms.contains("getMXBeanInterface"), "declares getMXBeanInterface");
        check(ms.contains("getStandardMBeanInterface"), "declares getStandardMBeanInterface");
        check(ms.contains("implementsMBean"), "declares implementsMBean");
        check(ms.contains("isDynamic"), "declares isDynamic");
        check(ms.contains("makeDynamicMBean"), "declares makeDynamicMBean");
        check(ms.contains("testCompliance"), "declares testCompliance");
        check(ms.contains("testComplianceMBeanInterface"), "declares testComplianceMBeanInterface");
        check(ms.contains("testComplianceMXBeanInterface"), "declares testComplianceMXBeanInterface");
        check(ms.contains("testCreation"), "declares testCreation");
        check(ms.contains("throwException"), "declares throwException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
