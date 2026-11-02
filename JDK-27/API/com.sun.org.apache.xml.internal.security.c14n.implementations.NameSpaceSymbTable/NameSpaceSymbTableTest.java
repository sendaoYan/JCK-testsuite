/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.implementations.NameSpaceSymbTable conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.c14n.implementations:+open
 * @run main NameSpaceSymbTableTest
 */
import java.lang.reflect.*;
import java.util.*;
public class NameSpaceSymbTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.c14n.implementations.NameSpaceSymbTable", false, NameSpaceSymbTableTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.c14n.implementations.NameSpaceSymbTable"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("addMapping"), "method:addMapping");
        check(methods.contains("addMappingAndRender"), "method:addMappingAndRender");
        check(methods.contains("getLevel"), "method:getLevel");
        check(methods.contains("getMapping"), "method:getMapping");
        check(methods.contains("getMappingWithoutRendered"), "method:getMappingWithoutRendered");
        check(methods.contains("getUnrenderedNodes"), "method:getUnrenderedNodes");
        check(methods.contains("outputNodePop"), "method:outputNodePop");
        check(methods.contains("outputNodePush"), "method:outputNodePush");
        check(methods.contains("pop"), "method:pop");
        check(methods.contains("push"), "method:push");
        check(methods.contains("removeMapping"), "method:removeMapping");
        check(methods.contains("removeMappingIfNotRender"), "method:removeMappingIfNotRender");
        check(methods.contains("removeMappingIfRender"), "method:removeMappingIfRender");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
