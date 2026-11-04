/*
 * @test
 * @summary javax.swing.InputMap conformance: put/get/remove/clear, parent chain, allKeys, ComponentInputMap (headless-safe).
 * @run main InputMapTest
 */

import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.util.HashSet;
import java.util.Set;

public class InputMapTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown");
    }

    static void testBasics() {
        InputMap m = new InputMap();
        check(m.size() == 0, "empty");
        check(m.keys() == null, "keys null when never populated");
        check(m.allKeys() == null, "allKeys null when empty and no parent");
        check(m.getParent() == null, "no parent");
        m.put(KeyStroke.getKeyStroke('a'), "action");
        check("action".equals(m.get(KeyStroke.getKeyStroke('a'))), "put/get");
        KeyStroke ka = KeyStroke.getKeyStroke('a');
        KeyStroke kb = KeyStroke.getKeyStroke('b');
        KeyStroke kc = KeyStroke.getKeyStroke('c');
        m.put(ka, "v1");
        check(m.size() == 1, "size 1 after put");
        check("v1".equals(m.get(ka)), "get returns bound value");
        check(m.keys() != null && m.keys().length == 1 && m.keys()[0] == ka, "keys returns own keys only");
        m.put(kb, "v2");
        check(m.size() == 2, "size 2");
        // null value removes the binding
        m.put(kb, null);
        check(m.size() == 1, "put(ks,null) removes binding");
        check(m.get(kb) == null, "removed key unbound");
        // put same key twice overwrites
        m.put(ka, "v1b");
        check(m.size() == 1, "overwrite keeps size");
        check("v1b".equals(m.get(ka)), "overwritten value read back");
        // remove
        m.remove(ka);
        check(m.size() == 0, "remove shrinks size");
        check(m.keys() != null && m.keys().length == 0, "keys returns empty array after put+remove (not null)");
        // get with null key
        check(m.get(null) == null, "get(null) -> null");
    }

    static void testParentChain() {
        InputMap child = new InputMap();
        InputMap parent = new InputMap();
        InputMap grandparent = new InputMap();
        KeyStroke ka = KeyStroke.getKeyStroke('a');
        KeyStroke kb = KeyStroke.getKeyStroke('b');
        KeyStroke kc = KeyStroke.getKeyStroke('c');
        parent.put(ka, "pa");
        grandparent.put(kb, "gpb");
        child.put(kc, "cc");
        child.setParent(parent);
        parent.setParent(grandparent);
        check(child.getParent() == parent, "setParent/getParent roundtrip");
        check("pa".equals(child.get(ka)), "lookup traverses parent");
        check("gpb".equals(child.get(kb)), "lookup traverses grandparent");
        check("cc".equals(child.get(kc)), "own binding wins presence");
        check(child.keys() != null && child.keys().length == 1, "keys() own entries only, parent invisible");
        Set<KeyStroke> all = new HashSet<KeyStroke>();
        for (KeyStroke k : child.allKeys()) all.add(k);
        check(all.size() == 3, "allKeys spans own + parent chain: " + all.size());
        // child binding shadows parent
        child.put(ka, "shadow");
        check("shadow".equals(child.get(ka)), "child shadows parent binding");
        check("pa".equals(parent.get(ka)), "parent map unaffected");
        // clear only clears own
        child.clear();
        check(child.size() == 0, "clear empties own map");
        check("pa".equals(child.get(ka)), "clear keeps parent chain");
        // parent cycle guard: setParent(null) allowed
        child.setParent(null);
        check(child.getParent() == null, "setParent(null) clears parent");
        check(child.get(ka) == null, "after parent removal lookup fails");
        // note: API trusts callers not to build a parent cycle; a cycle recurses in get()
    }

    static void testComponentInputMap() {
        JPanel p = new JPanel();
        ComponentInputMap cim = new ComponentInputMap(p);
        KeyStroke kx = KeyStroke.getKeyStroke('x');
        cim.put(kx, "act");
        check("act".equals(cim.get(kx)), "ComponentInputMap put/get");
        check(cim.size() == 1, "size");
        check(cim.getComponent() == p, "getComponent is constructor component");
        // parent must be a ComponentInputMap for the same component
        checkThrows(IllegalArgumentException.class, () ->
                cim.setParent(new ComponentInputMap(new JPanel())));
        ComponentInputMap sameOwner = new ComponentInputMap(p);
        cim.setParent(sameOwner);
        check(cim.getParent() == sameOwner, "same-component ComponentInputMap parent accepted");
        // plain InputMap as parent also rejected
        checkThrows(IllegalArgumentException.class, () ->
                cim.setParent(new InputMap()));
        check(cim.get(KeyStroke.getKeyStroke('z')) == null, "ComponentInputMap inherits nothing from empty parent");
        cim.getParent().put(kx, "pv");
        check("act".equals(cim.get(kx)), "own ComponentInputMap binding shadows parent");
        cim.getParent().put(KeyStroke.getKeyStroke('q'), "q");
        check("q".equals(cim.get(KeyStroke.getKeyStroke('q'))), "lookup crosses ComponentInputMap parent");
    }

    public static void main(String[] args) {
        testBasics();
        testParentChain();
        testComponentInputMap();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
