/*
 * @test
 * @summary sun.net.ftp.FtpDirEntry.Type conformance: internal package reflection.
 * @modules java.base/sun.net.ftp:+open
 * @run main TypeTest
 */
public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ftp.FtpDirEntry$Type", false, TypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "$VALUES"), "field $VALUES");
        check(hasField(c, "CDIR"), "field CDIR");
        check(hasField(c, "DIR"), "field DIR");
        check(hasField(c, "FILE"), "field FILE");
        check(hasField(c, "LINK"), "field LINK");
        check(hasField(c, "PDIR"), "field PDIR");
        check(hasEnumConstant(c, "CDIR"), "enum CDIR");
        check(hasEnumConstant(c, "DIR"), "enum DIR");
        check(hasEnumConstant(c, "FILE"), "enum FILE");
        check(hasEnumConstant(c, "LINK"), "enum LINK");
        check(hasEnumConstant(c, "PDIR"), "enum PDIR");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
