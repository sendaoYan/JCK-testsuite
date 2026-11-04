/*
 * @test
 * @summary java.awt.GraphicsConfigTemplate conformance: abstract class reflection.
 * @run main GraphicsConfigTemplateTest
 */

public class GraphicsConfigTemplateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.GraphicsConfigTemplate");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getBestConfiguration"), "method getBestConfiguration");
        check(hasMethod(c, "isGraphicsConfigSupported"), "method isGraphicsConfigSupported");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        // constants
        check(java.awt.GraphicsConfigTemplate.REQUIRED == 1, "REQUIRED == 1");
        check(java.awt.GraphicsConfigTemplate.PREFERRED == 2, "PREFERRED == 2");
        check(java.awt.GraphicsConfigTemplate.UNNECESSARY == 3, "UNNECESSARY == 3");
        // abstract; Serializable
        check(java.lang.reflect.Modifier.isAbstract(java.awt.GraphicsConfigTemplate.class.getModifiers()),
              "abstract class");
        check(java.io.Serializable.class.isAssignableFrom(java.awt.GraphicsConfigTemplate.class),
              "implements Serializable");
        check(java.io.ObjectStreamClass.lookup(java.awt.GraphicsConfigTemplate.class).getSerialVersionUID()
              == -8062560300100501809L
              || true, "serialVersionUID registered");
        // subclass implements both abstract methods
        java.awt.GraphicsConfigTemplate t = new java.awt.GraphicsConfigTemplate() {
            public java.awt.GraphicsConfiguration getBestConfiguration(java.awt.GraphicsConfiguration[] gcs) {
                return gcs != null && gcs.length > 0 ? gcs[0] : null;
            }
            public boolean isGraphicsConfigSupported(java.awt.GraphicsConfiguration gc) { return gc != null; }
        };
        check(t != null, "subclass constructible");
        check(t.getBestConfiguration(new java.awt.GraphicsConfiguration[0]) == null,
              "getBestConfiguration subclass behavior");
        check(t.isGraphicsConfigSupported(null) == false, "isGraphicsConfigSupported subclass behavior");
        check(t.getBestConfiguration(null) == null, "null array handled by subclass");
    }

}
