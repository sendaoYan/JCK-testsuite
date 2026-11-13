/*
 * @test
 * @summary sun.jvmstat.PlatformSupportImpl conformance: internal class reflection.
 * @modules jdk.internal.jvmstat/sun.jvmstat:+open
 * @library /lib
 * @run main PlatformSupportImplTest
 */
public class PlatformSupportImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c;
        try {
            c = Class.forName("sun.jvmstat.PlatformSupportImpl", false, PlatformSupportImplTest.class.getClassLoader());
        } catch (ClassNotFoundException cnfe) {
            // The class only exists in the unix build of jdk.internal.jvmstat;
            // the Windows build ships no counterpart, so there is nothing to
            // reflect on here.
            throw new jtreg.SkippedException("sun.jvmstat.PlatformSupportImpl is absent in this build");
        }
        check(c.getName().equals("sun.jvmstat.PlatformSupportImpl"), "name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        if (!System.getProperty("os.name", "").toLowerCase().contains("linux")) {
            // The remaining assertions snapshot the exact member set of the
            // unix build; the portable identity checks above are the part that
            // holds for every build that ships the class at all.
            System.out.println("PASS (" + passed + " assertions; SKIP build-specific internal structure)");
            return;
        }
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.jvmstat.PlatformSupport"), "superclass");
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
            check(String.join(";", s).equals("getLocalVmId;getNamespaceVmId;getTemporaryDirectories"), "methods");
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
