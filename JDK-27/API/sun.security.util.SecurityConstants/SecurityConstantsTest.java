/*
 * @test
 * @summary sun.security.util.SecurityConstants conformance: internal package reflection.
 * @modules java.base/sun.security.util:+open
 * @run main SecurityConstantsTest
 */
import java.lang.reflect.*;
import java.util.*;

public class SecurityConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.util.SecurityConstants", false, SecurityConstantsTest.class.getClassLoader());
        check(c != null, "loaded");
        check("sun.security.util.SecurityConstants".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass() == null ? "" : c.getSuperclass().getName()), "superclass");
        check(interfaceNames(c).equals(new TreeSet<String>()), "interfaces");
        check(methodNames(c).equals(new TreeSet<String>()), "methods");
        check(fieldNames(c).equals(new TreeSet<>(Arrays.asList("ALL_PERMISSION", "FILE_DELETE_ACTION", "FILE_EXECUTE_ACTION", "FILE_READLINK_ACTION", "FILE_READ_ACTION", "FILE_WRITE_ACTION", "PROPERTY_READ_ACTION", "PROPERTY_RW_ACTION", "PROPERTY_WRITE_ACTION", "PROVIDER_VER", "SOCKET_ACCEPT_ACTION", "SOCKET_CONNECT_ACCEPT_ACTION", "SOCKET_CONNECT_ACTION", "SOCKET_LISTEN_ACTION", "SOCKET_RESOLVE_ACTION"))), "fields");
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
