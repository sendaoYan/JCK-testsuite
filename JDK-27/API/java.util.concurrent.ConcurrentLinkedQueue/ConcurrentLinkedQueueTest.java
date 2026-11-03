/*
 * @test
 * @summary java.util.concurrent.ConcurrentLinkedQueue conformance: white-box + black-box
 *          (offer/poll/peek return semantics, size/contains/remove, null rejection,
 *          toArray/addAll/iterator weakly-consistent).
 * @run main ConcurrentLinkedQueueTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConcurrentLinkedQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                                         + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.ConcurrentLinkedQueue");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractQueue.class, "extends AbstractQueue");
        check(Queue.class.isAssignableFrom(c), "implements Queue");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null && c.getConstructor(Collection.class) != null,
              "constructors ()/(Collection)");
        check(hasMethod(c, "offer", Object.class) && hasMethod(c, "add", Object.class),
              "offer/add");
        check(hasMethod(c, "poll") && hasMethod(c, "peek"), "poll/peek");
        check(hasMethod(c, "size") && hasMethod(c, "isEmpty"), "size/isEmpty");
        check(hasMethod(c, "contains", Object.class) && hasMethod(c, "remove", Object.class),
              "contains/remove");
        check(hasMethod(c, "iterator") && hasMethod(c, "toArray"), "iterator/toArray");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testQueueSemantics() {
        ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<>();
        check(q.isEmpty() && q.size() == 0, "empty initially");
        check(q.offer("a") && q.offer("b") && q.offer("c"), "offer");
        check(q.add("d"), "add");
        check(q.size() == 4, "size");
        check("a".equals(q.peek()), "peek head");
        check("a".equals(q.poll()), "poll head");
        check("b".equals(q.poll()), "poll next");
        check(q.peek().equals("c"), "peek after poll");
        check(q.contains("c") && !q.contains("a"), "contains");
        check(q.remove("c"), "remove returns true");
        check(!q.remove("zzz"), "remove missing false");
        check(q.size() == 1 && "d".equals(q.peek()), "remaining element");
        q.clear();
        check(q.isEmpty() && q.peek() == null && q.poll() == null, "clear empties");
    }

    static void testNullRejection() {
        ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<>();
        expectThrows(() -> q.offer(null), NullPointerException.class, "offer(null)");
        expectThrows(() -> q.add(null), NullPointerException.class, "add(null)");
        check(!q.contains(null), "contains(null) returns false");
        check(!q.remove(null), "remove(null) returns false");
    }

    static void testToArrayAndAddAll() {
        ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<>();
        q.add(1);
        q.add(2);
        Object[] arr = q.toArray();
        check(arr.length == 2 && arr[0].equals(1) && arr[1].equals(2), "toArray order");
        Integer[] typed = q.toArray(new Integer[0]);
        check(typed.length == 2 && typed[0] == 1 && typed[1] == 2, "toArray(T[])");
        ConcurrentLinkedQueue<Integer> q2 = new ConcurrentLinkedQueue<>(Arrays.asList(3, 4, 5));
        q.addAll(q2);
        check(q.size() == 5, "addAll size");
    }

    static void testWeaklyConsistentIterator() {
        ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<>();
        q.add(1);
        q.add(2);
        q.add(3);
        Iterator<Integer> it = q.iterator();
        int seen = 0;
        boolean modified = false;
        while (it.hasNext()) {
            seen++;
            it.next();
            if (!modified) {
                modified = true;
                q.add(99); // concurrent modification (bounded) does not throw
            }
        }
        check(seen >= 3, "iterator completes without ConcurrentModificationException");
        check(q.contains(99) && q.size() == 4, "modification applied");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testQueueSemantics();
        testNullRejection();
        testToArrayAndAddAll();
        testWeaklyConsistentIterator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
