/*
 * @test
 * @summary sun.net.util.IPAddressUtil conformance: internal package reflection.
 * @modules java.base/sun.net.util:+open
 * @run main IPAddressUtilTest
 */
public class IPAddressUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.util.IPAddressUtil", false, IPAddressUtilTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "checkAuth"), "method checkAuth");
        check(hasMethod(c, "checkAuthority"), "method checkAuthority");
        check(hasMethod(c, "checkExternalForm"), "method checkExternalForm");
        check(hasMethod(c, "checkHostString"), "method checkHostString");
        check(hasMethod(c, "checkUserInfo"), "method checkUserInfo");
        check(hasMethod(c, "convertFromIPv4MappedAddress"), "method convertFromIPv4MappedAddress");
        check(hasMethod(c, "delayURLParsing"), "method delayURLParsing");
        check(hasMethod(c, "digit"), "method digit");
        check(hasMethod(c, "earlyURLParsing"), "method earlyURLParsing");
        check(hasMethod(c, "invalidIpAddressLiteral"), "method invalidIpAddressLiteral");
        check(hasMethod(c, "isBsdParsableV4"), "method isBsdParsableV4");
        check(hasMethod(c, "isIPv4LiteralAddress"), "method isIPv4LiteralAddress");
        check(hasMethod(c, "isIPv6LiteralAddress"), "method isIPv6LiteralAddress");
        check(hasMethod(c, "match"), "method match");
        check(hasMethod(c, "parseAsciiDigit"), "method parseAsciiDigit");
        check(hasMethod(c, "parseBsdLiteralV4"), "method parseBsdLiteralV4");
        check(hasMethod(c, "scan"), "method scan");
        check(hasMethod(c, "textToNumericFormatV4"), "method textToNumericFormatV4");
        check(hasMethod(c, "textToNumericFormatV6"), "method textToNumericFormatV6");
        check(hasMethod(c, "toScopedAddress"), "method toScopedAddress");
        check(hasMethod(c, "validateNumericFormatV4"), "method validateNumericFormatV4");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "ALLOW_AMBIGUOUS_IPADDRESS_LITERALS_SP"), "field ALLOW_AMBIGUOUS_IPADDRESS_LITERALS_SP");
        check(hasField(c, "ALLOW_AMBIGUOUS_IPADDRESS_LITERALS_SP_VALUE"), "field ALLOW_AMBIGUOUS_IPADDRESS_LITERALS_SP_VALUE");
        check(hasField(c, "CANT_PARSE_IN_RADIX"), "field CANT_PARSE_IN_RADIX");
        check(hasField(c, "DECIMAL"), "field DECIMAL");
        check(hasField(c, "HEXADECIMAL"), "field HEXADECIMAL");
        check(hasField(c, "H_AUTH_DELIMS"), "field H_AUTH_DELIMS");
        check(hasField(c, "H_BACKSLASH"), "field H_BACKSLASH");
        check(hasField(c, "H_COLON"), "field H_COLON");
        check(hasField(c, "H_EXCLUDE"), "field H_EXCLUDE");
        check(hasField(c, "H_EXCLUDED_DELIMS"), "field H_EXCLUDED_DELIMS");
        check(hasField(c, "H_FRAGMENT"), "field H_FRAGMENT");
        check(hasField(c, "H_GEN_DELIMS"), "field H_GEN_DELIMS");
        check(hasField(c, "H_IPV6_DELIMS"), "field H_IPV6_DELIMS");
        check(hasField(c, "H_NON_PRINTABLE"), "field H_NON_PRINTABLE");
        check(hasField(c, "H_QUERY"), "field H_QUERY");
        check(hasField(c, "H_SLASH"), "field H_SLASH");
        check(hasField(c, "H_UNWISE"), "field H_UNWISE");
        check(hasField(c, "INADDR16SZ"), "field INADDR16SZ");
        check(hasField(c, "INADDR4SZ"), "field INADDR4SZ");
        check(hasField(c, "INT16SZ"), "field INT16SZ");
        check(hasField(c, "L_AUTH_DELIMS"), "field L_AUTH_DELIMS");
        check(hasField(c, "L_BACKSLASH"), "field L_BACKSLASH");
        check(hasField(c, "L_COLON"), "field L_COLON");
        check(hasField(c, "L_EXCLUDE"), "field L_EXCLUDE");
        check(hasField(c, "L_EXCLUDED_DELIMS"), "field L_EXCLUDED_DELIMS");
        check(hasField(c, "L_FRAGMENT"), "field L_FRAGMENT");
        check(hasField(c, "L_GEN_DELIMS"), "field L_GEN_DELIMS");
        check(hasField(c, "L_IPV6_DELIMS"), "field L_IPV6_DELIMS");
        check(hasField(c, "L_NON_PRINTABLE"), "field L_NON_PRINTABLE");
        check(hasField(c, "L_QUERY"), "field L_QUERY");
        check(hasField(c, "L_SLASH"), "field L_SLASH");
        check(hasField(c, "L_UNWISE"), "field L_UNWISE");
        check(hasField(c, "OCTAL"), "field OCTAL");
        check(hasField(c, "OTHERS"), "field OTHERS");
        check(hasField(c, "SUPPORTED_RADIXES"), "field SUPPORTED_RADIXES");
        check(hasField(c, "TERMINAL_PARSE_ERROR"), "field TERMINAL_PARSE_ERROR");
        check(hasField(c, "cache"), "field cache");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
