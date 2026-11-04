/*
 * @test
 * @summary sun.net.ftp.FtpDirEntry.Permission conformance: enum constants and members.
 * @run main PermissionTest
 */

public class PermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ftp.FtpDirEntry$Permission", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.ftp.FtpDirEntry$Permission"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("valueOf"), "declares method valueOf");
        check(mnames.contains("values"), "declares method values");
        java.util.Set<String> consts = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) consts.add(f.getName());
        check(consts.contains("USER"), "enum constant USER");
        check(consts.contains("GROUP"), "enum constant GROUP");
        check(consts.contains("OTHERS"), "enum constant OTHERS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
