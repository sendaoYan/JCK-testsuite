/*
 * @test
 * @summary sun.invoke.util.VerifyType conformance: class and members.
 * @run main VerifyTypeTest
 */

public class VerifyTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.invoke.util.VerifyType", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.invoke.util.VerifyType"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("canPassUnchecked"), "declares method canPassUnchecked");
        check(mnames.contains("isNullConversion"), "declares method isNullConversion");
        check(mnames.contains("isNullReferenceConversion"), "declares method isNullReferenceConversion");
        check(mnames.contains("isNullType"), "declares method isNullType");
        check(mnames.contains("isSpreadArgType"), "declares method isSpreadArgType");
        check(mnames.contains("spreadArgElementType"), "declares method spreadArgElementType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
