/*
 * @test
 * @summary sun.awt.util.IdentityArrayList conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.util:+open
 * @run main IdentityArrayListTest
 */

import java.util.*;
import java.lang.reflect.*;

public class IdentityArrayListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.util.IdentityArrayList", false, IdentityArrayListTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.util.IdentityArrayList".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.util.AbstractList".equals(c.getSuperclass().getName()), "superclass");
        check("java.util.List;java.util.RandomAccess".equals(interfaces(c)), "interfaces");
        check("private#java.lang.String#outOfBoundsMsg(int);private#void#fastRemove(int);private#void#rangeCheck(int);private#void#rangeCheckForAdd(int);protected#void#removeRange(int,int);public#[Ljava.lang.Object;#toArray();public#[Ljava.lang.Object;#toArray([Ljava.lang.Object;);public#boolean#add(java.lang.Object);public#boolean#addAll(int,java.util.Collection);public#boolean#addAll(java.util.Collection);public#boolean#contains(java.lang.Object);public#boolean#isEmpty();public#boolean#remove(java.lang.Object);public#int#indexOf(java.lang.Object);public#int#lastIndexOf(java.lang.Object);public#int#size();public#java.lang.Object#get(int);public#java.lang.Object#remove(int);public#java.lang.Object#set(int,java.lang.Object);public#void#add(int,java.lang.Object);public#void#clear();public#void#ensureCapacity(int);public#void#trimToSize()".equals(methods(c)), "methods");
        check("private transient#[Ljava.lang.Object;#elementData;private#int#size".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 3, "ctors");
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
