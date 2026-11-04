/*
 * @test
 * @summary sun.net.ftp.impl.FtpClient conformance: internal package reflection.
 * @modules java.base/sun.net.ftp.impl:+open
 * @run main FtpClientTest
 */
public class FtpClientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ftp.impl.FtpClient", false, FtpClientTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.ftp.FtpClient"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "abort"), "method abort");
        check(hasMethod(c, "allocate"), "method allocate");
        check(hasMethod(c, "appendFile"), "method appendFile");
        check(hasMethod(c, "changeDirectory"), "method changeDirectory");
        check(hasMethod(c, "changeToParentDirectory"), "method changeToParentDirectory");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "completePending"), "method completePending");
        check(hasMethod(c, "connect"), "method connect");
        check(hasMethod(c, "create"), "method create");
        check(hasMethod(c, "deleteFile"), "method deleteFile");
        check(hasMethod(c, "enablePassiveMode"), "method enablePassiveMode");
        check(hasMethod(c, "endSecureSession"), "method endSecureSession");
        check(hasMethod(c, "getConnectTimeout"), "method getConnectTimeout");
        check(hasMethod(c, "getFeatures"), "method getFeatures");
        check(hasMethod(c, "getFile"), "method getFile");
        check(hasMethod(c, "getFileStream"), "method getFileStream");
        check(hasMethod(c, "getHelp"), "method getHelp");
        check(hasMethod(c, "getLastFileName"), "method getLastFileName");
        check(hasMethod(c, "getLastModified"), "method getLastModified");
        check(hasMethod(c, "getLastReplyCode"), "method getLastReplyCode");
        check(hasMethod(c, "getLastResponseString"), "method getLastResponseString");
        check(hasMethod(c, "getLastTransferSize"), "method getLastTransferSize");
        check(hasMethod(c, "getProxy"), "method getProxy");
        check(hasMethod(c, "getReadTimeout"), "method getReadTimeout");
        check(hasMethod(c, "getServerAddress"), "method getServerAddress");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getStatus"), "method getStatus");
        check(hasMethod(c, "getSystem"), "method getSystem");
        check(hasMethod(c, "getWelcomeMsg"), "method getWelcomeMsg");
        check(hasMethod(c, "getWorkingDirectory"), "method getWorkingDirectory");
        check(hasMethod(c, "isConnected"), "method isConnected");
        check(hasMethod(c, "isLoggedIn"), "method isLoggedIn");
        check(hasMethod(c, "isPassiveModeEnabled"), "method isPassiveModeEnabled");
        check(hasMethod(c, "list"), "method list");
        check(hasMethod(c, "listFiles"), "method listFiles");
        check(hasMethod(c, "login"), "method login");
        check(hasMethod(c, "makeDirectory"), "method makeDirectory");
        check(hasMethod(c, "nameList"), "method nameList");
        check(hasMethod(c, "noop"), "method noop");
        check(hasMethod(c, "putFile"), "method putFile");
        check(hasMethod(c, "putFileStream"), "method putFileStream");
        check(hasMethod(c, "reInit"), "method reInit");
        check(hasMethod(c, "removeDirectory"), "method removeDirectory");
        check(hasMethod(c, "rename"), "method rename");
        check(hasMethod(c, "setConnectTimeout"), "method setConnectTimeout");
        check(hasMethod(c, "setDirParser"), "method setDirParser");
        check(hasMethod(c, "setProxy"), "method setProxy");
        check(hasMethod(c, "setReadTimeout"), "method setReadTimeout");
        check(hasMethod(c, "setRestartOffset"), "method setRestartOffset");
        check(hasMethod(c, "setType"), "method setType");
        check(hasMethod(c, "siteCmd"), "method siteCmd");
        check(hasMethod(c, "startSecureSession"), "method startSecureSession");
        check(hasMethod(c, "structureMount"), "method structureMount");
        check(hasMethod(c, "useKerberos"), "method useKerberos");
        check(hasField(c, "ERROR_MSG"), "field ERROR_MSG");
        check(hasField(c, "RFC3659_DATETIME_FORMAT"), "field RFC3659_DATETIME_FORMAT");
        check(hasField(c, "acceptPasvAddressVal"), "field acceptPasvAddressVal");
        check(hasField(c, "connectTimeout"), "field connectTimeout");
        check(hasField(c, "defaultConnectTimeout"), "field defaultConnectTimeout");
        check(hasField(c, "defaultSoTimeout"), "field defaultSoTimeout");
        check(hasField(c, "df"), "field df");
        check(hasField(c, "encoding"), "field encoding");
        check(hasField(c, "epsvPat"), "field epsvPat");
        check(hasField(c, "in"), "field in");
        check(hasField(c, "lastFileName"), "field lastFileName");
        check(hasField(c, "lastReplyCode"), "field lastReplyCode");
        check(hasField(c, "lastTransSize"), "field lastTransSize");
        check(hasField(c, "linkp"), "field linkp");
        check(hasField(c, "loggedIn"), "field loggedIn");
        check(hasField(c, "logger"), "field logger");
        check(hasField(c, "mlsxParser"), "field mlsxParser");
        check(hasField(c, "oldSocket"), "field oldSocket");
        check(hasField(c, "out"), "field out");
        check(hasField(c, "parser"), "field parser");
        check(hasField(c, "passiveMode"), "field passiveMode");
        check(hasField(c, "pasvPat"), "field pasvPat");
        check(hasField(c, "patStrings"), "field patStrings");
        check(hasField(c, "patternGroups"), "field patternGroups");
        check(hasField(c, "patterns"), "field patterns");
        check(hasField(c, "proxy"), "field proxy");
        check(hasField(c, "readTimeout"), "field readTimeout");
        check(hasField(c, "replyPending"), "field replyPending");
        check(hasField(c, "restartOffset"), "field restartOffset");
        check(hasField(c, "server"), "field server");
        check(hasField(c, "serverAddr"), "field serverAddr");
        check(hasField(c, "serverResponse"), "field serverResponse");
        check(hasField(c, "sslFact"), "field sslFact");
        check(hasField(c, "transPat"), "field transPat");
        check(hasField(c, "type"), "field type");
        check(hasField(c, "useCrypto"), "field useCrypto");
        check(hasField(c, "welcomeMsg"), "field welcomeMsg");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
