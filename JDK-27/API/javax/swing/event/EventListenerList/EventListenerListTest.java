/*
 * @test
 * @summary javax.swing.event.EventListenerList conformance: white-box structure + black-box behavior.
 * @run main EventListenerListTest
 */

import java.lang.reflect.Modifier;
import java.util.EventListener;

public class EventListenerListTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.EventListenerList");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    static void testBlackBox() {
        javax.swing.event.EventListenerList list = new javax.swing.event.EventListenerList();
        check(list.getListenerCount() == 0, "initial total count 0");
        check(list.getListenerList().length == 0, "initial listener array empty");

        javax.swing.event.ChangeListener l1 = new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) { }
        };
        javax.swing.event.ChangeListener l2 = new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) { }
        };
        java.awt.event.ActionListener a1 = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { }
        };

        list.add(javax.swing.event.ChangeListener.class, l1);
        list.add(javax.swing.event.ChangeListener.class, l2);
        list.add(java.awt.event.ActionListener.class, a1);

        check(list.getListenerCount() == 3, "total count 3");
        check(list.getListenerCount(javax.swing.event.ChangeListener.class) == 2, "ChangeListener count 2");
        check(list.getListenerCount(java.awt.event.ActionListener.class) == 1, "ActionListener count 1");

        Object[] flat = list.getListenerList();
        check(flat.length == 6, "flat array length 6");
        check(flat[0] == javax.swing.event.ChangeListener.class, "flat pair 0 type");
        check(flat[1] == l1, "flat pair 0 listener");
        check(flat[4] == java.awt.event.ActionListener.class, "flat pair 2 type");
        check(flat[5] == a1, "flat pair 2 listener");

        javax.swing.event.ChangeListener[] changes = list.getListeners(javax.swing.event.ChangeListener.class);
        check(changes.length == 2, "getListeners(ChangeListener) length");
        check(changes[0] == l2 && changes[1] == l1, "getListeners returns reverse insertion order");

        list.remove(javax.swing.event.ChangeListener.class, l1);
        check(list.getListenerCount() == 2, "total count 2 after remove");
        check(list.getListenerCount(javax.swing.event.ChangeListener.class) == 1, "ChangeListener count 1 after remove");
        javax.swing.event.ChangeListener[] after = list.getListeners(javax.swing.event.ChangeListener.class);
        check(after.length == 1 && after[0] == l2, "remaining listener is l2");

        list.remove(javax.swing.event.ChangeListener.class, l2);
        list.remove(java.awt.event.ActionListener.class, a1);
        check(list.getListenerCount() == 0, "total count 0 after all removed");
        check(list.getListenerList().length == 0, "listener array empty after all removed");

        // add(null, listener) is a documented no-op.
        list.add(javax.swing.event.ChangeListener.class, null);
        check(list.getListenerCount() == 0, "null add is no-op");

        // add with a mismatched listener type throws IllegalArgumentException.
        @SuppressWarnings("unchecked")
        Class<EventListener> badType = (Class<EventListener>)(Class<?>) javax.swing.event.ChangeListener.class;
        boolean threw = false;
        try {
            list.add(badType, a1);
        } catch (IllegalArgumentException expected) {
            threw = true;
        }
        check(threw, "mismatched listener type throws IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
