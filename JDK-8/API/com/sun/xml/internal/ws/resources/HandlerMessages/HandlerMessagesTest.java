/*
 * @test
 * @summary com.sun.xml.internal.ws.resources.HandlerMessages conformance: reflection (kind, superclass, interfaces, methods).
 * @run main HandlerMessagesTest
 */

import java.lang.reflect.Modifier;


public class HandlerMessagesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.resources.HandlerMessages", false, HandlerMessagesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.resources.HandlerMessages"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("CANNOT_EXTEND_HANDLER_DIRECTLY"), "method CANNOT_EXTEND_HANDLER_DIRECTLY");
        check(mn.contains("CANNOT_INSTANTIATE_HANDLER"), "method CANNOT_INSTANTIATE_HANDLER");
        check(mn.contains("HANDLER_CHAIN_CONTAINS_HANDLER_ONLY"), "method HANDLER_CHAIN_CONTAINS_HANDLER_ONLY");
        check(mn.contains("HANDLER_MESSAGE_CONTEXT_INVALID_CLASS"), "method HANDLER_MESSAGE_CONTEXT_INVALID_CLASS");
        check(mn.contains("HANDLER_NESTED_ERROR"), "method HANDLER_NESTED_ERROR");
        check(mn.contains("HANDLER_NOT_VALID_TYPE"), "method HANDLER_NOT_VALID_TYPE");
        check(mn.contains("HANDLER_PREDESTROY_IGNORE"), "method HANDLER_PREDESTROY_IGNORE");
        check(mn.contains("localizableCANNOT_EXTEND_HANDLER_DIRECTLY"), "method localizableCANNOT_EXTEND_HANDLER_DIRECTLY");
        check(mn.contains("localizableCANNOT_INSTANTIATE_HANDLER"), "method localizableCANNOT_INSTANTIATE_HANDLER");
        check(mn.contains("localizableHANDLER_CHAIN_CONTAINS_HANDLER_ONLY"), "method localizableHANDLER_CHAIN_CONTAINS_HANDLER_ONLY");
        check(mn.contains("localizableHANDLER_MESSAGE_CONTEXT_INVALID_CLASS"), "method localizableHANDLER_MESSAGE_CONTEXT_INVALID_CLASS");
        check(mn.contains("localizableHANDLER_NESTED_ERROR"), "method localizableHANDLER_NESTED_ERROR");
        check(mn.contains("localizableHANDLER_NOT_VALID_TYPE"), "method localizableHANDLER_NOT_VALID_TYPE");
        check(mn.contains("localizableHANDLER_PREDESTROY_IGNORE"), "method localizableHANDLER_PREDESTROY_IGNORE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
