/*
 * @test
 * @summary sun.awt.X11.XKeysym conformance: reflection (no init, native/display-heavy).
 * @run main XKeysymTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XKeysymTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XKeysym", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XKeysym".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#long#getKeypadKeysym(sun.awt.X11.XKeyEvent);public static#boolean#isKPEvent(sun.awt.X11.XKeyEvent);public static#char#convertKeysym(long,int);public static#long#getUppercaseAlphabetic(long);static#int#getJavaKeycodeOnly(sun.awt.X11.XKeyEvent);static#int#getLegacyJavaKeycodeOnly(sun.awt.X11.XKeyEvent);static#long#getKeysym(sun.awt.X11.XKeyEvent);static#long#javaKeycode2Keysym(int);static#long#xkeycode2keysym(sun.awt.X11.XKeyEvent,int);static#long#xkeycode2keysym_noxkb(sun.awt.X11.XKeyEvent,int);static#long#xkeycode2keysym_xkb(sun.awt.X11.XKeyEvent,int);static#long#xkeycode2primary_keysym(sun.awt.X11.XKeyEvent);static#sun.awt.X11.XKeysym$Keysym2JavaKeycode#getJavaKeycode(long);static#sun.awt.X11.XKeysym$Keysym2JavaKeycode#getJavaKeycode(sun.awt.X11.XKeyEvent)".equals(methods(c)), "methods");
        check("private static#sun.misc.Unsafe#unsafe;private static#sun.util.logging.PlatformLogger#keyEventLog;static#java.util.Hashtable#javaKeycode2KeysymHash;static#java.util.Hashtable#keysym2JavaKeycodeHash;static#java.util.Hashtable#keysym2UCSHash;static#java.util.Hashtable#uppercaseHash;static#long#keysym_lowercase;static#long#keysym_uppercase;static#sun.awt.X11.XKeysym$Keysym2JavaKeycode#kanaLock".equals(fields(c)), "fields");
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
            s.append(Modifier.toString(m.getModifiers())).append('#');
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
            l.add(Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        try {
            Object[] ec = c.getEnumConstants();
            if (ec == null) return "";
            List<String> l = new ArrayList<>();
            for (Object o : ec) l.add(((Enum<?>) o).name());
            Collections.sort(l);
            return String.join(";", l);
        } catch (Throwable t) {
            return "";
        }
    }
}
