/*
 * @test
 * @summary com.sun.jndi.url.rmi.rmiURLContext.Parser conformance: hierarchy and declared members via reflection.
 * @run main ParserTest
 */

public class ParserTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.url.rmi.rmiURLContext$Parser", false, null);
        check(c.getName().equals("com.sun.jndi.url.rmi.rmiURLContext$Parser"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("acceptsFragment"), "declares acceptsFragment");
        check(ms.contains("host"), "declares host");
        check(ms.contains("mode"), "declares mode");
        check(ms.contains("newNamingException"), "declares newNamingException");
        check(ms.contains("objName"), "declares objName");
        check(ms.contains("parse"), "declares parse");
        check(ms.contains("parseCompat"), "declares parseCompat");
        check(ms.contains("parseLegacy"), "declares parseLegacy");
        check(ms.contains("parseStrict"), "declares parseStrict");
        check(ms.contains("port"), "declares port");
        check(ms.contains("url"), "declares url");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
