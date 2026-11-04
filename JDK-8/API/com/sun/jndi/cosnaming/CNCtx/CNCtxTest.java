/*
 * @test
 * @summary com.sun.jndi.cosnaming.CNCtx conformance: hierarchy and declared members via reflection.
 * @run main CNCtxTest
 */

public class CNCtxTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.cosnaming.CNCtx", false, null);
        check(c.getName().equals("com.sun.jndi.cosnaming.CNCtx"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.naming.Context"), "implements javax.naming.Context");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addToEnvironment"), "declares addToEnvironment");
        check(ms.contains("bind"), "declares bind");
        check(ms.contains("callBindNewContext"), "declares callBindNewContext");
        check(ms.contains("callBindOrRebind"), "declares callBindOrRebind");
        check(ms.contains("callDestroy"), "declares callDestroy");
        check(ms.contains("callResolve"), "declares callResolve");
        check(ms.contains("callUnbind"), "declares callUnbind");
        check(ms.contains("close"), "declares close");
        check(ms.contains("composeName"), "declares composeName");
        check(ms.contains("createSubcontext"), "declares createSubcontext");
        check(ms.contains("createUsingURL"), "declares createUsingURL");
        check(ms.contains("decEnumCount"), "declares decEnumCount");
        check(ms.contains("destroySubcontext"), "declares destroySubcontext");
        check(ms.contains("finalize"), "declares finalize");
        check(ms.contains("getContinuationContext"), "declares getContinuationContext");
        check(ms.contains("getDefaultOrb"), "declares getDefaultOrb");
        check(ms.contains("getEnvironment"), "declares getEnvironment");
        check(ms.contains("getNameInNamespace"), "declares getNameInNamespace");
        check(ms.contains("getNameParser"), "declares getNameParser");
        check(ms.contains("getStringifiedIor"), "declares getStringifiedIor");
        check(ms.contains("incEnumCount"), "declares incEnumCount");
        check(ms.contains("initOrbAndRootContext"), "declares initOrbAndRootContext");
        check(ms.contains("initUsingCorbanameUrl"), "declares initUsingCorbanameUrl");
        check(ms.contains("initUsingIiopUrl"), "declares initUsingIiopUrl");
        check(ms.contains("initUsingUrl"), "declares initUsingUrl");
        check(ms.contains("isCorbaUrl"), "declares isCorbaUrl");
        check(ms.contains("leafNotFound"), "declares leafNotFound");
        check(ms.contains("list"), "declares list");
        check(ms.contains("listBindings"), "declares listBindings");
        check(ms.contains("lookup"), "declares lookup");
        check(ms.contains("lookupLink"), "declares lookupLink");
        check(ms.contains("makeFullName"), "declares makeFullName");
        check(ms.contains("rebind"), "declares rebind");
        check(ms.contains("removeFromEnvironment"), "declares removeFromEnvironment");
        check(ms.contains("rename"), "declares rename");
        check(ms.contains("setOrbAndRootContext"), "declares setOrbAndRootContext");
        check(ms.contains("unbind"), "declares unbind");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
