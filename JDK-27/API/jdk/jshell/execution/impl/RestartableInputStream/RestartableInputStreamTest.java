/*
 * @test
 * @summary jdk.jshell.execution.impl.RestartableInputStream conformance: internal class reflection.
 * @modules jdk.jshell/jdk.jshell.execution.impl:+open
 * @run main/othervm --add-opens=jdk.jshell/jdk.jshell.execution.impl=ALL-UNNAMED RestartableInputStreamTest
 */
public class RestartableInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: close-flag lifecycle around a delegate stream ----
    static void testLifecycle() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.impl.RestartableInputStream");
        c.getDeclaredConstructor(java.io.InputStream.class).setAccessible(true);
        java.io.InputStream del = new java.io.ByteArrayInputStream(new byte[]{9, 8});
        java.io.InputStream ris = (java.io.InputStream)
                c.getDeclaredConstructor(java.io.InputStream.class).newInstance(del);
        check(!((Boolean) c.getMethod("isClosed").invoke(ris)), "fresh stream is not closed");
        check(ris.read() == 9 && ris.read() == 8, "reads pass through to the delegate");
        check(ris.read() == -1, "EOF passes through");
        ris.close();
        check((Boolean) c.getMethod("isClosed").invoke(ris), "close() flips the closed flag");
        ris.close();
        check((Boolean) c.getMethod("isClosed").invoke(ris), "close is idempotent");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.impl.RestartableInputStream", false, RestartableInputStreamTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.jshell.execution.impl.RestartableInputStream"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.FilterInputStream"), "superclass");
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
            check(String.join(";", s).equals("close;isClosed"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals(""), "fields");
        }
        testLifecycle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
