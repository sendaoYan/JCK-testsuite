/*
 * @test
 * @summary sun.awt.AppContext conformance: reflection (no init, native/display-heavy).
 * @run main AppContextTest
 */

import java.util.*;
import java.lang.reflect.*;

public class AppContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.AppContext", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.AppContext".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static final#sun.awt.AppContext#getExecutionAppContext();private static final#void#initMainAppContext();public static final#boolean#isMainContext(sun.awt.AppContext);public static final#sun.awt.AppContext#getAppContext();public static#java.lang.Object#getSoftReferenceValue(java.lang.Object,java.util.function.Supplier);public static#java.util.Set#getAppContexts();public synchronized#[Ljava.beans.PropertyChangeListener;#getPropertyChangeListeners();public synchronized#[Ljava.beans.PropertyChangeListener;#getPropertyChangeListeners(java.lang.String);public synchronized#void#addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener);public synchronized#void#removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener);public#boolean#isDisposed();public#java.lang.ClassLoader#getContextClassLoader();public#java.lang.Object#get(java.lang.Object);public#java.lang.Object#put(java.lang.Object,java.lang.Object);public#java.lang.Object#remove(java.lang.Object);public#java.lang.String#toString();public#java.lang.ThreadGroup#getThreadGroup();public#void#dispose();static#void#stopEventDispatchThreads()".equals(methods(c)), "methods");
        check("private final#java.lang.ClassLoader#contextClassLoader;private final#java.lang.ThreadGroup#threadGroup;private final#java.util.Map#table;private static final#java.lang.Object#getAppContextLock;private static final#java.lang.ThreadLocal#threadAppContext;private static final#java.util.Map#threadGroup2appContext;private static final#java.util.concurrent.atomic.AtomicInteger#numAppContexts;private static final#sun.util.logging.PlatformLogger#log;private static volatile#sun.awt.AppContext#mainAppContext;private volatile#sun.awt.AppContext$State#state;private#java.beans.PropertyChangeSupport#changeSupport;private#long#DISPOSAL_TIMEOUT;private#long#THREAD_INTERRUPT_TIMEOUT;private#sun.awt.MostRecentKeyValue#mostRecentKeyValue;private#sun.awt.MostRecentKeyValue#shadowMostRecentKeyValue;public static final#java.lang.Object#EVENT_QUEUE_COND_KEY;public static final#java.lang.Object#EVENT_QUEUE_KEY;public static final#java.lang.Object#EVENT_QUEUE_LOCK_KEY;public static final#java.lang.String#DISPOSED_PROPERTY_NAME;public static final#java.lang.String#GUI_DISPOSED".equals(fields(c)), "fields");
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
