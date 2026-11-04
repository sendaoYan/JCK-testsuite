/*
 * @test
 * @summary com.sun.xml.internal.ws.policy.PolicyMap conformance: reflection (kind, superclass, interfaces, methods).
 * @run main PolicyMapTest
 */

import java.lang.reflect.Modifier;


public class PolicyMapTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.policy.PolicyMap", false, PolicyMapTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.policy.PolicyMap"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.Iterable"), "implements java.lang.Iterable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createPolicyMap"), "method createPolicyMap");
        check(mn.contains("createWsdlEndpointScopeKey"), "method createWsdlEndpointScopeKey");
        check(mn.contains("createWsdlFaultMessageScopeKey"), "method createWsdlFaultMessageScopeKey");
        check(mn.contains("createWsdlMessageScopeKey"), "method createWsdlMessageScopeKey");
        check(mn.contains("createWsdlOperationScopeKey"), "method createWsdlOperationScopeKey");
        check(mn.contains("createWsdlServiceScopeKey"), "method createWsdlServiceScopeKey");
        check(mn.contains("getAllEndpointScopeKeys"), "method getAllEndpointScopeKeys");
        check(mn.contains("getAllFaultMessageScopeKeys"), "method getAllFaultMessageScopeKeys");
        check(mn.contains("getAllInputMessageScopeKeys"), "method getAllInputMessageScopeKeys");
        check(mn.contains("getAllOperationScopeKeys"), "method getAllOperationScopeKeys");
        check(mn.contains("getAllOutputMessageScopeKeys"), "method getAllOutputMessageScopeKeys");
        check(mn.contains("getAllServiceScopeKeys"), "method getAllServiceScopeKeys");
        check(mn.contains("getEndpointEffectivePolicy"), "method getEndpointEffectivePolicy");
        check(mn.contains("getFaultMessageEffectivePolicy"), "method getFaultMessageEffectivePolicy");
        check(mn.contains("getInputMessageEffectivePolicy"), "method getInputMessageEffectivePolicy");
        check(mn.contains("getOperationEffectivePolicy"), "method getOperationEffectivePolicy");
        check(mn.contains("getOutputMessageEffectivePolicy"), "method getOutputMessageEffectivePolicy");
        check(mn.contains("getPolicySubjects"), "method getPolicySubjects");
        check(mn.contains("getServiceEffectivePolicy"), "method getServiceEffectivePolicy");
        check(mn.contains("isEmpty"), "method isEmpty");
        check(mn.contains("isFaultMessageSubject"), "method isFaultMessageSubject");
        check(mn.contains("isInputMessageSubject"), "method isInputMessageSubject");
        check(mn.contains("isOutputMessageSubject"), "method isOutputMessageSubject");
        check(mn.contains("iterator"), "method iterator");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
