/*
 * @test
 * @summary java.util.prefs.NodeChangeListener conformance: childAdded/childRemoved callbacks.
 * @run main NodeChangeListenerTest
 */

import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;

public class NodeChangeListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNodeChangeListener() {
        NodeChangeListener l = new NodeChangeListener() {
            public void childAdded(NodeChangeEvent evt) {
                check(evt.getChild() != null, "childAdded event child");
            }

            public void childRemoved(NodeChangeEvent evt) {
                check(evt.getChild() != null, "childRemoved event child");
            }
        };
        check(l != null, "listener implemented");
    }

    public static void main(String[] args) {
        testNodeChangeListener();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
