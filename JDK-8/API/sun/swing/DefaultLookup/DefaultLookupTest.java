/*
 * @test
 * @summary sun.swing.DefaultLookup conformance: reflection (no init, native/display-heavy).
 * @run main DefaultLookupTest
 */

import java.util.*;
import java.lang.reflect.*;

public class DefaultLookupTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.swing.DefaultLookup", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.swing.DefaultLookup".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public static#boolean#getBoolean(javax.swing.JComponent,javax.swing.plaf.ComponentUI,java.lang.String);public static#boolean#getBoolean(javax.swing.JComponent,javax.swing.plaf.ComponentUI,java.lang.String,boolean);public static#int#getInt(javax.swing.JComponent,javax.swing.plaf.ComponentUI,java.lang.String);public static#int#getInt(javax.swing.JComponent,javax.swing.plaf.ComponentUI,java.lang.String,int);public static#java.awt.Color#getColor(javax.swing.JComponent,javax.swing.plaf.ComponentUI,java.lang.String);public static#java.awt.Color#getColor(javax.swing.JComponent,javax.swing.plaf.ComponentUI,java.lang.String,java.awt.Color);public static#java.awt.Insets#getInsets(javax.swing.JComponent,javax.swing.plaf.ComponentUI,java.lang.String);public static#java.awt.Insets#getInsets(javax.swing.JComponent,javax.swing.plaf.ComponentUI,java.lang.String,java.awt.Insets);public static#java.lang.Object#get(javax.swing.JComponent,javax.swing.plaf.ComponentUI,java.lang.String);public static#javax.swing.Icon#getIcon(javax.swing.JComponent,javax.swing.plaf.ComponentUI,java.lang.String);public static#javax.swing.Icon#getIcon(javax.swing.JComponent,javax.swing.plaf.ComponentUI,java.lang.String,javax.swing.Icon);public static#javax.swing.border.Border#getBorder(javax.swing.JComponent,javax.swing.plaf.ComponentUI,java.lang.String);public static#javax.swing.border.Border#getBorder(javax.swing.JComponent,javax.swing.plaf.ComponentUI,java.lang.String,javax.swing.border.Border);public static#void#setDefaultLookup(sun.swing.DefaultLookup);public#java.lang.Object#getDefault(javax.swing.JComponent,javax.swing.plaf.ComponentUI,java.lang.String)".equals(methods(c)), "methods");
        check("private static final#java.lang.Object#DEFAULT_LOOKUP_KEY;private static#boolean#isLookupSet;private static#java.lang.Thread#currentDefaultThread;private static#sun.swing.DefaultLookup#currentDefaultLookup".equals(fields(c)), "fields");
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
