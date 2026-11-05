/*
 * @test
 * @summary jdk.editpad.EditPadProvider conformance: internal class reflection.
 * @modules jdk.editpad/jdk.editpad:+open
 * @run main EditPadProviderTest
 */
public class EditPadProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepen() throws Exception {
        Class<?> c = Class.forName("jdk.editpad.EditPadProvider");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        check(java.lang.reflect.Modifier.isPublic(ctor.getModifiers()), "public no-arg constructor");
        Object provider = ctor.newInstance();
        check(((Integer) c.getMethod("rank").invoke(provider)) == 5, "rank 5 above default editors");
        java.lang.reflect.Method edit = c.getMethod("edit", String.class, String.class,
                java.util.function.Consumer.class, java.util.function.Consumer.class);
        check(edit.getReturnType() == void.class, "edit returns void");
        check(java.lang.reflect.Modifier.isPublic(edit.getModifiers()), "edit public");
        // BuildInEditorProvider contract: edit(windowLabel, initialText, saveHandler, errorHandler), rank()
        Class<?> spi = Class.forName("jdk.internal.editor.spi.BuildInEditorProvider");
        check(spi.isAssignableFrom(c), "assignable to BuildInEditorProvider");
        check(spi.getMethod("rank").getReturnType() == int.class, "spi rank returns int");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        Class<?> c = Class.forName("jdk.editpad.EditPadProvider", false, EditPadProviderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.editpad.EditPadProvider"), "name");
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
            check(String.join(";", n).equals("jdk.internal.editor.spi.BuildInEditorProvider"), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("edit;rank"), "methods");
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
