/*
 * @test
 * @summary sun.net.ftp.FtpReplyCode conformance: internal package reflection.
 * @modules java.base/sun.net.ftp:+open
 * @run main FtpReplyCodeTest
 */
public class FtpReplyCodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.ftp.FtpReplyCode", false, FtpReplyCodeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "find"), "method find");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "isAuthentication"), "method isAuthentication");
        check(hasMethod(c, "isConnection"), "method isConnection");
        check(hasMethod(c, "isFileSystem"), "method isFileSystem");
        check(hasMethod(c, "isInformation"), "method isInformation");
        check(hasMethod(c, "isPermanentNegative"), "method isPermanentNegative");
        check(hasMethod(c, "isPositiveCompletion"), "method isPositiveCompletion");
        check(hasMethod(c, "isPositiveIntermediate"), "method isPositiveIntermediate");
        check(hasMethod(c, "isPositivePreliminary"), "method isPositivePreliminary");
        check(hasMethod(c, "isProtectedReply"), "method isProtectedReply");
        check(hasMethod(c, "isSyntax"), "method isSyntax");
        check(hasMethod(c, "isTransientNegative"), "method isTransientNegative");
        check(hasMethod(c, "isUnspecified"), "method isUnspecified");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "$VALUES"), "field $VALUES");
        check(hasField(c, "ACTION_ABORTED"), "field ACTION_ABORTED");
        check(hasField(c, "BAD_SEQUENCE"), "field BAD_SEQUENCE");
        check(hasField(c, "CANT_OPEN_DATA_CONNECTION"), "field CANT_OPEN_DATA_CONNECTION");
        check(hasField(c, "CLOSING_DATA_CONNECTION"), "field CLOSING_DATA_CONNECTION");
        check(hasField(c, "COMMAND_OK"), "field COMMAND_OK");
        check(hasField(c, "COMMAND_UNRECOGNIZED"), "field COMMAND_UNRECOGNIZED");
        check(hasField(c, "CONNECTION_CLOSED"), "field CONNECTION_CLOSED");
        check(hasField(c, "DATA_CONNECTION_ALREADY_OPEN"), "field DATA_CONNECTION_ALREADY_OPEN");
        check(hasField(c, "DATA_CONNECTION_OPEN"), "field DATA_CONNECTION_OPEN");
        check(hasField(c, "DIRECTORY_STATUS"), "field DIRECTORY_STATUS");
        check(hasField(c, "ENTERING_EXT_PASSIVE_MODE"), "field ENTERING_EXT_PASSIVE_MODE");
        check(hasField(c, "ENTERING_PASSIVE_MODE"), "field ENTERING_PASSIVE_MODE");
        check(hasField(c, "EXCEEDED_STORAGE"), "field EXCEEDED_STORAGE");
        check(hasField(c, "FAILED_SECURITY_CHECK"), "field FAILED_SECURITY_CHECK");
        check(hasField(c, "FILE_ACTION_NOT_TAKEN"), "field FILE_ACTION_NOT_TAKEN");
        check(hasField(c, "FILE_ACTION_OK"), "field FILE_ACTION_OK");
        check(hasField(c, "FILE_ACTION_PENDING"), "field FILE_ACTION_PENDING");
        check(hasField(c, "FILE_NAME_NOT_ALLOWED"), "field FILE_NAME_NOT_ALLOWED");
        check(hasField(c, "FILE_STATUS"), "field FILE_STATUS");
        check(hasField(c, "FILE_STATUS_OK"), "field FILE_STATUS_OK");
        check(hasField(c, "FILE_UNAVAILABLE"), "field FILE_UNAVAILABLE");
        check(hasField(c, "HELP_MESSAGE"), "field HELP_MESSAGE");
        check(hasField(c, "INSUFFICIENT_STORAGE"), "field INSUFFICIENT_STORAGE");
        check(hasField(c, "INVALID_PARAMETER"), "field INVALID_PARAMETER");
        check(hasField(c, "LOGGED_IN"), "field LOGGED_IN");
        check(hasField(c, "NAME_SYSTEM_TYPE"), "field NAME_SYSTEM_TYPE");
        check(hasField(c, "NEED_ACCOUNT"), "field NEED_ACCOUNT");
        check(hasField(c, "NEED_ACCOUNT_FOR_STORING"), "field NEED_ACCOUNT_FOR_STORING");
        check(hasField(c, "NEED_ADAT"), "field NEED_ADAT");
        check(hasField(c, "NEED_MORE_ADAT"), "field NEED_MORE_ADAT");
        check(hasField(c, "NEED_PASSWORD"), "field NEED_PASSWORD");
        check(hasField(c, "NEED_SECURITY_RESOURCE"), "field NEED_SECURITY_RESOURCE");
        check(hasField(c, "NOT_IMPLEMENTED"), "field NOT_IMPLEMENTED");
        check(hasField(c, "NOT_IMPLEMENTED_FOR_PARAMETER"), "field NOT_IMPLEMENTED_FOR_PARAMETER");
        check(hasField(c, "NOT_LOGGED_IN"), "field NOT_LOGGED_IN");
        check(hasField(c, "PAGE_TYPE_UNKNOWN"), "field PAGE_TYPE_UNKNOWN");
        check(hasField(c, "PATHNAME_CREATED"), "field PATHNAME_CREATED");
        check(hasField(c, "PROTECTED_REPLY"), "field PROTECTED_REPLY");
        check(hasField(c, "PROT_LEVEL_DENIED"), "field PROT_LEVEL_DENIED");
        check(hasField(c, "PROT_LEVEL_NOT_SUPPORTED_BY_SECURITY"), "field PROT_LEVEL_NOT_SUPPORTED_BY_SECURITY");
        check(hasField(c, "REQUEST_DENIED"), "field REQUEST_DENIED");
        check(hasField(c, "RESTART_MARKER"), "field RESTART_MARKER");
        check(hasField(c, "SECURELY_LOGGED_IN"), "field SECURELY_LOGGED_IN");
        check(hasField(c, "SECURITY_EXCHANGE_COMPLETE"), "field SECURITY_EXCHANGE_COMPLETE");
        check(hasField(c, "SECURITY_EXCHANGE_OK"), "field SECURITY_EXCHANGE_OK");
        check(hasField(c, "SERVICE_CLOSING"), "field SERVICE_CLOSING");
        check(hasField(c, "SERVICE_NOT_AVAILABLE"), "field SERVICE_NOT_AVAILABLE");
        check(hasField(c, "SERVICE_READY"), "field SERVICE_READY");
        check(hasField(c, "SERVICE_READY_IN"), "field SERVICE_READY_IN");
        check(hasField(c, "SYSTEM_STATUS"), "field SYSTEM_STATUS");
        check(hasField(c, "UNKNOWN_ERROR"), "field UNKNOWN_ERROR");
        check(hasField(c, "UNSUPPORTED_PROT_LEVEL"), "field UNSUPPORTED_PROT_LEVEL");
        check(hasField(c, "value"), "field value");
        check(hasEnumConstant(c, "ACTION_ABORTED"), "enum ACTION_ABORTED");
        check(hasEnumConstant(c, "BAD_SEQUENCE"), "enum BAD_SEQUENCE");
        check(hasEnumConstant(c, "CANT_OPEN_DATA_CONNECTION"), "enum CANT_OPEN_DATA_CONNECTION");
        check(hasEnumConstant(c, "CLOSING_DATA_CONNECTION"), "enum CLOSING_DATA_CONNECTION");
        check(hasEnumConstant(c, "COMMAND_OK"), "enum COMMAND_OK");
        check(hasEnumConstant(c, "COMMAND_UNRECOGNIZED"), "enum COMMAND_UNRECOGNIZED");
        check(hasEnumConstant(c, "CONNECTION_CLOSED"), "enum CONNECTION_CLOSED");
        check(hasEnumConstant(c, "DATA_CONNECTION_ALREADY_OPEN"), "enum DATA_CONNECTION_ALREADY_OPEN");
        check(hasEnumConstant(c, "DATA_CONNECTION_OPEN"), "enum DATA_CONNECTION_OPEN");
        check(hasEnumConstant(c, "DIRECTORY_STATUS"), "enum DIRECTORY_STATUS");
        check(hasEnumConstant(c, "ENTERING_EXT_PASSIVE_MODE"), "enum ENTERING_EXT_PASSIVE_MODE");
        check(hasEnumConstant(c, "ENTERING_PASSIVE_MODE"), "enum ENTERING_PASSIVE_MODE");
        check(hasEnumConstant(c, "EXCEEDED_STORAGE"), "enum EXCEEDED_STORAGE");
        check(hasEnumConstant(c, "FAILED_SECURITY_CHECK"), "enum FAILED_SECURITY_CHECK");
        check(hasEnumConstant(c, "FILE_ACTION_NOT_TAKEN"), "enum FILE_ACTION_NOT_TAKEN");
        check(hasEnumConstant(c, "FILE_ACTION_OK"), "enum FILE_ACTION_OK");
        check(hasEnumConstant(c, "FILE_ACTION_PENDING"), "enum FILE_ACTION_PENDING");
        check(hasEnumConstant(c, "FILE_NAME_NOT_ALLOWED"), "enum FILE_NAME_NOT_ALLOWED");
        check(hasEnumConstant(c, "FILE_STATUS"), "enum FILE_STATUS");
        check(hasEnumConstant(c, "FILE_STATUS_OK"), "enum FILE_STATUS_OK");
        check(hasEnumConstant(c, "FILE_UNAVAILABLE"), "enum FILE_UNAVAILABLE");
        check(hasEnumConstant(c, "HELP_MESSAGE"), "enum HELP_MESSAGE");
        check(hasEnumConstant(c, "INSUFFICIENT_STORAGE"), "enum INSUFFICIENT_STORAGE");
        check(hasEnumConstant(c, "INVALID_PARAMETER"), "enum INVALID_PARAMETER");
        check(hasEnumConstant(c, "LOGGED_IN"), "enum LOGGED_IN");
        check(hasEnumConstant(c, "NAME_SYSTEM_TYPE"), "enum NAME_SYSTEM_TYPE");
        check(hasEnumConstant(c, "NEED_ACCOUNT"), "enum NEED_ACCOUNT");
        check(hasEnumConstant(c, "NEED_ACCOUNT_FOR_STORING"), "enum NEED_ACCOUNT_FOR_STORING");
        check(hasEnumConstant(c, "NEED_ADAT"), "enum NEED_ADAT");
        check(hasEnumConstant(c, "NEED_MORE_ADAT"), "enum NEED_MORE_ADAT");
        check(hasEnumConstant(c, "NEED_PASSWORD"), "enum NEED_PASSWORD");
        check(hasEnumConstant(c, "NEED_SECURITY_RESOURCE"), "enum NEED_SECURITY_RESOURCE");
        check(hasEnumConstant(c, "NOT_IMPLEMENTED"), "enum NOT_IMPLEMENTED");
        check(hasEnumConstant(c, "NOT_IMPLEMENTED_FOR_PARAMETER"), "enum NOT_IMPLEMENTED_FOR_PARAMETER");
        check(hasEnumConstant(c, "NOT_LOGGED_IN"), "enum NOT_LOGGED_IN");
        check(hasEnumConstant(c, "PAGE_TYPE_UNKNOWN"), "enum PAGE_TYPE_UNKNOWN");
        check(hasEnumConstant(c, "PATHNAME_CREATED"), "enum PATHNAME_CREATED");
        check(hasEnumConstant(c, "PROTECTED_REPLY"), "enum PROTECTED_REPLY");
        check(hasEnumConstant(c, "PROT_LEVEL_DENIED"), "enum PROT_LEVEL_DENIED");
        check(hasEnumConstant(c, "PROT_LEVEL_NOT_SUPPORTED_BY_SECURITY"), "enum PROT_LEVEL_NOT_SUPPORTED_BY_SECURITY");
        check(hasEnumConstant(c, "REQUEST_DENIED"), "enum REQUEST_DENIED");
        check(hasEnumConstant(c, "RESTART_MARKER"), "enum RESTART_MARKER");
        check(hasEnumConstant(c, "SECURELY_LOGGED_IN"), "enum SECURELY_LOGGED_IN");
        check(hasEnumConstant(c, "SECURITY_EXCHANGE_COMPLETE"), "enum SECURITY_EXCHANGE_COMPLETE");
        check(hasEnumConstant(c, "SECURITY_EXCHANGE_OK"), "enum SECURITY_EXCHANGE_OK");
        check(hasEnumConstant(c, "SERVICE_CLOSING"), "enum SERVICE_CLOSING");
        check(hasEnumConstant(c, "SERVICE_NOT_AVAILABLE"), "enum SERVICE_NOT_AVAILABLE");
        check(hasEnumConstant(c, "SERVICE_READY"), "enum SERVICE_READY");
        check(hasEnumConstant(c, "SERVICE_READY_IN"), "enum SERVICE_READY_IN");
        check(hasEnumConstant(c, "SYSTEM_STATUS"), "enum SYSTEM_STATUS");
        check(hasEnumConstant(c, "UNKNOWN_ERROR"), "enum UNKNOWN_ERROR");
        check(hasEnumConstant(c, "UNSUPPORTED_PROT_LEVEL"), "enum UNSUPPORTED_PROT_LEVEL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
