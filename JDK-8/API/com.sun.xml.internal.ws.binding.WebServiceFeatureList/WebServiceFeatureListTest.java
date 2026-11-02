/*
 * @test
 * @summary com.sun.xml.internal.ws.binding.WebServiceFeatureList conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WebServiceFeatureListTest
 */

import java.lang.reflect.Modifier;


public class WebServiceFeatureListTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.binding.WebServiceFeatureList", false, WebServiceFeatureListTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.binding.WebServiceFeatureList"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.util.AbstractMap"), "superclass java.util.AbstractMap");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.WSFeatureList"), "implements com.sun.xml.internal.ws.api.WSFeatureList");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("add"), "method add");
        check(mn.contains("addAll"), "method addAll");
        check(mn.contains("contains"), "method contains");
        check(mn.contains("entrySet"), "method entrySet");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("get"), "method get");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("getSoapVersion"), "method getSoapVersion");
        check(mn.contains("isEnabled"), "method isEnabled");
        check(mn.contains("isFeatureEnabled"), "method isFeatureEnabled");
        check(mn.contains("iterator"), "method iterator");
        check(mn.contains("mergeFeatures"), "method mergeFeatures");
        check(mn.contains("parseAnnotations"), "method parseAnnotations");
        check(mn.contains("put"), "method put");
        check(mn.contains("setParentFeaturedObject"), "method setParentFeaturedObject");
        check(mn.contains("toArray"), "method toArray");
        check(mn.contains("toFeatureArray"), "method toFeatureArray");
        check(mn.contains("toList"), "method toList");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("validate"), "method validate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
