/*
 * @test
 * @summary com.sun.org.glassfish.gmbal.AMXMetadata conformance: reflection.
 * @run main AMXMetadataTest
 */

public class AMXMetadataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.gmbal.AMXMetadata", false, AMXMetadataTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("AMXMetadata"), "simple name AMXMetadata");
        check(c.isAnnotation(), "is annotation");
        check(c.isInterface(), "annotation is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.annotation.Annotation"), "interface java.lang.annotation.Annotation");
        check(has(c, "isSingleton"), "method isSingleton");
        check(has(c, "group"), "method group");
        check(has(c, "subTypes"), "method subTypes");
        check(has(c, "genericInterfaceName"), "method genericInterfaceName");
        check(has(c, "immutableInfo"), "method immutableInfo");
        check(has(c, "interfaceClassName"), "method interfaceClassName");
        check(has(c, "type"), "method type");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
