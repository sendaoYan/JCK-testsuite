/*
 * @test
 * @summary jdk.jfr.internal.LogTag conformance: internal package reflection.
 * @modules jdk.jfr/jdk.jfr.internal:+open
 * @run main LogTagTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogTagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static String[] methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(m.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] enumConstants(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.internal.LogTag", false, LogTagTest.class.getClassLoader());
        check(c.getName().equals("jdk.jfr.internal.LogTag"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(Arrays.equals(interfaces(c), new String[]{}), "interfaces");
        check(Arrays.equals(methods(c), new String[]{"level","valueOf","values"}), "methods");
        check(Arrays.equals(fields(c), new String[]{"JFR","JFR_DCMD","JFR_EVENT","JFR_METADATA","JFR_METHODTRACE","JFR_PERIODIC","JFR_SETTING","JFR_START","JFR_SYSTEM","JFR_SYSTEM_BYTECODE","JFR_SYSTEM_EVENT","JFR_SYSTEM_METADATA","JFR_SYSTEM_PARSER","JFR_SYSTEM_PERIODIC","JFR_SYSTEM_SAMPLING","JFR_SYSTEM_SETTING","JFR_SYSTEM_STREAMING","JFR_SYSTEM_THROTTLE"}), "fields");
        check(Arrays.equals(enumConstants(c), new String[]{"JFR","JFR_DCMD","JFR_EVENT","JFR_METADATA","JFR_METHODTRACE","JFR_PERIODIC","JFR_SETTING","JFR_START","JFR_SYSTEM","JFR_SYSTEM_BYTECODE","JFR_SYSTEM_EVENT","JFR_SYSTEM_METADATA","JFR_SYSTEM_PARSER","JFR_SYSTEM_PERIODIC","JFR_SYSTEM_SAMPLING","JFR_SYSTEM_SETTING","JFR_SYSTEM_STREAMING","JFR_SYSTEM_THROTTLE"}), "enumConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
