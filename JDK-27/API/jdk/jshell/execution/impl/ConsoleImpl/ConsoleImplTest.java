/*
 * @test
 * @summary jdk.jshell.execution.impl.ConsoleImpl conformance: internal class reflection.
 * @modules jdk.jshell/jdk.jshell.execution.impl:+open
 * @run main/othervm --add-opens=jdk.jshell/jdk.jshell.execution.impl=ALL-UNNAMED ConsoleImplTest
 */
public class ConsoleImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: holder shape and safe static entry point ----
    static void testDeepShape() throws Exception {
        // ensureOutputAreWritten() is safe to call when no remote console is installed
        Class<?> c = Class.forName("jdk.jshell.execution.impl.ConsoleImpl");
        c.getMethod("ensureOutputAreWritten").invoke(null);
        check(true, "ensureOutputAreWritten() is a no-op without a remote console");
        // the console protocol machinery lives in nested classes
        Class<?> prov = Class.forName("jdk.jshell.execution.impl.ConsoleImpl$ConsoleProviderImpl");
        check(prov != null, "nested ConsoleProviderImpl");
        Class<?> outs = Class.forName("jdk.jshell.execution.impl.ConsoleImpl$ConsoleOutputStream");
        check(java.lang.reflect.Modifier.isPublic(outs.getModifiers()), "ConsoleOutputStream is public");
        check(java.io.OutputStream.class.isAssignableFrom(outs), "ConsoleOutputStream extends OutputStream");
        // the Task protocol has exactly the documented 8 operations, in dispatch order
        Class<?> task = Class.forName("jdk.jshell.execution.impl.ConsoleImpl$Task");
        check(task.isEnum(), "Task is an enum");
        check(task.getEnumConstants().length == 8, "eight console tasks");
        java.util.List<String> names = new java.util.ArrayList<>();
        for (Object t : task.getEnumConstants()) names.add(((Enum<?>) t).name());
        check(names.equals(java.util.Arrays.asList("WRITE_CHARS", "FLUSH_OUTPUT", "READ_CHARS", "READ_LINE",
              "READ_PASSWORD", "FLUSH_CONSOLE", "CHARSET", "ENSURE_OUTPUTS_ARE_WRITTEN")),
              "task order matches the wire protocol: " + names);
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.impl.ConsoleImpl", false, ConsoleImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.jshell.execution.impl.ConsoleImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals(""), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("ensureOutputAreWritten"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals(""), "fields");
        }
        testDeepShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
