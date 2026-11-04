/*
 * @test
 * @summary java.util.prefs.NodeChangeEvent conformance: getParent/getChild.
 * @run main NodeChangeEventTest
 */

import java.util.prefs.NodeChangeEvent;
import java.util.prefs.Preferences;

public class NodeChangeEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNodeChangeEvent() {
        Preferences parent = Preferences.userRoot();
        Preferences child = parent.node("jck_node_event");
        try {
            NodeChangeEvent e = new NodeChangeEvent(parent, child);
            check(e.getParent() == parent, "getParent");
            check(e.getChild() == child, "getChild");
            check(e instanceof java.util.EventObject, "is EventObject");
        } finally {
            try {
                child.removeNode();
            } catch (Exception ignored) {
            }
        }
    }

    public static void main(String[] args) {
        testNodeChangeEvent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
