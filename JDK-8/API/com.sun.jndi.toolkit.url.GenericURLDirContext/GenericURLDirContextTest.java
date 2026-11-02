/*
 * @test
 * @summary com.sun.jndi.toolkit.url.GenericURLDirContext conformance: hierarchy and declared members via reflection.
 * @run main GenericURLDirContextTest
 */

public class GenericURLDirContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.url.GenericURLDirContext", false, null);
        check(c.getName().equals("com.sun.jndi.toolkit.url.GenericURLDirContext"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.url.GenericURLContext"), "superclass com.sun.jndi.toolkit.url.GenericURLContext");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.naming.directory.DirContext"), "implements javax.naming.directory.DirContext");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("bind"), "declares bind");
        check(ms.contains("createSubcontext"), "declares createSubcontext");
        check(ms.contains("getAttributes"), "declares getAttributes");
        check(ms.contains("getContinuationDirContext"), "declares getContinuationDirContext");
        check(ms.contains("getSchema"), "declares getSchema");
        check(ms.contains("getSchemaClassDefinition"), "declares getSchemaClassDefinition");
        check(ms.contains("modifyAttributes"), "declares modifyAttributes");
        check(ms.contains("rebind"), "declares rebind");
        check(ms.contains("search"), "declares search");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
