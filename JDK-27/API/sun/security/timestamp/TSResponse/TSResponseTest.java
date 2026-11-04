/*
 * @test
 * @summary sun.security.timestamp.TSResponse conformance: internal package reflection.
 * @modules java.base/sun.security.timestamp:+open
 * @run main TSResponseTest
 */
import java.lang.reflect.*;
import java.util.*;

public class TSResponseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.timestamp.TSResponse", false, TSResponseTest.class.getClassLoader());
        check(c != null, "loaded");
        check("sun.security.timestamp.TSResponse".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass() == null ? "" : c.getSuperclass().getName()), "superclass");
        check(interfaceNames(c).equals(new TreeSet<String>()), "interfaces");
        check(methodNames(c).equals(new TreeSet<>(Arrays.asList("getEncodedToken", "getFailureCodeAsText", "getFailureInfo", "getStatusCode", "getStatusCodeAsText", "getStatusMessages", "getTimestampToken", "getToken"))), "methods");
        check(fieldNames(c).equals(new TreeSet<>(Arrays.asList("ADD_INFO_NOT_AVAILABLE", "BAD_ALG", "BAD_DATA_FORMAT", "BAD_REQUEST", "GRANTED", "GRANTED_WITH_MODS", "REJECTION", "REVOCATION_NOTIFICATION", "REVOCATION_WARNING", "SYSTEM_FAILURE", "TIME_NOT_AVAILABLE", "UNACCEPTED_EXTENSION", "UNACCEPTED_POLICY", "WAITING"))), "fields");
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
