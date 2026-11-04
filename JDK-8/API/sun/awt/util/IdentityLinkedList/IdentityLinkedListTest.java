/*
 * @test
 * @summary sun.awt.util.IdentityLinkedList conformance: reflection (no init, native/display-heavy).
 * @run main IdentityLinkedListTest
 */

import java.util.*;
import java.lang.reflect.*;

public class IdentityLinkedListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.util.IdentityLinkedList", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.util.IdentityLinkedList".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.util.AbstractSequentialList".equals(c.getSuperclass().getName()), "superclass");
        check("java.util.Deque;java.util.List".equals(interfaces(c)), "interfaces");
        check("private#java.lang.Object#remove(sun.awt.util.IdentityLinkedList$Entry);private#sun.awt.util.IdentityLinkedList$Entry#addBefore(java.lang.Object,sun.awt.util.IdentityLinkedList$Entry);private#sun.awt.util.IdentityLinkedList$Entry#entry(int);public#[Ljava.lang.Object;#toArray();public#[Ljava.lang.Object;#toArray([Ljava.lang.Object;);public#boolean#add(java.lang.Object);public#boolean#addAll(int,java.util.Collection);public#boolean#addAll(java.util.Collection);public#boolean#contains(java.lang.Object);public#boolean#offer(java.lang.Object);public#boolean#offerFirst(java.lang.Object);public#boolean#offerLast(java.lang.Object);public#boolean#remove(java.lang.Object);public#boolean#removeFirstOccurrence(java.lang.Object);public#boolean#removeLastOccurrence(java.lang.Object);public#int#indexOf(java.lang.Object);public#int#lastIndexOf(java.lang.Object);public#int#size();public#java.lang.Object#element();public#java.lang.Object#get(int);public#java.lang.Object#getFirst();public#java.lang.Object#getLast();public#java.lang.Object#peek();public#java.lang.Object#peekFirst();public#java.lang.Object#peekLast();public#java.lang.Object#poll();public#java.lang.Object#pollFirst();public#java.lang.Object#pollLast();public#java.lang.Object#pop();public#java.lang.Object#remove();public#java.lang.Object#remove(int);public#java.lang.Object#removeFirst();public#java.lang.Object#removeLast();public#java.lang.Object#set(int,java.lang.Object);public#java.util.Iterator#descendingIterator();public#java.util.ListIterator#listIterator(int);public#void#add(int,java.lang.Object);public#void#addFirst(java.lang.Object);public#void#addLast(java.lang.Object);public#void#clear();public#void#push(java.lang.Object)".equals(methods(c)), "methods");
        check("private transient#int#size;private transient#sun.awt.util.IdentityLinkedList$Entry#header".equals(fields(c)), "fields");
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
