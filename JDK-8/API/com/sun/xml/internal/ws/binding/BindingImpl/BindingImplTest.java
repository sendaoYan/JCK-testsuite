/*
 * @test
 * @summary com.sun.xml.internal.ws.binding.BindingImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main BindingImplTest
 */

import java.lang.reflect.Modifier;


public class BindingImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.binding.BindingImpl", false, BindingImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.binding.BindingImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.WSBinding"), "implements com.sun.xml.internal.ws.api.WSBinding");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addKnownHeader"), "method addKnownHeader");
        check(mn.contains("create"), "method create");
        check(mn.contains("createCodec"), "method createCodec");
        check(mn.contains("getAddressingVersion"), "method getAddressingVersion");
        check(mn.contains("getBindingID"), "method getBindingID");
        check(mn.contains("getBindingId"), "method getBindingId");
        check(mn.contains("getDefaultBinding"), "method getDefaultBinding");
        check(mn.contains("getFaultMessageFeatures"), "method getFaultMessageFeatures");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("getFeatures"), "method getFeatures");
        check(mn.contains("getHandlerChain"), "method getHandlerChain");
        check(mn.contains("getHandlerConfig"), "method getHandlerConfig");
        check(mn.contains("getInputMessageFeatures"), "method getInputMessageFeatures");
        check(mn.contains("getKnownHeaders"), "method getKnownHeaders");
        check(mn.contains("getMessageContextFactory"), "method getMessageContextFactory");
        check(mn.contains("getOperationFeature"), "method getOperationFeature");
        check(mn.contains("getOperationFeatures"), "method getOperationFeatures");
        check(mn.contains("getOutputMessageFeatures"), "method getOutputMessageFeatures");
        check(mn.contains("getSOAPVersion"), "method getSOAPVersion");
        check(mn.contains("initializeJavaActivationHandlers"), "method initializeJavaActivationHandlers");
        check(mn.contains("isFeatureEnabled"), "method isFeatureEnabled");
        check(mn.contains("isOperationFeatureEnabled"), "method isOperationFeatureEnabled");
        check(mn.contains("setFaultMessageFeatures"), "method setFaultMessageFeatures");
        check(mn.contains("setHandlerConfig"), "method setHandlerConfig");
        check(mn.contains("setInputMessageFeatures"), "method setInputMessageFeatures");
        check(mn.contains("setMode"), "method setMode");
        check(mn.contains("setOperationFeatures"), "method setOperationFeatures");
        check(mn.contains("setOutputMessageFeatures"), "method setOutputMessageFeatures");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("EMPTY_FEATURES"), "field EMPTY_FEATURES");
        check(fn.contains("faultMessageFeatures"), "field faultMessageFeatures");
        check(fn.contains("features"), "field features");
        check(fn.contains("inputMessageFeatures"), "field inputMessageFeatures");
        check(fn.contains("messageContextFactory"), "field messageContextFactory");
        check(fn.contains("operationFeatures"), "field operationFeatures");
        check(fn.contains("outputMessageFeatures"), "field outputMessageFeatures");
        check(fn.contains("serviceMode"), "field serviceMode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
