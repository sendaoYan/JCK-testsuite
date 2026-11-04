/*
 * @test
 * @summary sun.awt.DebugSettings conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main DebugSettingsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class DebugSettingsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.DebugSettings", false, DebugSettingsTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.DebugSettings".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private synchronized native#void#setCTracingOn(boolean);private synchronized native#void#setCTracingOn(boolean,java.lang.String);private synchronized native#void#setCTracingOn(boolean,java.lang.String,int);private synchronized#java.util.List#getPropertyNames();private synchronized#void#loadProperties();private#void#loadDefaultProperties();private#void#loadFileProperties();private#void#loadNativeSettings();private#void#loadSystemProperties();private#void#println(java.lang.Object);public static#sun.awt.DebugSettings#getInstance();public synchronized#boolean#getBoolean(java.lang.String,boolean);public synchronized#int#getInt(java.lang.String,int);public synchronized#java.lang.String#getString(java.lang.String,java.lang.String);public#java.lang.String#toString();static synchronized#void#init()".equals(methods(c)), "methods");
        check("private final#java.util.Properties#props;private static final#[Ljava.lang.String;#DEFAULT_PROPS;private static final#int#PROP_CTRACE_LEN;private static final#java.lang.String#PROP_CTRACE;private static final#sun.awt.DebugSettings#instance;private static final#sun.util.logging.PlatformLogger#log;static final#java.lang.String#PREFIX;static final#java.lang.String#PROP_FILE".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 1, "ctors");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        Collections.sort(l);
        return String.join(";", l);
    }

    static String methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            StringBuilder s = new StringBuilder();
            s.append(java.lang.reflect.Modifier.toString(m.getModifiers())).append('#');
            s.append(m.getReturnType().getName()).append('#');
            s.append(m.getName()).append('(');
            Class<?>[] ps = m.getParameterTypes();
            for (int i = 0; i < ps.length; i++) { if (i > 0) s.append(','); s.append(ps[i].getName()); }
            s.append(')');
            l.add(s.toString());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            l.add(java.lang.reflect.Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        if (!c.isEnum()) return "";
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }
}
