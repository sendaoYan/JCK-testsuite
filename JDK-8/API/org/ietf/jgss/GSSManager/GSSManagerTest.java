/*
 * @test
 * @summary org.ietf.jgss.GSSManager conformance: abstract class hierarchy and key methods.
 * @run main GSSManagerTest
 */

public class GSSManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.ietf.jgss.GSSManager");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addProviderAtEnd"), "declares addProviderAtEnd");
        check(ms.contains("addProviderAtFront"), "declares addProviderAtFront");
        check(ms.contains("createContext"), "declares createContext");
        check(ms.contains("createCredential"), "declares createCredential");
        check(ms.contains("createName"), "declares createName");
        check(ms.contains("getInstance"), "declares getInstance");
        check(ms.contains("getMechs"), "declares getMechs");
        check(ms.contains("getMechsForName"), "declares getMechsForName");
        check(ms.contains("getNamesForMech"), "declares getNamesForMech");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
