/*
 * @test
 * @summary com.sun.jndi.toolkit.ctx.AtomicDirContext conformance: hierarchy and declared members via reflection.
 * @run main AtomicDirContextTest
 */

public class AtomicDirContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.ctx.AtomicDirContext", false, null);
        check(c.getName().equals("com.sun.jndi.toolkit.ctx.AtomicDirContext"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.ctx.ComponentDirContext"), "superclass com.sun.jndi.toolkit.ctx.ComponentDirContext");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("a_bind"), "declares a_bind");
        check(ms.contains("a_bind_nns"), "declares a_bind_nns");
        check(ms.contains("a_createSubcontext"), "declares a_createSubcontext");
        check(ms.contains("a_createSubcontext_nns"), "declares a_createSubcontext_nns");
        check(ms.contains("a_getAttributes"), "declares a_getAttributes");
        check(ms.contains("a_getAttributes_nns"), "declares a_getAttributes_nns");
        check(ms.contains("a_getSchema"), "declares a_getSchema");
        check(ms.contains("a_getSchemaClassDefinition"), "declares a_getSchemaClassDefinition");
        check(ms.contains("a_getSchemaDefinition_nns"), "declares a_getSchemaDefinition_nns");
        check(ms.contains("a_getSchema_nns"), "declares a_getSchema_nns");
        check(ms.contains("a_modifyAttributes"), "declares a_modifyAttributes");
        check(ms.contains("a_modifyAttributes_nns"), "declares a_modifyAttributes_nns");
        check(ms.contains("a_rebind"), "declares a_rebind");
        check(ms.contains("a_rebind_nns"), "declares a_rebind_nns");
        check(ms.contains("a_search"), "declares a_search");
        check(ms.contains("a_search_nns"), "declares a_search_nns");
        check(ms.contains("c_bind"), "declares c_bind");
        check(ms.contains("c_bind_nns"), "declares c_bind_nns");
        check(ms.contains("c_createSubcontext"), "declares c_createSubcontext");
        check(ms.contains("c_createSubcontext_nns"), "declares c_createSubcontext_nns");
        check(ms.contains("c_getAttributes"), "declares c_getAttributes");
        check(ms.contains("c_getAttributes_nns"), "declares c_getAttributes_nns");
        check(ms.contains("c_getSchema"), "declares c_getSchema");
        check(ms.contains("c_getSchemaClassDefinition"), "declares c_getSchemaClassDefinition");
        check(ms.contains("c_getSchemaClassDefinition_nns"), "declares c_getSchemaClassDefinition_nns");
        check(ms.contains("c_getSchema_nns"), "declares c_getSchema_nns");
        check(ms.contains("c_modifyAttributes"), "declares c_modifyAttributes");
        check(ms.contains("c_modifyAttributes_nns"), "declares c_modifyAttributes_nns");
        check(ms.contains("c_rebind"), "declares c_rebind");
        check(ms.contains("c_rebind_nns"), "declares c_rebind_nns");
        check(ms.contains("c_search"), "declares c_search");
        check(ms.contains("c_search_nns"), "declares c_search_nns");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
