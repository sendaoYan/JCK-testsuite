/*
 * @test
 * @summary com.sun.jndi.toolkit.ctx.PartialCompositeDirContext conformance: hierarchy and declared members via reflection.
 * @run main PartialCompositeDirContextTest
 */

public class PartialCompositeDirContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.ctx.PartialCompositeDirContext", false, null);
        check(c.getName().equals("com.sun.jndi.toolkit.ctx.PartialCompositeDirContext"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.ctx.AtomicContext"), "superclass com.sun.jndi.toolkit.ctx.AtomicContext");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.naming.directory.DirContext"), "implements javax.naming.directory.DirContext");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("a_bind"), "declares a_bind");
        check(ms.contains("a_createSubcontext"), "declares a_createSubcontext");
        check(ms.contains("a_destroySubcontext"), "declares a_destroySubcontext");
        check(ms.contains("a_getNameParser"), "declares a_getNameParser");
        check(ms.contains("a_list"), "declares a_list");
        check(ms.contains("a_listBindings"), "declares a_listBindings");
        check(ms.contains("a_lookup"), "declares a_lookup");
        check(ms.contains("a_lookupLink"), "declares a_lookupLink");
        check(ms.contains("a_rebind"), "declares a_rebind");
        check(ms.contains("a_rename"), "declares a_rename");
        check(ms.contains("a_unbind"), "declares a_unbind");
        check(ms.contains("bind"), "declares bind");
        check(ms.contains("c_parseComponent"), "declares c_parseComponent");
        check(ms.contains("createSubcontext"), "declares createSubcontext");
        check(ms.contains("getAttributes"), "declares getAttributes");
        check(ms.contains("getPCDirContext"), "declares getPCDirContext");
        check(ms.contains("getSchema"), "declares getSchema");
        check(ms.contains("getSchemaClassDefinition"), "declares getSchemaClassDefinition");
        check(ms.contains("modifyAttributes"), "declares modifyAttributes");
        check(ms.contains("p_bind"), "declares p_bind");
        check(ms.contains("p_createSubcontext"), "declares p_createSubcontext");
        check(ms.contains("p_getAttributes"), "declares p_getAttributes");
        check(ms.contains("p_getSchema"), "declares p_getSchema");
        check(ms.contains("p_getSchemaClassDefinition"), "declares p_getSchemaClassDefinition");
        check(ms.contains("p_modifyAttributes"), "declares p_modifyAttributes");
        check(ms.contains("p_rebind"), "declares p_rebind");
        check(ms.contains("p_search"), "declares p_search");
        check(ms.contains("rebind"), "declares rebind");
        check(ms.contains("search"), "declares search");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
