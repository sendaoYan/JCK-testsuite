/*
 * @test
 * @summary com.sun.jmx.remote.util.ClassLogger conformance: hierarchy and declared members via reflection.
 * @run main ClassLoggerTest
 */

public class ClassLoggerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.util.ClassLogger", false, null);
        check(c.getName().equals("com.sun.jmx.remote.util.ClassLogger"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("config"), "declares config");
        check(ms.contains("configOn"), "declares configOn");
        check(ms.contains("debug"), "declares debug");
        check(ms.contains("debugOn"), "declares debugOn");
        check(ms.contains("error"), "declares error");
        check(ms.contains("fine"), "declares fine");
        check(ms.contains("fineOn"), "declares fineOn");
        check(ms.contains("finer"), "declares finer");
        check(ms.contains("finerOn"), "declares finerOn");
        check(ms.contains("finest"), "declares finest");
        check(ms.contains("finestOn"), "declares finestOn");
        check(ms.contains("info"), "declares info");
        check(ms.contains("infoOn"), "declares infoOn");
        check(ms.contains("severe"), "declares severe");
        check(ms.contains("trace"), "declares trace");
        check(ms.contains("traceOn"), "declares traceOn");
        check(ms.contains("warning"), "declares warning");
        check(ms.contains("warningOn"), "declares warningOn");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
