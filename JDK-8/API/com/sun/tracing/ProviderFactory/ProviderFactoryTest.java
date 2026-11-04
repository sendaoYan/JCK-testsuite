/*
 * @test
 * @summary com.sun.tracing.ProviderFactory conformance: hierarchy and declared members via reflection.
 * @run main ProviderFactoryTest
 */

public class ProviderFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tracing.ProviderFactory", false, null);
        check(c.getName().equals("com.sun.tracing.ProviderFactory"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("createProvider"), "declares createProvider");
        check(ms.contains("getDefaultFactory"), "declares getDefaultFactory");
        check(ms.contains("getPrintStreamFromSpec"), "declares getPrintStreamFromSpec");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
