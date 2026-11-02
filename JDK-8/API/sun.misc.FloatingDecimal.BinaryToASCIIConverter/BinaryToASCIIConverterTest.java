/*
 * @test
 * @summary sun.misc.FloatingDecimal.BinaryToASCIIConverter conformance: interface and members.
 * @run main BinaryToASCIIConverterTest
 */

public class BinaryToASCIIConverterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.FloatingDecimal$BinaryToASCIIConverter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.FloatingDecimal$BinaryToASCIIConverter"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("appendTo"), "declares method appendTo");
        check(mnames.contains("decimalDigitsExact"), "declares method decimalDigitsExact");
        check(mnames.contains("digitsRoundedUp"), "declares method digitsRoundedUp");
        check(mnames.contains("getDecimalExponent"), "declares method getDecimalExponent");
        check(mnames.contains("getDigits"), "declares method getDigits");
        check(mnames.contains("isExceptional"), "declares method isExceptional");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
