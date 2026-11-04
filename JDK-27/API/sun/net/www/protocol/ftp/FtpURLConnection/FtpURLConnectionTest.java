/*
 * @test
 * @summary sun.net.www.protocol.ftp.FtpURLConnection conformance: internal package reflection.
 * @modules java.base/sun.net.www.protocol.ftp:+open
 * @run main FtpURLConnectionTest
 */
public class FtpURLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.ftp.FtpURLConnection", false, FtpURLConnectionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.www.URLConnection"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "connect"), "method connect");
        check(hasMethod(c, "getConnectTimeout"), "method getConnectTimeout");
        check(hasMethod(c, "getInputStream"), "method getInputStream");
        check(hasMethod(c, "getOutputStream"), "method getOutputStream");
        check(hasMethod(c, "getPermission"), "method getPermission");
        check(hasMethod(c, "getReadTimeout"), "method getReadTimeout");
        check(hasMethod(c, "getRequestProperty"), "method getRequestProperty");
        check(hasMethod(c, "setConnectTimeout"), "method setConnectTimeout");
        check(hasMethod(c, "setReadTimeout"), "method setReadTimeout");
        check(hasMethod(c, "setRequestProperty"), "method setRequestProperty");
        check(hasField(c, "ASCII"), "field ASCII");
        check(hasField(c, "BIN"), "field BIN");
        check(hasField(c, "DIR"), "field DIR");
        check(hasField(c, "NONE"), "field NONE");
        check(hasField(c, "connectTimeout"), "field connectTimeout");
        check(hasField(c, "filename"), "field filename");
        check(hasField(c, "ftp"), "field ftp");
        check(hasField(c, "fullpath"), "field fullpath");
        check(hasField(c, "host"), "field host");
        check(hasField(c, "http"), "field http");
        check(hasField(c, "instProxy"), "field instProxy");
        check(hasField(c, "is"), "field is");
        check(hasField(c, "os"), "field os");
        check(hasField(c, "password"), "field password");
        check(hasField(c, "pathname"), "field pathname");
        check(hasField(c, "permission"), "field permission");
        check(hasField(c, "port"), "field port");
        check(hasField(c, "readTimeout"), "field readTimeout");
        check(hasField(c, "type"), "field type");
        check(hasField(c, "user"), "field user");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
