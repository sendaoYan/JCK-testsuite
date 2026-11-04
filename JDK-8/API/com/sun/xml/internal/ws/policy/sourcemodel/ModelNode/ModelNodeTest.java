/*
 * @test
 * @summary com.sun.xml.internal.ws.policy.sourcemodel.ModelNode conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ModelNodeTest
 */

import java.lang.reflect.Modifier;


public class ModelNodeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.policy.sourcemodel.ModelNode", false, ModelNodeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.policy.sourcemodel.ModelNode"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        check(has(ifs, "java.lang.Iterable"), "implements java.lang.Iterable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("childrenSize"), "method childrenSize");
        check(mn.contains("clone"), "method clone");
        check(mn.contains("createChildAllNode"), "method createChildAllNode");
        check(mn.contains("createChildAssertionNode"), "method createChildAssertionNode");
        check(mn.contains("createChildAssertionParameterNode"), "method createChildAssertionParameterNode");
        check(mn.contains("createChildExactlyOneNode"), "method createChildExactlyOneNode");
        check(mn.contains("createChildPolicyNode"), "method createChildPolicyNode");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getNodeData"), "method getNodeData");
        check(mn.contains("getParentModel"), "method getParentModel");
        check(mn.contains("getParentNode"), "method getParentNode");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("hasChildren"), "method hasChildren");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("iterator"), "method iterator");
        check(mn.contains("setOrReplaceNodeData"), "method setOrReplaceNodeData");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
