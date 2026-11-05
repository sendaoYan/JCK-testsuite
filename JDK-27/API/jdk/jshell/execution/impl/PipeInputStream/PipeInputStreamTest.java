/*
 * @test
 * @summary jdk.jshell.execution.impl.PipeInputStream conformance: internal class reflection.
 * @modules jdk.jshell/jdk.jshell.execution.impl:+open
 * @run main/othervm --add-opens=jdk.jshell/jdk.jshell.execution.impl=ALL-UNNAMED PipeInputStreamTest
 */
public class PipeInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: single-byte FIFO pipe with byte-wide (unsigned) transport ----
    static void testRealPipe() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.impl.PipeInputStream");
        c.getDeclaredConstructor().setAccessible(true);
        java.io.InputStream pis = (java.io.InputStream) c.getDeclaredConstructor().newInstance();
        java.io.OutputStream out = (java.io.OutputStream) c.getMethod("createOutput").invoke(pis);
        out.write(65); out.write(66); out.write(67);
        check(pis.read() == 65 && pis.read() == 66 && pis.read() == 67, "bytes arrive in FIFO order");
        out.write(new byte[]{1, 2, 3, 4}, 0, 4);
        byte[] buf = new byte[4];
        check(pis.read(buf, 0, 4) == 4 && buf[0] == 1 && buf[3] == 4, "bulk read returns buffered bytes");
        out.write(255);
        check(pis.read() == 255, "values are transported unsigned");
        check(pis.read(new byte[0], 0, 0) == 0, "zero-length read is a no-op");
        try { pis.read(new byte[4], 5, 1); check(false, "out-of-range read must throw"); }
        catch (java.lang.IndexOutOfBoundsException expected) { check(true, "out-of-range read -> IndexOutOfBoundsException"); }
        check(pis.available() == 0, "empty pipe has nothing available");
        out.close();
        check(pis.read() == -1, "closed output yields EOF");
        check(pis.read() == -1, "EOF is stable");
        try { out.write(1); check(false, "write after close must throw"); }
        catch (IllegalStateException expected) { check(true, "write after close -> IllegalStateException: " + expected.getMessage()); }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.impl.PipeInputStream", false, PipeInputStreamTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.jshell.execution.impl.PipeInputStream"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.InputStream"), "superclass");
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
            check(String.join(";", s).equals("close;createOutput;inputNeeded;read"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals(""), "fields");
        }
        testRealPipe();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
