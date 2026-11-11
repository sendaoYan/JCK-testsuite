/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11.WindowPropertyGetter conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main WindowPropertyGetterTest
 */

import java.util.*;
import java.lang.reflect.*;

public class WindowPropertyGetterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.WindowPropertyGetter", false, WindowPropertyGetterTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.WindowPropertyGetter".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#boolean#isCached();#int#getDataLength();#void#cacheProperty();#void#readFromCache();public#boolean#isDisposed();public#boolean#isExecuted();public#int#execute();public#int#execute(sun.awt.X11.XErrorHandler);public#int#getActualFormat();public#int#getNumberOfItems();public#long#getActualType();public#long#getBytesAfter();public#long#getData();public#void#dispose();static#boolean#isCacheableProperty(sun.awt.X11.XAtom);static#boolean#isCachingSupported()".equals(methods(c)), "methods");
        check("#sun.awt.X11.UnsafeXDisposerRecord#disposer;private final#boolean#auto_delete;private final#long#actual_format;private final#long#actual_type;private final#long#bytes_after;private final#long#data;private final#long#length;private final#long#nitems_ptr;private final#long#offset;private final#long#type;private final#long#window;private final#sun.awt.X11.XAtom#property;private static#jdk.internal.misc.Unsafe#unsafe;private#boolean#executed;static#java.util.Set#cacheableProperties".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 2, "ctors");
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
