/*
 * @test
 * @summary sun.net.www.protocol.file.FileURLConnection conformance: internal package reflection.
 * @modules java.base/sun.net.www.protocol.file:+open
 * @run main FileURLConnectionTest
 */
public class FileURLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.file.FileURLConnection", false, FileURLConnectionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.www.URLConnection"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "closeInputStream"), "method closeInputStream");
        check(hasMethod(c, "connect"), "method connect");
        check(hasMethod(c, "ensureCanServeHeaders"), "method ensureCanServeHeaders");
        check(hasMethod(c, "getContentLength"), "method getContentLength");
        check(hasMethod(c, "getContentLengthLong"), "method getContentLengthLong");
        check(hasMethod(c, "getHeaderField"), "method getHeaderField");
        check(hasMethod(c, "getHeaderFieldKey"), "method getHeaderFieldKey");
        check(hasMethod(c, "getHeaderFields"), "method getHeaderFields");
        check(hasMethod(c, "getInputStream"), "method getInputStream");
        check(hasMethod(c, "getLastModified"), "method getLastModified");
        check(hasMethod(c, "getPermission"), "method getPermission");
        check(hasMethod(c, "getProperties"), "method getProperties");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "CONTENT_LENGTH"), "field CONTENT_LENGTH");
        check(hasField(c, "CONTENT_TYPE"), "field CONTENT_TYPE");
        check(hasField(c, "FTP_FALLBACK_ENABLED"), "field FTP_FALLBACK_ENABLED");
        check(hasField(c, "LAST_MODIFIED"), "field LAST_MODIFIED");
        check(hasField(c, "TEXT_PLAIN"), "field TEXT_PLAIN");
        check(hasField(c, "directoryListing"), "field directoryListing");
        check(hasField(c, "exists"), "field exists");
        check(hasField(c, "file"), "field file");
        check(hasField(c, "initializedHeaders"), "field initializedHeaders");
        check(hasField(c, "is"), "field is");
        check(hasField(c, "isDirectory"), "field isDirectory");
        check(hasField(c, "lastModified"), "field lastModified");
        check(hasField(c, "length"), "field length");
        check(hasField(c, "permission"), "field permission");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
