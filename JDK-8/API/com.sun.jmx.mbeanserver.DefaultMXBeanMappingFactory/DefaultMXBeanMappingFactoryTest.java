/*
 * @test
 * @summary com.sun.jmx.mbeanserver.DefaultMXBeanMappingFactory conformance: hierarchy and declared members via reflection.
 * @run main DefaultMXBeanMappingFactoryTest
 */

public class DefaultMXBeanMappingFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.mbeanserver.DefaultMXBeanMappingFactory", false, null);
        check(c.getName().equals("com.sun.jmx.mbeanserver.DefaultMXBeanMappingFactory"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jmx.mbeanserver.MXBeanMappingFactory"), "superclass com.sun.jmx.mbeanserver.MXBeanMappingFactory");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("capitalize"), "declares capitalize");
        check(ms.contains("decapitalize"), "declares decapitalize");
        check(ms.contains("getMapping"), "declares getMapping");
        check(ms.contains("invalidObjectException"), "declares invalidObjectException");
        check(ms.contains("isIdentity"), "declares isIdentity");
        check(ms.contains("makeArrayOrCollectionMapping"), "declares makeArrayOrCollectionMapping");
        check(ms.contains("makeCompositeMapping"), "declares makeCompositeMapping");
        check(ms.contains("makeEnumMapping"), "declares makeEnumMapping");
        check(ms.contains("makeMXBeanRefMapping"), "declares makeMXBeanRefMapping");
        check(ms.contains("makeMapping"), "declares makeMapping");
        check(ms.contains("makeParameterizedTypeMapping"), "declares makeParameterizedTypeMapping");
        check(ms.contains("makeTabularMapping"), "declares makeTabularMapping");
        check(ms.contains("mappingForType"), "declares mappingForType");
        check(ms.contains("mustBeComparable"), "declares mustBeComparable");
        check(ms.contains("openDataException"), "declares openDataException");
        check(ms.contains("propertyName"), "declares propertyName");
        check(ms.contains("putMapping"), "declares putMapping");
        check(ms.contains("putPermanentMapping"), "declares putPermanentMapping");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
