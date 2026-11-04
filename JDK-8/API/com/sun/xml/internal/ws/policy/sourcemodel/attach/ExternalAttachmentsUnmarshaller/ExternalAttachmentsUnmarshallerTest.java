/*
 * @test
 * @summary com.sun.xml.internal.ws.policy.sourcemodel.attach.ExternalAttachmentsUnmarshaller conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ExternalAttachmentsUnmarshallerTest
 */

import java.lang.reflect.Modifier;


public class ExternalAttachmentsUnmarshallerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.policy.sourcemodel.attach.ExternalAttachmentsUnmarshaller", false, ExternalAttachmentsUnmarshallerTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.policy.sourcemodel.attach.ExternalAttachmentsUnmarshaller"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("unmarshal"), "method unmarshal");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("BINDING_ID"), "field BINDING_ID");
        check(fn.contains("BINDING_OPERATION_FAULT_ID"), "field BINDING_OPERATION_FAULT_ID");
        check(fn.contains("BINDING_OPERATION_ID"), "field BINDING_OPERATION_ID");
        check(fn.contains("BINDING_OPERATION_INPUT_ID"), "field BINDING_OPERATION_INPUT_ID");
        check(fn.contains("BINDING_OPERATION_OUTPUT_ID"), "field BINDING_OPERATION_OUTPUT_ID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
