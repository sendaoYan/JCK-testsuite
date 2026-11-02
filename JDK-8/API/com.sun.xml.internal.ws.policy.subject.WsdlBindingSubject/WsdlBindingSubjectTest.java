/*
 * @test
 * @summary com.sun.xml.internal.ws.policy.subject.WsdlBindingSubject conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WsdlBindingSubjectTest
 */

import java.lang.reflect.Modifier;


public class WsdlBindingSubjectTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.policy.subject.WsdlBindingSubject", false, WsdlBindingSubjectTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.policy.subject.WsdlBindingSubject"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createBindingMessageSubject"), "method createBindingMessageSubject");
        check(mn.contains("createBindingOperationSubject"), "method createBindingOperationSubject");
        check(mn.contains("createBindingSubject"), "method createBindingSubject");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getMessageType"), "method getMessageType");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getParent"), "method getParent");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("isBindingMessageSubject"), "method isBindingMessageSubject");
        check(mn.contains("isBindingOperationSubject"), "method isBindingOperationSubject");
        check(mn.contains("isBindingSubject"), "method isBindingSubject");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
