/*
 * @test
 * @summary com.sun.jndi.url.ldap.ldapURLContext conformance: hierarchy and declared members via reflection.
 * @run main ldapURLContextTest
 */

public class ldapURLContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.url.ldap.ldapURLContext", false, null);
        check(c.getName().equals("com.sun.jndi.url.ldap.ldapURLContext"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.url.GenericURLDirContext"), "superclass com.sun.jndi.toolkit.url.GenericURLDirContext");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("bind"), "declares bind");
        check(ms.contains("composeName"), "declares composeName");
        check(ms.contains("createSubcontext"), "declares createSubcontext");
        check(ms.contains("destroySubcontext"), "declares destroySubcontext");
        check(ms.contains("getAttributes"), "declares getAttributes");
        check(ms.contains("getNameParser"), "declares getNameParser");
        check(ms.contains("getRootURLContext"), "declares getRootURLContext");
        check(ms.contains("getSchema"), "declares getSchema");
        check(ms.contains("getSchemaClassDefinition"), "declares getSchemaClassDefinition");
        check(ms.contains("getURLSuffix"), "declares getURLSuffix");
        check(ms.contains("list"), "declares list");
        check(ms.contains("listBindings"), "declares listBindings");
        check(ms.contains("lookup"), "declares lookup");
        check(ms.contains("lookupLink"), "declares lookupLink");
        check(ms.contains("modifyAttributes"), "declares modifyAttributes");
        check(ms.contains("rebind"), "declares rebind");
        check(ms.contains("rename"), "declares rename");
        check(ms.contains("search"), "declares search");
        check(ms.contains("searchUsingURL"), "declares searchUsingURL");
        check(ms.contains("setFilterUsingURL"), "declares setFilterUsingURL");
        check(ms.contains("setSearchControlsUsingURL"), "declares setSearchControlsUsingURL");
        check(ms.contains("unbind"), "declares unbind");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
