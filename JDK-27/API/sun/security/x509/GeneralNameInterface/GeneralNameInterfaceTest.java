/*
 * @test
 * @summary sun.security.x509.GeneralNameInterface conformance: internal package reflection.
 * @modules java.base/sun.security.x509:+open
 * @run main GeneralNameInterfaceTest
 */
import java.lang.reflect.*;
import java.util.*;

public class GeneralNameInterfaceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.x509.GeneralNameInterface", false, GeneralNameInterfaceTest.class.getClassLoader());
        check(c != null, "loaded");
        check("sun.security.x509.GeneralNameInterface".equals(c.getName()), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass-null");
        check(interfaceNames(c).equals(new TreeSet<>(Arrays.asList("sun.security.util.DerEncoder"))), "interfaces");
        check(methodNames(c).equals(new TreeSet<>(Arrays.asList("constrains", "getType", "subtreeDepth"))), "methods");
        check(fieldNames(c).equals(new TreeSet<>(Arrays.asList("NAME_ANY", "NAME_DIFF_TYPE", "NAME_DIRECTORY", "NAME_DNS", "NAME_EDI", "NAME_IP", "NAME_MATCH", "NAME_NARROWS", "NAME_OID", "NAME_RFC822", "NAME_SAME_TYPE", "NAME_URI", "NAME_WIDENS", "NAME_X400"))), "fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static Set<String> interfaceNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mm = m.getModifiers();
            if (Modifier.isPublic(mm) || Modifier.isProtected(mm)) s.add(m.getName());
        }
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Field fld : c.getDeclaredFields()) {
            if (fld.isEnumConstant()) continue;
            int fm = fld.getModifiers();
            if (Modifier.isPublic(fm) || Modifier.isProtected(fm)) s.add(fld.getName());
        }
        return s;
    }
    static Set<String> enumConstantNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Field fld : c.getDeclaredFields()) {
            if (fld.isEnumConstant()) s.add(fld.getName());
        }
        return s;
    }
}
