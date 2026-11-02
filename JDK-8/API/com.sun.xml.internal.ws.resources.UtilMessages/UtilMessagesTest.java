/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.UtilMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main UtilMessagesTest
 */

import java.lang.reflect.Modifier;


public class UtilMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.UtilMessages", false, UtilMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.UtilMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("UTIL_FAILED_TO_FIND_HANDLERCHAIN_FILE"), "method UTIL_FAILED_TO_FIND_HANDLERCHAIN_FILE");
        check(mn.contains("UTIL_FAILED_TO_PARSE_HANDLERCHAIN_FILE"), "method UTIL_FAILED_TO_PARSE_HANDLERCHAIN_FILE");
        check(mn.contains("UTIL_HANDLER_CANNOT_COMBINE_SOAPMESSAGEHANDLERS"), "method UTIL_HANDLER_CANNOT_COMBINE_SOAPMESSAGEHANDLERS");
        check(mn.contains("UTIL_HANDLER_CLASS_NOT_FOUND"), "method UTIL_HANDLER_CLASS_NOT_FOUND");
        check(mn.contains("UTIL_HANDLER_ENDPOINT_INTERFACE_NO_WEBSERVICE"), "method UTIL_HANDLER_ENDPOINT_INTERFACE_NO_WEBSERVICE");
        check(mn.contains("UTIL_HANDLER_NO_WEBSERVICE_ANNOTATION"), "method UTIL_HANDLER_NO_WEBSERVICE_ANNOTATION");
        check(mn.contains("UTIL_LOCATION"), "method UTIL_LOCATION");
        check(mn.contains("UTIL_PARSER_WRONG_ELEMENT"), "method UTIL_PARSER_WRONG_ELEMENT");
        check(mn.contains("localizableUTIL_FAILED_TO_FIND_HANDLERCHAIN_FILE"), "method localizableUTIL_FAILED_TO_FIND_HANDLERCHAIN_FILE");
        check(mn.contains("localizableUTIL_FAILED_TO_PARSE_HANDLERCHAIN_FILE"), "method localizableUTIL_FAILED_TO_PARSE_HANDLERCHAIN_FILE");
        check(mn.contains("localizableUTIL_HANDLER_CANNOT_COMBINE_SOAPMESSAGEHANDLERS"), "method localizableUTIL_HANDLER_CANNOT_COMBINE_SOAPMESSAGEHANDLERS");
        check(mn.contains("localizableUTIL_HANDLER_CLASS_NOT_FOUND"), "method localizableUTIL_HANDLER_CLASS_NOT_FOUND");
        check(mn.contains("localizableUTIL_HANDLER_ENDPOINT_INTERFACE_NO_WEBSERVICE"), "method localizableUTIL_HANDLER_ENDPOINT_INTERFACE_NO_WEBSERVICE");
        check(mn.contains("localizableUTIL_HANDLER_NO_WEBSERVICE_ANNOTATION"), "method localizableUTIL_HANDLER_NO_WEBSERVICE_ANNOTATION");
        check(mn.contains("localizableUTIL_LOCATION"), "method localizableUTIL_LOCATION");
        check(mn.contains("localizableUTIL_PARSER_WRONG_ELEMENT"), "method localizableUTIL_PARSER_WRONG_ELEMENT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
