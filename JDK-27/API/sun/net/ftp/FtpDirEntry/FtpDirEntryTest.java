/*
 * @test
 * @summary sun.net.ftp.FtpDirEntry conformance: internal package reflection.
 * @modules java.base/sun.net.ftp:+open
 * @run main FtpDirEntryTest
 */
public class FtpDirEntryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ftp.FtpDirEntry", false, FtpDirEntryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "addFact"), "method addFact");
        check(hasMethod(c, "canExexcute"), "method canExexcute");
        check(hasMethod(c, "canRead"), "method canRead");
        check(hasMethod(c, "canWrite"), "method canWrite");
        check(hasMethod(c, "getCreated"), "method getCreated");
        check(hasMethod(c, "getFact"), "method getFact");
        check(hasMethod(c, "getGroup"), "method getGroup");
        check(hasMethod(c, "getLastModified"), "method getLastModified");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getUser"), "method getUser");
        check(hasMethod(c, "setCreated"), "method setCreated");
        check(hasMethod(c, "setGroup"), "method setGroup");
        check(hasMethod(c, "setLastModified"), "method setLastModified");
        check(hasMethod(c, "setPermissions"), "method setPermissions");
        check(hasMethod(c, "setSize"), "method setSize");
        check(hasMethod(c, "setType"), "method setType");
        check(hasMethod(c, "setUser"), "method setUser");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "created"), "field created");
        check(hasField(c, "facts"), "field facts");
        check(hasField(c, "group"), "field group");
        check(hasField(c, "lastModified"), "field lastModified");
        check(hasField(c, "name"), "field name");
        check(hasField(c, "permissions"), "field permissions");
        check(hasField(c, "size"), "field size");
        check(hasField(c, "type"), "field type");
        check(hasField(c, "user"), "field user");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
