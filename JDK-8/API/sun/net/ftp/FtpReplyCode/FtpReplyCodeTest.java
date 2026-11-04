/*
 * @test
 * @summary sun.net.ftp.FtpReplyCode conformance: enum constants and members.
 * @run main FtpReplyCodeTest
 */

public class FtpReplyCodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ftp.FtpReplyCode", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.ftp.FtpReplyCode"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("find"), "declares method find");
        check(mnames.contains("getValue"), "declares method getValue");
        check(mnames.contains("isAuthentication"), "declares method isAuthentication");
        check(mnames.contains("isConnection"), "declares method isConnection");
        check(mnames.contains("isFileSystem"), "declares method isFileSystem");
        check(mnames.contains("isInformation"), "declares method isInformation");
        java.util.Set<String> consts = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) consts.add(f.getName());
        check(consts.contains("RESTART_MARKER"), "enum constant RESTART_MARKER");
        check(consts.contains("SERVICE_READY_IN"), "enum constant SERVICE_READY_IN");
        check(consts.contains("DATA_CONNECTION_ALREADY_OPEN"), "enum constant DATA_CONNECTION_ALREADY_OPEN");
        check(consts.contains("FILE_STATUS_OK"), "enum constant FILE_STATUS_OK");
        check(consts.contains("COMMAND_OK"), "enum constant COMMAND_OK");
        check(consts.contains("NOT_IMPLEMENTED"), "enum constant NOT_IMPLEMENTED");
        check(consts.contains("SYSTEM_STATUS"), "enum constant SYSTEM_STATUS");
        check(consts.contains("DIRECTORY_STATUS"), "enum constant DIRECTORY_STATUS");
        check(consts.contains("FILE_STATUS"), "enum constant FILE_STATUS");
        check(consts.contains("HELP_MESSAGE"), "enum constant HELP_MESSAGE");
        check(consts.contains("NAME_SYSTEM_TYPE"), "enum constant NAME_SYSTEM_TYPE");
        check(consts.contains("SERVICE_READY"), "enum constant SERVICE_READY");
        check(consts.contains("SERVICE_CLOSING"), "enum constant SERVICE_CLOSING");
        check(consts.contains("DATA_CONNECTION_OPEN"), "enum constant DATA_CONNECTION_OPEN");
        check(consts.contains("CLOSING_DATA_CONNECTION"), "enum constant CLOSING_DATA_CONNECTION");
        check(consts.contains("ENTERING_PASSIVE_MODE"), "enum constant ENTERING_PASSIVE_MODE");
        check(consts.contains("ENTERING_EXT_PASSIVE_MODE"), "enum constant ENTERING_EXT_PASSIVE_MODE");
        check(consts.contains("LOGGED_IN"), "enum constant LOGGED_IN");
        check(consts.contains("SECURELY_LOGGED_IN"), "enum constant SECURELY_LOGGED_IN");
        check(consts.contains("SECURITY_EXCHANGE_OK"), "enum constant SECURITY_EXCHANGE_OK");
        check(consts.contains("SECURITY_EXCHANGE_COMPLETE"), "enum constant SECURITY_EXCHANGE_COMPLETE");
        check(consts.contains("FILE_ACTION_OK"), "enum constant FILE_ACTION_OK");
        check(consts.contains("PATHNAME_CREATED"), "enum constant PATHNAME_CREATED");
        check(consts.contains("NEED_PASSWORD"), "enum constant NEED_PASSWORD");
        check(consts.contains("NEED_ACCOUNT"), "enum constant NEED_ACCOUNT");
        check(consts.contains("NEED_ADAT"), "enum constant NEED_ADAT");
        check(consts.contains("NEED_MORE_ADAT"), "enum constant NEED_MORE_ADAT");
        check(consts.contains("FILE_ACTION_PENDING"), "enum constant FILE_ACTION_PENDING");
        check(consts.contains("SERVICE_NOT_AVAILABLE"), "enum constant SERVICE_NOT_AVAILABLE");
        check(consts.contains("CANT_OPEN_DATA_CONNECTION"), "enum constant CANT_OPEN_DATA_CONNECTION");
        check(consts.contains("CONNECTION_CLOSED"), "enum constant CONNECTION_CLOSED");
        check(consts.contains("NEED_SECURITY_RESOURCE"), "enum constant NEED_SECURITY_RESOURCE");
        check(consts.contains("FILE_ACTION_NOT_TAKEN"), "enum constant FILE_ACTION_NOT_TAKEN");
        check(consts.contains("ACTION_ABORTED"), "enum constant ACTION_ABORTED");
        check(consts.contains("INSUFFICIENT_STORAGE"), "enum constant INSUFFICIENT_STORAGE");
        check(consts.contains("COMMAND_UNRECOGNIZED"), "enum constant COMMAND_UNRECOGNIZED");
        check(consts.contains("INVALID_PARAMETER"), "enum constant INVALID_PARAMETER");
        check(consts.contains("BAD_SEQUENCE"), "enum constant BAD_SEQUENCE");
        check(consts.contains("NOT_IMPLEMENTED_FOR_PARAMETER"), "enum constant NOT_IMPLEMENTED_FOR_PARAMETER");
        check(consts.contains("NOT_LOGGED_IN"), "enum constant NOT_LOGGED_IN");
        check(consts.contains("NEED_ACCOUNT_FOR_STORING"), "enum constant NEED_ACCOUNT_FOR_STORING");
        check(consts.contains("PROT_LEVEL_DENIED"), "enum constant PROT_LEVEL_DENIED");
        check(consts.contains("REQUEST_DENIED"), "enum constant REQUEST_DENIED");
        check(consts.contains("FAILED_SECURITY_CHECK"), "enum constant FAILED_SECURITY_CHECK");
        check(consts.contains("UNSUPPORTED_PROT_LEVEL"), "enum constant UNSUPPORTED_PROT_LEVEL");
        check(consts.contains("PROT_LEVEL_NOT_SUPPORTED_BY_SECURITY"), "enum constant PROT_LEVEL_NOT_SUPPORTED_BY_SECURITY");
        check(consts.contains("FILE_UNAVAILABLE"), "enum constant FILE_UNAVAILABLE");
        check(consts.contains("PAGE_TYPE_UNKNOWN"), "enum constant PAGE_TYPE_UNKNOWN");
        check(consts.contains("EXCEEDED_STORAGE"), "enum constant EXCEEDED_STORAGE");
        check(consts.contains("FILE_NAME_NOT_ALLOWED"), "enum constant FILE_NAME_NOT_ALLOWED");
        check(consts.contains("PROTECTED_REPLY"), "enum constant PROTECTED_REPLY");
        check(consts.contains("UNKNOWN_ERROR"), "enum constant UNKNOWN_ERROR");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
