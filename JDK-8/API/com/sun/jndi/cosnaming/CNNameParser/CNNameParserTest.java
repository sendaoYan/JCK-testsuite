/*
 * @test
 * @summary com.sun.jndi.cosnaming.CNNameParser conformance: hierarchy and declared members via reflection.
 * @run main CNNameParserTest
 */

public class CNNameParserTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.cosnaming.CNNameParser", false, null);
        check(c.getName().equals("com.sun.jndi.cosnaming.CNNameParser"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.naming.NameParser"), "implements javax.naming.NameParser");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("cosNameToInsString"), "declares cosNameToInsString");
        check(ms.contains("cosNameToName"), "declares cosNameToName");
        check(ms.contains("escape"), "declares escape");
        check(ms.contains("insStringToStringifiedComps"), "declares insStringToStringifiedComps");
        check(ms.contains("isMeta"), "declares isMeta");
        check(ms.contains("nameToCosName"), "declares nameToCosName");
        check(ms.contains("parse"), "declares parse");
        check(ms.contains("parseComponent"), "declares parseComponent");
        check(ms.contains("stringifyComponent"), "declares stringifyComponent");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
