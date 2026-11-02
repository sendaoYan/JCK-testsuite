/*
 * @test
 * @summary sun.awt.screencast.ScreencastHelper conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.screencast:+open
 * @run main ScreencastHelperTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ScreencastHelperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.screencast.ScreencastHelper", false, ScreencastHelperTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.screencast.ScreencastHelper".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static native#boolean#loadPipewire(int,boolean);private static native#int#getRGBPixelsImpl(int,int,int,int,[I,[I,java.lang.String);private static synchronized native#int#remoteDesktopKeyImpl(boolean,int,java.lang.String);private static synchronized native#int#remoteDesktopMouseButtonImpl(boolean,int,java.lang.String);private static synchronized native#int#remoteDesktopMouseMoveImpl(int,int,java.lang.String);private static synchronized native#int#remoteDesktopMouseWheelImpl(int,java.lang.String);private static synchronized native#void#closeSession();private static#boolean#checkReturnValue(int,boolean);private static#java.util.List#getSystemScreensBounds();private static#void#performWithToken(java.util.function.Function);private static#void#timerCloseSessionRestart();public static synchronized#void#getRGBPixels(int,int,int,int,[I);public static synchronized#void#remoteDesktopKey(boolean,int);public static synchronized#void#remoteDesktopMouseButton(boolean,int);public static synchronized#void#remoteDesktopMouseMove(int,int);public static synchronized#void#remoteDesktopMouseWheel(int);public static#boolean#isAvailable()".equals(methods(c)), "methods");
        check("private static final#boolean#IS_NATIVE_LOADED;private static final#int#DELAY_BEFORE_SESSION_CLOSE;private static final#int#DENIED;private static final#int#ERROR;private static final#int#NO_STREAMS;private static final#int#OUT_OF_BOUNDS;private static final#int#XDG_METHOD_REMOTE_DESKTOP;private static final#int#XDG_METHOD_SCREENCAST;private static volatile#java.util.TimerTask#timerTask;static final#boolean#SCREENCAST_DEBUG".equals(fields(c)), "fields");
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
