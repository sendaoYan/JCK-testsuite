/*
 * @test
 * @summary com.sun.istack.internal.localization.LocalizableMessage conformance: hierarchy and declared members via reflection.
 * @run main LocalizableMessageTest
 */

public class LocalizableMessageTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.istack.internal.localization.LocalizableMessage", false, null);
        check(c.getName().equals("com.sun.istack.internal.localization.LocalizableMessage"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.istack.internal.localization.Localizable"), "implements com.sun.istack.internal.localization.Localizable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getArguments"), "declares getArguments");
        check(ms.contains("getKey"), "declares getKey");
        check(ms.contains("getResourceBundleName"), "declares getResourceBundleName");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
