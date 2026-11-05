/*
 * @test
 * @summary jdk.swing.interop.internal.InteropProviderImpl conformance: internal class reflection.
 * @modules jdk.unsupported.desktop/jdk.swing.interop.internal:+open
 * @run main InteropProviderImplTest
 */
public class InteropProviderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepen() throws Exception {
        Class<?> c = Class.forName("jdk.swing.interop.internal.InteropProviderImpl");
        check(c.getDeclaredConstructors().length == 1, "single constructor");
        check(java.lang.reflect.Modifier.isPublic(c.getDeclaredConstructors()[0].getModifiers()), "ctor public");
        check(c.getPackageName().equals("jdk.swing.interop.internal"), "internal package");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "no wrapper superclass");
        check(c.getDeclaredMethods().length == 0, "zero declared methods: pure facade");
        Class<?> spi = Class.forName("sun.swing.InteropProvider");
        check(spi.isInterface() && spi.getDeclaredMethods().length == 0, "sun.swing.InteropProvider is an empty marker interface");
        check(spi.isAssignableFrom(c), "implements marker interface");
        java.util.TreeSet<java.lang.Class<?>> ifaces = new java.util.TreeSet<>(java.util.Comparator.comparing(Class::getName));
        ifaces.addAll(java.util.Arrays.asList(c.getInterfaces()));
        check(ifaces.size() == 1 && ifaces.first().getName().equals("sun.swing.InteropProvider"), "single direct interface");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        Class<?> c = Class.forName("jdk.swing.interop.internal.InteropProviderImpl", false, InteropProviderImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.swing.interop.internal.InteropProviderImpl"), "name");
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
            check(String.join(";", n).equals("sun.swing.InteropProvider"), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals(""), "methods");
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
