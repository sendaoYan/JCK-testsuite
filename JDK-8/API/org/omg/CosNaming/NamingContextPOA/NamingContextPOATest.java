/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPOA conformance: abstract class shape.
 * @run main NamingContextPOATest
 */

public class NamingContextPOATest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming.NamingContextPOA");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(org.omg.PortableServer.Servant.class.isAssignableFrom(c), "extends org.omg.PortableServer.Servant");
        check(org.omg.CosNaming.NamingContextOperations.class.isAssignableFrom(c), "implements org.omg.CosNaming.NamingContextOperations");
        check(org.omg.CORBA.portable.InvokeHandler.class.isAssignableFrom(c), "implements org.omg.CORBA.portable.InvokeHandler");
        // ---- deepened (batch C, jdk8u372 probed): POA servant shape ----
        check(java.lang.reflect.Modifier.isAbstract(org.omg.CosNaming.NamingContextPOA.class.getModifiers()), "POA tie base is abstract");
        check(org.omg.CosNaming.NamingContextPOA.class.getSuperclass().getName().equals("org.omg.PortableServer.Servant"), "extends org.omg.PortableServer.Servant");
        boolean hasOps = false, hasInvoke = false;
        for (Class<?> ci : org.omg.CosNaming.NamingContextPOA.class.getInterfaces()) {
            if (ci == org.omg.CosNaming.NamingContextOperations.class) hasOps = true;
            if (ci == org.omg.CORBA.portable.InvokeHandler.class) hasInvoke = true;
        }
        check(hasOps, "implements org.omg.CosNaming.NamingContextOperations");
        check(hasInvoke, "implements org.omg.CORBA.portable.InvokeHandler");
        try {
            org.omg.CosNaming.NamingContextPOA.class.getConstructor();
            check(true, "public no-arg ctor declared");
        } catch (NoSuchMethodException impossible) {
            throw new AssertionError("POA base must declare public ctor");
        }
        for (String m : new String[]{"_invoke", "_all_interfaces", "_this"})
            try {
                java.lang.reflect.Method mm = null;
                for (java.lang.reflect.Method cand : org.omg.CosNaming.NamingContextPOA.class.getDeclaredMethods())
                    if (cand.getName().equals(m)) { mm = cand; break; }
                check(mm != null, "declares " + m + "(...)");
            } catch (Throwable t) { throw new AssertionError(m); }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
