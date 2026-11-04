/*
 * @test
 * @summary com.sun.jndi.toolkit.dir.HierMemDirCtx conformance: hierarchy and declared members via reflection.
 * @run main HierMemDirCtxTest
 */

public class HierMemDirCtxTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.dir.HierMemDirCtx", false, null);
        check(c.getName().equals("com.sun.jndi.toolkit.dir.HierMemDirCtx"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.naming.directory.DirContext"), "implements javax.naming.directory.DirContext");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addToEnvironment"), "declares addToEnvironment");
        check(ms.contains("applyMods"), "declares applyMods");
        check(ms.contains("bind"), "declares bind");
        check(ms.contains("canonizeName"), "declares canonizeName");
        check(ms.contains("close"), "declares close");
        check(ms.contains("composeName"), "declares composeName");
        check(ms.contains("createNewCtx"), "declares createNewCtx");
        check(ms.contains("createSubcontext"), "declares createSubcontext");
        check(ms.contains("destroySubcontext"), "declares destroySubcontext");
        check(ms.contains("doBind"), "declares doBind");
        check(ms.contains("doBindAux"), "declares doBindAux");
        check(ms.contains("doCreateSubcontext"), "declares doCreateSubcontext");
        check(ms.contains("doDestroySubcontext"), "declares doDestroySubcontext");
        check(ms.contains("doGetAttributes"), "declares doGetAttributes");
        check(ms.contains("doList"), "declares doList");
        check(ms.contains("doListBindings"), "declares doListBindings");
        check(ms.contains("doLookup"), "declares doLookup");
        check(ms.contains("doModifyAttributes"), "declares doModifyAttributes");
        check(ms.contains("doRebind"), "declares doRebind");
        check(ms.contains("doRebindAux"), "declares doRebindAux");
        check(ms.contains("doRename"), "declares doRename");
        check(ms.contains("doUnbind"), "declares doUnbind");
        check(ms.contains("getAttributes"), "declares getAttributes");
        check(ms.contains("getEnvironment"), "declares getEnvironment");
        check(ms.contains("getInternalName"), "declares getInternalName");
        check(ms.contains("getLeafName"), "declares getLeafName");
        check(ms.contains("getNameInNamespace"), "declares getNameInNamespace");
        check(ms.contains("getNameParser"), "declares getNameParser");
        check(ms.contains("getSchema"), "declares getSchema");
        check(ms.contains("getSchemaClassDefinition"), "declares getSchemaClassDefinition");
        check(ms.contains("init"), "declares init");
        check(ms.contains("list"), "declares list");
        check(ms.contains("listBindings"), "declares listBindings");
        check(ms.contains("lookup"), "declares lookup");
        check(ms.contains("lookupLink"), "declares lookupLink");
        check(ms.contains("modifyAttributes"), "declares modifyAttributes");
        check(ms.contains("rebind"), "declares rebind");
        check(ms.contains("removeFromEnvironment"), "declares removeFromEnvironment");
        check(ms.contains("rename"), "declares rename");
        check(ms.contains("search"), "declares search");
        check(ms.contains("setIgnoreCase"), "declares setIgnoreCase");
        check(ms.contains("setNameParser"), "declares setNameParser");
        check(ms.contains("setReadOnly"), "declares setReadOnly");
        check(ms.contains("unbind"), "declares unbind");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
