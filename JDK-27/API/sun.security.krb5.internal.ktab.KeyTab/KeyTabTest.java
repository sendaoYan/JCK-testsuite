/*
 * @test
 * @summary sun.security.krb5.internal.ktab.KeyTab conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.ktab:+open
 * @run main KeyTabTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KeyTabTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.ktab.KeyTab", false, KeyTabTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.ktab.KeyTab"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("sun.security.krb5.internal.ktab.KeyTabConstants"), "iface:sun.security.krb5.internal.ktab.KeyTabConstants");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("addEntry"), "method:addEntry");
        check(methods.contains("create"), "method:create");
        check(methods.contains("createVersion"), "method:createVersion");
        check(methods.contains("deleteEntries"), "method:deleteEntries");
        check(methods.contains("findServiceEntry"), "method:findServiceEntry");
        check(methods.contains("getEntries"), "method:getEntries");
        check(methods.contains("getInstance"), "method:getInstance");
        check(methods.contains("getOneName"), "method:getOneName");
        check(methods.contains("isMissing"), "method:isMissing");
        check(methods.contains("isValid"), "method:isValid");
        check(methods.contains("normalize"), "method:normalize");
        check(methods.contains("readServiceKeys"), "method:readServiceKeys");
        check(methods.contains("save"), "method:save");
        check(methods.contains("tabName"), "method:tabName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
