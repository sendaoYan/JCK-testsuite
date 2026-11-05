/*
 * @test
 * @summary jdk.nio.zipfs.ZipInfo conformance: internal class reflection.
 * @modules jdk.zipfs/jdk.nio.zipfs:+open
 * @run main ZipInfoTest
 */
public class ZipInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepen() throws Exception {
        java.io.File zip = java.io.File.createTempFile("zipinfodeepen", ".zip");
        zip.deleteOnExit();
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(zip))) {
            zos.putNextEntry(new java.util.zip.ZipEntry("deep/entry.txt"));
            zos.write("info body".getBytes("UTF-8"));
            zos.closeEntry();
        }
        Class<?> c = Class.forName("jdk.nio.zipfs.ZipInfo");
        java.io.PrintStream old = System.out;
        java.io.ByteArrayOutputStream cap = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(cap, true, "UTF-8"));
        try {
            c.getDeclaredMethod("main", String[].class).invoke(null, (Object) new String[] {zip.getAbsolutePath()});
        } finally {
            System.setOut(old);
        }
        String out = cap.toString("UTF-8");
        check(out.contains("deep/entry.txt"), "main prints entry name");
        check(out.length() > 100, "main prints substantial info");
        cap.reset();
        System.setOut(new java.io.PrintStream(cap, true, "UTF-8"));
        try {
            c.getDeclaredMethod("main", String[].class).invoke(null, (Object) new String[] {});
        } finally {
            System.setOut(old);
        }
        check(cap.toString("UTF-8").startsWith("Usage: java ZipInfo"), "no args prints usage");
        check(java.lang.reflect.Modifier.isStatic(c.getDeclaredMethod("main", String[].class).getModifiers()), "main static");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        Class<?> c = Class.forName("jdk.nio.zipfs.ZipInfo", false, ZipInfoTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.nio.zipfs.ZipInfo"), "name");
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
            check(String.join(";", s).equals("main"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals(""), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
