/*
 * @test
 * @summary javax.swing.ActionMap conformance: put/get/remove/clear, parent chain, allKeys (headless-safe).
 * @run main ActionMapTest
 */

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;

public class ActionMapTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Action act() {
        return new AbstractAction() { public void actionPerformed(ActionEvent e) { } };
    }

    static void testBasics() {
        ActionMap m = new ActionMap();
        check(m.size() == 0, "empty");
        check(m.get("nope") == null, "get missing");
        check(m.keys() == null, "keys null");
        check(m.getParent() == null, "no parent");
        check(m.allKeys() == null, "allKeys null when empty");
        Action a1 = act();
        Action a2 = act();
        m.put("k1", a1);
        check(m.size() == 1, "size 1");
        check(m.get("k1") == a1, "get returns same instance");
        check(m.keys() != null && m.keys().length == 1 && "k1".equals(m.keys()[0]), "keys contents");
        m.put("k2", a2);
        check(m.size() == 2, "size 2");
        // null value removes
        m.put("k2", null);
        check(m.size() == 1, "put(k,null) removes");
        check(m.get("k2") == null, "removed key");
        // overwrite
        m.put("k1", a2);
        check(m.size() == 1, "overwrite keeps size");
        check(m.get("k1") == a2, "overwritten value");
        // remove existing then keys()==null again
        m.remove("k1");
        check(m.size() == 0, "remove");
        check(m.keys() == null, "keys null after removing all");
        m.remove("never");
        check(m.size() == 0, "remove absent no-op");
        check(m.get(null) == null, "get(null) -> null");
    }

    static void testParentChain() {
        ActionMap child = new ActionMap();
        ActionMap parent = new ActionMap();
        ActionMap grand = new ActionMap();
        Action pa = act(), ga = act(), ca = act();
        parent.put("p", pa);
        grand.put("g", ga);
        child.put("c", ca);
        child.setParent(parent);
        parent.setParent(grand);
        check(child.getParent() == parent, "parent roundtrip");
        check(child.get("p") == pa, "lookup parent");
        check(child.get("g") == ga, "lookup grandparent");
        check(child.get("c") == ca, "own entry");
        check(child.keys() != null && child.keys().length == 1, "keys own only");
        Set<Object> all = new HashSet<Object>();
        for (Object k : child.allKeys()) all.add(k);
        check(all.size() == 3, "allKeys spans chain: " + all.size());
        // shadowing
        Action shadow = act();
        child.put("p", shadow);
        check(child.get("p") == shadow, "child shadows parent");
        check(parent.get("p") == pa, "parent unchanged");
        // clear own only
        child.clear();
        check(child.size() == 0, "clear own");
        check(child.get("p") == pa, "clear keeps parent lookup");
        child.setParent(null);
        check(child.getParent() == null, "setParent(null)");
        check(child.get("p") == null, "no parent lookup");
        // note: a parent cycle recurses in get(); API trusts callers not to build one
    }

    static void testSharedThroughComponent() {
        // JPanel (and every JComponent) exposes an ActionMap; ensure it is usable like a standalone one
        javax.swing.JPanel p = new javax.swing.JPanel();
        ActionMap am = p.getActionMap();
        Action a = act();
        am.put("probe", a);
        check(am.get("probe") == a, "component ActionMap usable");
        check(am.size() >= 1, "component ActionMap non-empty");
        am.remove("probe");
        check(am.get("probe") == null, "component ActionMap remove");
    }

    public static void main(String[] args) {
        testBasics();
        testParentChain();
        testSharedThroughComponent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
