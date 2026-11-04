/*
 * @test
 * @summary com.sun.jmx.remote.internal.IIOPProxy conformance: hierarchy and declared members via reflection.
 * @run main IIOPProxyTest
 */

public class IIOPProxyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.internal.IIOPProxy", false, null);
        check(c.getName().equals("com.sun.jmx.remote.internal.IIOPProxy"), "binary name");
        check(c.isInterface() == true, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("connect"), "declares connect");
        check(ms.contains("createOrb"), "declares createOrb");
        check(ms.contains("exportObject"), "declares exportObject");
        check(ms.contains("getDelegate"), "declares getDelegate");
        check(ms.contains("getOrb"), "declares getOrb");
        check(ms.contains("isOrb"), "declares isOrb");
        check(ms.contains("isStub"), "declares isStub");
        check(ms.contains("narrow"), "declares narrow");
        check(ms.contains("objectToString"), "declares objectToString");
        check(ms.contains("setDelegate"), "declares setDelegate");
        check(ms.contains("stringToObject"), "declares stringToObject");
        check(ms.contains("toStub"), "declares toStub");
        check(ms.contains("unexportObject"), "declares unexportObject");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
