/*
 * @test
 * @summary sun.misc.JavaLangAccess conformance: interface and members.
 * @run main JavaLangAccessTest
 */

public class JavaLangAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.JavaLangAccess", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.JavaLangAccess"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("blockedOn"), "declares method blockedOn");
        check(mnames.contains("casAnnotationType"), "declares method casAnnotationType");
        check(mnames.contains("getAnnotationType"), "declares method getAnnotationType");
        check(mnames.contains("getConstantPool"), "declares method getConstantPool");
        check(mnames.contains("getDeclaredAnnotationMap"), "declares method getDeclaredAnnotationMap");
        check(mnames.contains("getEnumConstantsShared"), "declares method getEnumConstantsShared");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
