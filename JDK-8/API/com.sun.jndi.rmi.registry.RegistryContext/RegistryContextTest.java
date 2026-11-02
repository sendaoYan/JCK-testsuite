/*
 * @test
 * @summary com.sun.jndi.rmi.registry.RegistryContext conformance: hierarchy and declared members via reflection.
 * @run main RegistryContextTest
 */

public class RegistryContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.rmi.registry.RegistryContext", false, null);
        check(c.getName().equals("com.sun.jndi.rmi.registry.RegistryContext"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.naming.Context"), "implements javax.naming.Context");
        check(in.contains("javax.naming.Referenceable"), "implements javax.naming.Referenceable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addToEnvironment"), "declares addToEnvironment");
        check(ms.contains("bind"), "declares bind");
        check(ms.contains("close"), "declares close");
        check(ms.contains("composeName"), "declares composeName");
        check(ms.contains("createSubcontext"), "declares createSubcontext");
        check(ms.contains("decodeObject"), "declares decodeObject");
        check(ms.contains("destroySubcontext"), "declares destroySubcontext");
        check(ms.contains("encodeObject"), "declares encodeObject");
        check(ms.contains("finalize"), "declares finalize");
        check(ms.contains("getEnvironment"), "declares getEnvironment");
        check(ms.contains("getNameInNamespace"), "declares getNameInNamespace");
        check(ms.contains("getNameParser"), "declares getNameParser");
        check(ms.contains("getReference"), "declares getReference");
        check(ms.contains("getRegistry"), "declares getRegistry");
        check(ms.contains("installSecurityMgr"), "declares installSecurityMgr");
        check(ms.contains("list"), "declares list");
        check(ms.contains("listBindings"), "declares listBindings");
        check(ms.contains("lookup"), "declares lookup");
        check(ms.contains("lookupLink"), "declares lookupLink");
        check(ms.contains("rebind"), "declares rebind");
        check(ms.contains("removeFromEnvironment"), "declares removeFromEnvironment");
        check(ms.contains("rename"), "declares rename");
        check(ms.contains("unbind"), "declares unbind");
        check(ms.contains("wrapRemoteException"), "declares wrapRemoteException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
