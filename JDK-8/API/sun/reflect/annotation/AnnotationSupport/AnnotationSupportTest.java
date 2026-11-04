/*
 * @test
 * @summary sun.reflect.annotation.AnnotationSupport conformance: class and members.
 * @run main AnnotationSupportTest
 */

public class AnnotationSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.annotation.AnnotationSupport", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.reflect.annotation.AnnotationSupport"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkTypes"), "declares method checkTypes");
        check(mnames.contains("containerBeforeContainee"), "declares method containerBeforeContainee");
        check(mnames.contains("getAssociatedAnnotations"), "declares method getAssociatedAnnotations");
        check(mnames.contains("getDirectlyAndIndirectlyPresent"), "declares method getDirectlyAndIndirectlyPresent");
        check(mnames.contains("getIndirectlyPresent"), "declares method getIndirectlyPresent");
        check(mnames.contains("getValueArray"), "declares method getValueArray");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
