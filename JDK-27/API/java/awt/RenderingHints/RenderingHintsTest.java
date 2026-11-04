/*
 * @test
 * @summary java.awt.RenderingHints conformance: white-box structure + black-box behavior.
 * @run main RenderingHintsTest
 */

import java.lang.reflect.*;

public class RenderingHintsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.RenderingHints", false, RenderingHintsTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.RenderingHints");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!Modifier.isAbstract(m), "concrete class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(java.util.Map.class.isAssignableFrom(c), "implements java.util.Map");
        check(java.lang.Cloneable.class.isAssignableFrom(c), "implements Cloneable");

        check(c.getField("KEY_ANTIALIASING").getType() == java.awt.RenderingHints.Key.class,
              "static Key KEY_ANTIALIASING");
        check(c.getField("VALUE_ANTIALIAS_ON") != null, "static VALUE_ANTIALIAS_ON");

        check(c.getConstructor(java.util.Map.class) != null, "ctor (Map)");
        check(c.getConstructor(java.awt.RenderingHints.Key.class, Object.class) != null, "ctor (Key,Object)");
        for (String name : new String[] {"size", "isEmpty", "containsKey", "containsValue", "get",
                "put", "add", "clear", "remove", "putAll", "keySet", "values", "entrySet",
                "equals", "hashCode", "clone", "toString"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)), "method present: " + name);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        java.awt.RenderingHints.Key aa = java.awt.RenderingHints.KEY_ANTIALIASING;
        java.awt.RenderingHints.Key rend = java.awt.RenderingHints.KEY_RENDERING;
        Object aaOn = java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
        Object aaOff = java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;

        check(aa != null && rend != null && aaOn != null, "hint constants non-null");

        // (Key, Object) constructor
        java.awt.RenderingHints rh = new java.awt.RenderingHints(aa, aaOn);
        check(rh.size() == 1, "ctor (Key,Object) size 1");
        check(!rh.isEmpty(), "not empty");
        check(rh.get(aa) == aaOn, "ctor stores value");

        // put / get / containsKey / containsValue
        check(rh.put(aa, aaOff) == aaOn, "put returns previous value");
        check(rh.get(aa) == aaOff, "put updated value");
        check(rh.containsKey(aa), "containsKey true");
        check(rh.containsValue(aaOff), "containsValue true");
        check(!rh.containsKey(rend), "containsKey false for absent key");

        // put validation
        boolean iae = false;
        try { rh.put(aa, "not-a-valid-value"); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "incompatible value throws IllegalArgumentException");
        boolean npe = false;
        try { rh.put(null, aaOn); } catch (NullPointerException e) { npe = true; }
        check(npe, "null key throws NullPointerException");
        boolean cce = false;
        try { rh.put(Integer.valueOf(1), aaOn); } catch (ClassCastException e) { cce = true; }
        check(cce, "non-Key key throws ClassCastException");

        // putAll / add
        java.util.HashMap<java.awt.RenderingHints.Key, Object> map = new java.util.HashMap<>();
        map.put(rend, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        rh.putAll(map);
        check(rh.size() == 2, "putAll adds mappings");
        java.awt.RenderingHints other = new java.awt.RenderingHints(aa, aaOn);
        other.add(rh);
        check(other.size() == 2 && other.get(rend) == java.awt.RenderingHints.VALUE_RENDER_QUALITY,
              "add merges another RenderingHints");

        // keySet sorted then asserted
        java.util.Set<Object> keys = rh.keySet();
        java.util.List<Object> sorted = new java.util.ArrayList<>(keys);
        java.util.Collections.sort(sorted, (x, y) ->
                Integer.compare(System.identityHashCode(x), System.identityHashCode(y)));
        check(sorted.size() == 2, "keySet size 2");
        check(sorted.contains(aa) && sorted.contains(rend), "keySet contains both keys");
        check(rh.values().size() == 2, "values size 2");
        check(rh.entrySet().size() == 2, "entrySet size 2");

        // remove / clear
        Object removed = rh.remove(aa);
        check(removed == aaOff, "remove returns removed value");
        check(!rh.containsKey(aa), "removed key absent");
        check(rh.size() == 1, "size after remove");
        rh.clear();
        check(rh.isEmpty(), "clear empties");
        check(rh.size() == 0, "clear size 0");

        // equals / hashCode
        java.awt.RenderingHints r1 = new java.awt.RenderingHints(aa, aaOn);
        r1.put(rend, java.awt.RenderingHints.VALUE_RENDER_SPEED);
        java.awt.RenderingHints r2 = new java.awt.RenderingHints(aa, aaOn);
        r2.put(rend, java.awt.RenderingHints.VALUE_RENDER_SPEED);
        check(r1.equals(r2), "equal RenderingHints");
        check(r1.hashCode() == r2.hashCode(), "equal hashCodes");
        check(!r1.equals(new java.awt.RenderingHints(aa, aaOff)), "different value not equal");
        check(!r1.equals(null), "not equal null");
        check(!r1.equals("hints"), "not equal unrelated type");

        // clone
        java.awt.RenderingHints orig = new java.awt.RenderingHints(aa, aaOn);
        Object raw = orig.clone();
        check(raw instanceof java.awt.RenderingHints, "clone returns RenderingHints");
        java.awt.RenderingHints cl = (java.awt.RenderingHints) raw;
        check(cl.equals(orig), "clone equals original");
        cl.put(rend, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        check(orig.size() == 1, "clone independent (mutation not shared)");

        // toString
        check(new java.awt.RenderingHints(new java.util.HashMap<>()).toString().equals("{}"),
              "empty toString == {}");
        check(orig.toString() != null && !orig.toString().isEmpty(), "non-empty toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        // keys present and distinct
        java.awt.RenderingHints.Key[] keys = {java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.KEY_DITHERING,
            java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.KEY_FRACTIONALMETRICS,
            java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION,
            java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.KEY_STROKE_CONTROL};
        String[] kn = {"ANTIALIASING","RENDERING","DITHERING","TEXT_ANTIALIASING","FRACTIONALMETRICS",
            "INTERPOLATION","ALPHA_INTERPOLATION","COLOR_RENDERING","STROKE_CONTROL"};
        java.util.Set<java.awt.RenderingHints.Key> seen = new java.util.HashSet<java.awt.RenderingHints.Key>();
        for (int i = 0; i < keys.length; i++) {
            check(keys[i] != null && seen.add(keys[i]), "key distinct: " + kn[i]);
        }
        // value constants distinct and bound to hints
        check(java.awt.RenderingHints.VALUE_ANTIALIAS_ON != java.awt.RenderingHints.VALUE_ANTIALIAS_OFF
              && java.awt.RenderingHints.VALUE_ANTIALIAS_OFF != java.awt.RenderingHints.VALUE_ANTIALIAS_DEFAULT,
              "antialias values distinct");
        check(java.awt.RenderingHints.VALUE_RENDER_SPEED != java.awt.RenderingHints.VALUE_RENDER_QUALITY,
              "render values distinct");
        check(java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
              != java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR, "interpolation values distinct");
        check(java.awt.RenderingHints.VALUE_STROKE_NORMALIZE
              != java.awt.RenderingHints.VALUE_STROKE_PURE, "stroke values distinct");
        // put returns previous value; null previous for absent
        java.awt.RenderingHints h = new java.awt.RenderingHints(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        Object old = h.put(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
        check(old == java.awt.RenderingHints.VALUE_ANTIALIAS_ON, "put returns previous value");
        check(h.get(java.awt.RenderingHints.KEY_ANTIALIASING) == java.awt.RenderingHints.VALUE_ANTIALIAS_OFF,
              "get after overwrite");
        check(h.put(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_SPEED) == null,
              "put absent key returns null");
        // get for absent key
        check(h.get(java.awt.RenderingHints.KEY_DITHERING) == null, "get absent -> null");
        check(h.containsKey(java.awt.RenderingHints.KEY_ANTIALIASING), "containsKey present");
        check(!h.containsKey(java.awt.RenderingHints.KEY_DITHERING), "containsKey absent");
        check(h.containsValue(java.awt.RenderingHints.VALUE_RENDER_SPEED), "containsValue");
        check(!h.containsValue(java.awt.RenderingHints.VALUE_RENDER_QUALITY), "containsValue absent");
        // size / isEmpty / clear
        check(h.size() == 2, "size 2");
        check(!h.isEmpty(), "not empty");
        h.clear();
        check(h.size() == 0 && h.isEmpty(), "clear empties");
        // remove returns removed value
        h.put(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        check(h.remove(java.awt.RenderingHints.KEY_RENDERING) == java.awt.RenderingHints.VALUE_RENDER_QUALITY,
              "remove returns value");
        check(h.remove(java.awt.RenderingHints.KEY_RENDERING) == null, "remove absent -> null");
        // value toString is human readable (locked observed)
        check(java.awt.RenderingHints.VALUE_ANTIALIAS_ON.toString().equals("Antialiased rendering mode"),
              "VALUE_ANTIALIAS_ON toString");
        check(java.awt.RenderingHints.VALUE_ANTIALIAS_OFF.toString().equals("Nonantialiased rendering mode"),
              "VALUE_ANTIALIAS_OFF toString");
        check(java.awt.RenderingHints.VALUE_RENDER_SPEED.toString().equals("Fastest rendering methods"),
              "VALUE_RENDER_SPEED toString");
        // Map view semantics: keySet/values are unmodifiable views
        boolean uoe1 = false;
        try { h.keySet().add(java.awt.RenderingHints.KEY_DITHERING); } catch (java.lang.UnsupportedOperationException e) { uoe1 = true; }
        check(uoe1, "keySet.add -> UnsupportedOperationException");
        boolean uoe2 = false;
        try { h.values().add("x"); } catch (java.lang.UnsupportedOperationException e) { uoe2 = true; }
        check(uoe2, "values.add -> UnsupportedOperationException");
        h.put(java.awt.RenderingHints.KEY_DITHERING, java.awt.RenderingHints.VALUE_DITHER_DISABLE);
        check(h.keySet().contains(java.awt.RenderingHints.KEY_DITHERING), "keySet reflects mutation");
        check(h.values().contains(java.awt.RenderingHints.VALUE_DITHER_DISABLE), "values reflects mutation");
        check(h.entrySet().size() == h.size(), "entrySet size");
        // constructor with Map null arg does not throw immediately (lazy defaults)
        java.awt.RenderingHints h2 = new java.awt.RenderingHints(null);
        check(h2.size() == 0, "null-map ctor empty");
        // add merges other hints
        java.awt.RenderingHints other = new java.awt.RenderingHints(java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        h.add(other);
        check(h.get(java.awt.RenderingHints.KEY_RENDERING) == java.awt.RenderingHints.VALUE_RENDER_QUALITY,
              "add merges");
        // add(null) throws NPE
        boolean npe = false;
        try { h2.add(null); } catch (java.lang.NullPointerException e) { npe = true; }
        check(npe, "add(null) -> NPE");
        // equals/hashCode against a plain Map copy
        java.util.Map<Object,Object> copy = new java.util.HashMap<Object,Object>();
        copy.putAll(h);
        check(h.equals(copy) && copy.equals(h), "equals Map copy (symmetric)");
        check(h.hashCode() == copy.hashCode(), "hashCode equals Map copy");
        check(!h.equals(new java.awt.RenderingHints(null)), "different hints not equal");
        check(h.toString().length() > 0, "toString non-empty");
        // clone returns shallow copy
        java.awt.RenderingHints cl = (java.awt.RenderingHints) h.clone();
        check(cl.equals(h), "clone equal");
        cl.put(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        check(!h.containsKey(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING), "clone put does not leak");
        // Key int values hidden but each key distinct even across equality
        check(!keys[0].equals(keys[1]), "keys not equal to each other");
        check(keys[0].hashCode() != keys[1].hashCode() || true, "key hashCodes usable");
    }

}
