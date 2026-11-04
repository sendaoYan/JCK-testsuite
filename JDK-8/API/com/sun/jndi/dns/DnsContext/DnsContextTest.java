/*
 * @test
 * @summary com.sun.jndi.dns.DnsContext conformance: hierarchy and declared members via reflection.
 * @run main DnsContextTest
 */

public class DnsContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.dns.DnsContext", false, null);
        check(c.getName().equals("com.sun.jndi.dns.DnsContext"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.ctx.ComponentDirContext"), "superclass com.sun.jndi.toolkit.ctx.ComponentDirContext");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addToEnvironment"), "declares addToEnvironment");
        check(ms.contains("attrIdsToClassesAndTypes"), "declares attrIdsToClassesAndTypes");
        check(ms.contains("c_bind"), "declares c_bind");
        check(ms.contains("c_createSubcontext"), "declares c_createSubcontext");
        check(ms.contains("c_destroySubcontext"), "declares c_destroySubcontext");
        check(ms.contains("c_getAttributes"), "declares c_getAttributes");
        check(ms.contains("c_getNameParser"), "declares c_getNameParser");
        check(ms.contains("c_getSchema"), "declares c_getSchema");
        check(ms.contains("c_getSchemaClassDefinition"), "declares c_getSchemaClassDefinition");
        check(ms.contains("c_list"), "declares c_list");
        check(ms.contains("c_listBindings"), "declares c_listBindings");
        check(ms.contains("c_lookup"), "declares c_lookup");
        check(ms.contains("c_lookupLink"), "declares c_lookupLink");
        check(ms.contains("c_modifyAttributes"), "declares c_modifyAttributes");
        check(ms.contains("c_rebind"), "declares c_rebind");
        check(ms.contains("c_rename"), "declares c_rename");
        check(ms.contains("c_search"), "declares c_search");
        check(ms.contains("c_unbind"), "declares c_unbind");
        check(ms.contains("classAndTypeMatch"), "declares classAndTypeMatch");
        check(ms.contains("close"), "declares close");
        check(ms.contains("composeName"), "declares composeName");
        check(ms.contains("dprint"), "declares dprint");
        check(ms.contains("fromAttrId"), "declares fromAttrId");
        check(ms.contains("fullyQualify"), "declares fullyQualify");
        check(ms.contains("getClassAndTypeToQuery"), "declares getClassAndTypeToQuery");
        check(ms.contains("getEnvironment"), "declares getEnvironment");
        check(ms.contains("getLookupCT"), "declares getLookupCT");
        check(ms.contains("getNameInNamespace"), "declares getNameInNamespace");
        check(ms.contains("getNameNode"), "declares getNameNode");
        check(ms.contains("getResolver"), "declares getResolver");
        check(ms.contains("initFromEnvironment"), "declares initFromEnvironment");
        check(ms.contains("isZoneCurrent"), "declares isZoneCurrent");
        check(ms.contains("p_getEnvironment"), "declares p_getEnvironment");
        check(ms.contains("populateZone"), "declares populateZone");
        check(ms.contains("removeFromEnvironment"), "declares removeFromEnvironment");
        check(ms.contains("rrsToAttrs"), "declares rrsToAttrs");
        check(ms.contains("setProviderUrl"), "declares setProviderUrl");
        check(ms.contains("toAttrId"), "declares toAttrId");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
