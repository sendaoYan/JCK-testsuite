/*
 * @test
 * @summary jdk.internal.jshell.tool.ConsoleIOContext.CompletionTask.Result conformance: internal enum reflection.
 * @modules jdk.jshell/jdk.internal.jshell.tool:+open
 * @run main ResultTest
 */
public class ResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jshell.tool.ConsoleIOContext$CompletionTask$Result", false, ResultTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.jshell.tool.ConsoleIOContext$CompletionTask$Result"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
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
            check(String.join(";", s).equals("valueOf;values"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals("CONTINUE;FINISH;NO_DATA;SKIP;SKIP_NOREPAINT"), "fields");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) s.add(f.getName());
            check(String.join(";", s).equals("CONTINUE;FINISH;NO_DATA;SKIP;SKIP_NOREPAINT"), "enum constants");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
