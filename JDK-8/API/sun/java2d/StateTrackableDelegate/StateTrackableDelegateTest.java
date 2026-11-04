/*
 * @test
 * @summary sun.java2d.StateTrackableDelegate conformance: reflection (no init, native/display-heavy).
 * @run main StateTrackableDelegateTest
 */

import java.util.*;
import java.lang.reflect.*;

public class StateTrackableDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.StateTrackableDelegate", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.StateTrackableDelegate".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.StateTrackable".equals(interfaces(c)), "interfaces");
        check("protected synchronized#void#removeDynamicAgent();public final#void#markDirty();public static#sun.java2d.StateTrackableDelegate#createInstance(sun.java2d.StateTrackable$State);public synchronized#sun.java2d.StateTracker#getStateTracker();public synchronized#void#addDynamicAgent();public synchronized#void#setImmutable();public synchronized#void#setUntrackable();public#sun.java2d.StateTrackable$State#getState()".equals(methods(c)), "methods");
        check("#sun.java2d.StateTracker#theTracker;private#int#numDynamicAgents;private#sun.java2d.StateTrackable$State#theState;public static final#sun.java2d.StateTrackableDelegate#IMMUTABLE_DELEGATE;public static final#sun.java2d.StateTrackableDelegate#UNTRACKABLE_DELEGATE".equals(fields(c)), "fields");
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
