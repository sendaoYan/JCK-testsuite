/*
 * @test
 * @summary sun.font.ScriptRun conformance: reflection (no init, native/display-heavy).
 * @run main ScriptRunTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ScriptRunTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.ScriptRun", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.ScriptRun".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private final#int#nextCodePoint();private final#void#pushback(int);private static final#byte#highBit(int);private static#boolean#sameScript(int,int);private static#int#getPairIndex(int);public final#boolean#next();public final#int#getScriptCode();public final#int#getScriptLimit();public final#int#getScriptStart();public#void#init([C,int,int)".equals(methods(c)), "methods");
        check("private static final#int#pairedCharExtra;private static final#int#pairedCharPower;private static#[I#pairedChars;private#[C#text;private#[I#stack;private#int#parenSP;private#int#scriptCode;private#int#scriptLimit;private#int#scriptStart;private#int#textLimit;private#int#textStart;static final#int#DONE;static final#int#LEAD_LIMIT;static final#int#LEAD_START;static final#int#LEAD_SURROGATE_SHIFT;static final#int#SURROGATE_OFFSET;static final#int#SURROGATE_START;static final#int#TAIL_LIMIT;static final#int#TAIL_START".equals(fields(c)), "fields");
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
