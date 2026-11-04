/*
 * @test
 * @summary com.sun.jndi.toolkit.ctx.ComponentDirContext conformance: hierarchy and declared members via reflection.
 * @run main ComponentDirContextTest
 */

public class ComponentDirContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.ctx.ComponentDirContext", false, null);
        check(c.getName().equals("com.sun.jndi.toolkit.ctx.ComponentDirContext"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.ctx.PartialCompositeDirContext"), "superclass com.sun.jndi.toolkit.ctx.PartialCompositeDirContext");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
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
        check(ms.contains("p_bind"), "declares p_bind");
        check(ms.contains("p_createSubcontext"), "declares p_createSubcontext");
        check(ms.contains("p_getAttributes"), "declares p_getAttributes");
        check(ms.contains("p_getSchema"), "declares p_getSchema");
        check(ms.contains("p_getSchemaClassDefinition"), "declares p_getSchemaClassDefinition");
        check(ms.contains("p_modifyAttributes"), "declares p_modifyAttributes");
        check(ms.contains("p_rebind"), "declares p_rebind");
        check(ms.contains("p_search"), "declares p_search");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
