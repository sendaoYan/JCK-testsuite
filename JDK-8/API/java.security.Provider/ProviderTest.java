/*
 * @test
 * @summary java.security.Provider conformance: abstract class hierarchy and key methods.
 * @run main ProviderTest
 */

public class ProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.Provider");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("java.util.Properties"), "extends java.util.Properties");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("clear"), "declares clear");
        check(ms.contains("compute"), "declares compute");
        check(ms.contains("computeIfAbsent"), "declares computeIfAbsent");
        check(ms.contains("computeIfPresent"), "declares computeIfPresent");
        check(ms.contains("elements"), "declares elements");
        check(ms.contains("forEach"), "declares forEach");
        check(ms.contains("get"), "declares get");
        check(ms.contains("getInfo"), "declares getInfo");
        check(ms.contains("getName"), "declares getName");
        check(ms.contains("getOrDefault"), "declares getOrDefault");
        check(ms.contains("getProperty"), "declares getProperty");
        check(ms.contains("getService"), "declares getService");
        check(ms.contains("getServices"), "declares getServices");
        check(ms.contains("getVersion"), "declares getVersion");
        check(ms.contains("keySet"), "declares keySet");
        check(ms.contains("keys"), "declares keys");
        check(ms.contains("load"), "declares load");
        check(ms.contains("merge"), "declares merge");
        check(ms.contains("put"), "declares put");
        check(ms.contains("putAll"), "declares putAll");
        check(ms.contains("putIfAbsent"), "declares putIfAbsent");
        check(ms.contains("putService"), "declares putService");
        check(ms.contains("remove"), "declares remove");
        check(ms.contains("removeService"), "declares removeService");
        check(ms.contains("replace"), "declares replace");
        check(ms.contains("replaceAll"), "declares replaceAll");
        check(ms.contains("toString"), "declares toString");
        check(ms.contains("values"), "declares values");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
