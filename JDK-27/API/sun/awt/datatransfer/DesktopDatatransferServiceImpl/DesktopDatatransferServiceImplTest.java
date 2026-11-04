/*
 * @test
 * @summary sun.awt.datatransfer.DesktopDatatransferServiceImpl conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.datatransfer:+open
 * @run main DesktopDatatransferServiceImplTest
 */

import java.util.*;
import java.lang.reflect.*;

public class DesktopDatatransferServiceImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.datatransfer.DesktopDatatransferServiceImpl", false, DesktopDatatransferServiceImplTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.datatransfer.DesktopDatatransferServiceImpl".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.datatransfer.DesktopDatatransferService".equals(interfaces(c)), "interfaces");
        check("public synchronized#java.awt.datatransfer.FlavorMap#getFlavorMap(java.util.function.Supplier);public#boolean#isDesktopPresent();public#java.lang.String#getDefaultUnicodeEncoding();public#java.util.LinkedHashSet#getPlatformMappingsForFlavor(java.awt.datatransfer.DataFlavor);public#java.util.LinkedHashSet#getPlatformMappingsForNative(java.lang.String);public#void#invokeOnEventThread(java.lang.Runnable);public#void#registerTextFlavorProperties(java.lang.String,java.lang.String,java.lang.String,java.lang.String)".equals(methods(c)), "methods");
        check("private#java.awt.datatransfer.FlavorMap#fm".equals(fields(c)), "fields");
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
