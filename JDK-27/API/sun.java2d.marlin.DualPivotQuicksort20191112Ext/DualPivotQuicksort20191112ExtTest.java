/*
 * @test
 * @summary sun.java2d.marlin.DualPivotQuicksort20191112Ext conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.marlin:+open
 * @run main DualPivotQuicksort20191112ExtTest
 */

import java.util.*;
import java.lang.reflect.*;

public class DualPivotQuicksort20191112ExtTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.marlin.DualPivotQuicksort20191112Ext", false, DualPivotQuicksort20191112ExtTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.marlin.DualPivotQuicksort20191112Ext".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#[I#mergeRuns([I,[I,[I,[I,int,int,[I,int,int);private static#boolean#tryMergeRuns(sun.java2d.marlin.DPQSSorterContext,[I,[I,int,int);private static#void#heapSort([I,[I,int,int);private static#void#mergeParts([I,[I,int,[I,[I,int,int,[I,[I,int,int);private static#void#mixedInsertionSort([I,[I,int,int,int);private static#void#pushDown([I,[I,int,int,int,int,int);private static#void#sort(sun.java2d.marlin.DPQSSorterContext,[I,[I,int,int,int);static#void#insertionSort([I,[I,int,int);static#void#sort(sun.java2d.marlin.DPQSSorterContext,[I,[I,[I,[I,int,int)".equals(methods(c)), "methods");
        check("private static final#boolean#FAST_ISORT;private static final#int#DELTA;private static final#int#MAX_INSERTION_SORT_SIZE;private static final#int#MAX_MIXED_INSERTION_SORT_SIZE;private static final#int#MAX_RECURSION_DEPTH;private static final#int#MIN_FIRST_RUNS_FACTOR;private static final#int#MIN_FIRST_RUN_SIZE;private static final#int#MIN_TRY_MERGE_SIZE;static final#int#MAX_RUN_CAPACITY".equals(fields(c)), "fields");
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
