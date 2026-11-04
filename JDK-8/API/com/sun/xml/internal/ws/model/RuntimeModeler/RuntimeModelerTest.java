/*
 * @test
 * @summary com.sun.xml.internal.ws.model.RuntimeModeler conformance: reflection (kind, superclass, interfaces, methods).
 * @run main RuntimeModelerTest
 */

import java.lang.reflect.Modifier;


public class RuntimeModelerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.model.RuntimeModeler", false, RuntimeModelerTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.model.RuntimeModeler"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("buildRuntimeModel"), "method buildRuntimeModel");
        check(mn.contains("capitalize"), "method capitalize");
        check(mn.contains("createBinding"), "method createBinding");
        check(mn.contains("determineWebMethodUse"), "method determineWebMethodUse");
        check(mn.contains("getNamespace"), "method getNamespace");
        check(mn.contains("getPortName"), "method getPortName");
        check(mn.contains("getPortTypeName"), "method getPortTypeName");
        check(mn.contains("getServiceName"), "method getServiceName");
        check(mn.contains("getWSDLExceptionFaultInfo"), "method getWSDLExceptionFaultInfo");
        check(mn.contains("processDocBareMethod"), "method processDocBareMethod");
        check(mn.contains("processDocWrappedMethod"), "method processDocWrappedMethod");
        check(mn.contains("processExceptions"), "method processExceptions");
        check(mn.contains("processRpcMethod"), "method processRpcMethod");
        check(mn.contains("setClassLoader"), "method setClassLoader");
        check(mn.contains("setPortName"), "method setPortName");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("BEAN"), "field BEAN");
        check(fn.contains("DecapitalizeExceptionBeanProperties"), "field DecapitalizeExceptionBeanProperties");
        check(fn.contains("DocWrappeeNamespapceQualified"), "field DocWrappeeNamespapceQualified");
        check(fn.contains("EXCEPTION_CLASS"), "field EXCEPTION_CLASS");
        check(fn.contains("HOLDER_CLASS"), "field HOLDER_CLASS");
        check(fn.contains("JAXWS_PACKAGE_PD"), "field JAXWS_PACKAGE_PD");
        check(fn.contains("PD_JAXWS_PACKAGE_PD"), "field PD_JAXWS_PACKAGE_PD");
        check(fn.contains("PORT"), "field PORT");
        check(fn.contains("REMOTE_EXCEPTION_CLASS"), "field REMOTE_EXCEPTION_CLASS");
        check(fn.contains("RESPONSE"), "field RESPONSE");
        check(fn.contains("RETURN"), "field RETURN");
        check(fn.contains("RUNTIME_EXCEPTION_CLASS"), "field RUNTIME_EXCEPTION_CLASS");
        check(fn.contains("SERVICE"), "field SERVICE");
        check(fn.contains("SuppressDocLitWrapperGeneration"), "field SuppressDocLitWrapperGeneration");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
