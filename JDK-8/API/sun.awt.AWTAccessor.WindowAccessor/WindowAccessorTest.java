/*
 * @test
 * @summary sun.awt.AWTAccessor.WindowAccessor conformance: reflection (no init, native/display-heavy).
 * @run main WindowAccessorTest
 */

import java.util.*;
import java.lang.reflect.*;

public class WindowAccessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.AWTAccessor$WindowAccessor", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.AWTAccessor$WindowAccessor".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.getSuperclass() == null, "superclass null");
        check("".equals(interfaces(c)), "interfaces");
        check("public abstract#[Ljava.awt.Window;#getOwnedWindows(java.awt.Window);public abstract#boolean#isAutoRequestFocus(java.awt.Window);public abstract#boolean#isTrayIconWindow(java.awt.Window);public abstract#float#getOpacity(java.awt.Window);public abstract#java.awt.Dimension#getSecurityWarningSize(java.awt.Window);public abstract#java.awt.Shape#getShape(java.awt.Window);public abstract#java.awt.geom.Point2D#calculateSecurityWarningPosition(java.awt.Window,double,double,double,double);public abstract#void#setLWRequestStatus(java.awt.Window,boolean);public abstract#void#setOpacity(java.awt.Window,float);public abstract#void#setOpaque(java.awt.Window,boolean);public abstract#void#setSecurityWarningPosition(java.awt.Window,java.awt.geom.Point2D,float,float);public abstract#void#setSecurityWarningSize(java.awt.Window,int,int);public abstract#void#setShape(java.awt.Window,java.awt.Shape);public abstract#void#setTrayIconWindow(java.awt.Window,boolean);public abstract#void#updateWindow(java.awt.Window)".equals(methods(c)), "methods");
        check("".equals(fields(c)), "fields");
        check(c.getDeclaredConstructors().length == 0, "ctors");
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
