/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager.NameMap conformance: enum constants and methods.
 * @run main NameMapTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class NameMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$NameMap");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 3, "3 constants");
        java.util.Set<String> cn = new java.util.HashSet<String>();
        for (Object o : ec) cn.add(((java.lang.Enum<?>) o).name());
        check(cn.contains("ENTITY_EXPANSION_LIMIT"), "constant ENTITY_EXPANSION_LIMIT");
        check(cn.contains("MAX_OCCUR_NODE_LIMIT"), "constant MAX_OCCUR_NODE_LIMIT");
        check(cn.contains("ELEMENT_ATTRIBUTE_LIMIT"), "constant ELEMENT_ATTRIBUTE_LIMIT");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "has method values");
        check(names.contains("valueOf"), "has method valueOf");

        // ---- declaration order and ordinals ----
        Object[] order = (Object[]) c.getMethod("values").invoke(null);
        check(((java.lang.Enum<?>) order[0]).name().equals("ENTITY_EXPANSION_LIMIT"), "ordinal 0 ENTITY_EXPANSION_LIMIT");
        check(((java.lang.Enum<?>) order[1]).name().equals("MAX_OCCUR_NODE_LIMIT"), "ordinal 1 MAX_OCCUR_NODE_LIMIT");
        check(((java.lang.Enum<?>) order[2]).name().equals("ELEMENT_ATTRIBUTE_LIMIT"), "ordinal 2 ELEMENT_ATTRIBUTE_LIMIT");
        for (int i = 0; i < order.length; i++) {
            check(((java.lang.Enum<?>) order[i]).ordinal() == i, "ordinal " + i + " matches declaration");
        }

        // ---- newName/oldName fields: current-to-legacy system property names ----
        java.lang.reflect.Field newF = c.getDeclaredField("newName");
        newF.setAccessible(true);
        java.lang.reflect.Field oldF = c.getDeclaredField("oldName");
        oldF.setAccessible(true);
        check("jdk.xml.entityExpansionLimit".equals(newF.get(order[0])), "ENTITY new name");
        check("entityExpansionLimit".equals(oldF.get(order[0])), "ENTITY legacy name");
        check("jdk.xml.maxOccurLimit".equals(newF.get(order[1])), "OCCUR new name");
        check("maxOccurLimit".equals(oldF.get(order[1])), "OCCUR legacy name");
        check("jdk.xml.elementAttributeLimit".equals(newF.get(order[2])), "ATTRIBUTE new name");
        check("elementAttributeLimit".equals(oldF.get(order[2])), "ATTRIBUTE legacy name");

        // ---- getOldName: bijection over own name only; everything else yields null ----
        java.lang.reflect.Method getOldName = c.getDeclaredMethod("getOldName", String.class);
        getOldName.setAccessible(true);
        check("entityExpansionLimit".equals(getOldName.invoke(order[0], "jdk.xml.entityExpansionLimit")), "ENTITY maps to legacy name");
        check("maxOccurLimit".equals(getOldName.invoke(order[1], "jdk.xml.maxOccurLimit")), "OCCUR maps to legacy name");
        check("elementAttributeLimit".equals(getOldName.invoke(order[2], "jdk.xml.elementAttributeLimit")), "ATTRIBUTE maps to legacy name");
        check(getOldName.invoke(order[0], "jdk.xml.maxOccurLimit") == null, "sibling new name -> null");
        check(getOldName.invoke(order[1], "entityExpansionLimit") == null, "sibling legacy name -> null");
        check(getOldName.invoke(order[2], "") == null, "empty string -> null");
        try {
            getOldName.invoke(order[0], (Object) null);
            check(false, "getOldName(null) must throw NPE");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            check(ite.getCause() instanceof NullPointerException, "getOldName(null) -> NullPointerException (no null guard)");
        }

        // ---- valueOf round-trip; unknown rejected ----
        java.lang.reflect.Method valueOf = c.getMethod("valueOf", String.class);
        for (Object o : order) {
            String n = ((java.lang.Enum<?>) o).name();
            check(valueOf.invoke(null, n) == o, "valueOf round-trip " + n);
            check(n.equals(((java.lang.Enum<?>) o).toString()), "toString " + n);
        }
        try {
            valueOf.invoke(null, "NO_SUCH_NAME_MAP");
            check(false, "valueOf unknown must throw");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            check(ite.getCause() instanceof IllegalArgumentException, "valueOf unknown -> IllegalArgumentException");
        }

        // ---- linkage: Limit.systemProperty names resolve through this map ----
        Class<?> limC = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit");
        java.lang.reflect.Method limSys = limC.getDeclaredMethod("systemProperty");
        limSys.setAccessible(true);
        for (Object o : order) {
            String limSp = (String) limSys.invoke(limC.getMethod("valueOf", String.class).invoke(null, ((java.lang.Enum<?>) o).name()));
            check(newF.get(o).equals(limSp), ((java.lang.Enum<?>) o).name() + " new name == Limit.systemProperty");
            check(getOldName.invoke(o, limSp).equals(oldF.get(o)), ((java.lang.Enum<?>) o).name() + " resolves Limit name to legacy name");
        }

        // ---- REAL LINKAGE: XMLSecurityManager picks up a limit set only under its legacy name ----
        Class<?> smC = Class.forName("jdk.xml.internal.XMLSecurityManager");
        java.lang.reflect.Method getLimitValueAsString = smC.getMethod("getLimitValueAsString", limC);
        Object eel = limC.getMethod("valueOf", String.class).invoke(null, "ENTITY_EXPANSION_LIMIT");
        String prevNew = System.clearProperty("jdk.xml.entityExpansionLimit");
        String prevOld = System.setProperty("entityExpansionLimit", "66");
        try {
            Object sm = smC.getConstructor(boolean.class).newInstance(false);
            check("66".equals(getLimitValueAsString.invoke(sm, eel)), "legacy name entityExpansionLimit=66 honored via NameMap fallback");
        } finally {
            restore("entityExpansionLimit", prevOld);
            restore("jdk.xml.entityExpansionLimit", prevNew);
        }
        Object smR = smC.getConstructor(boolean.class).newInstance(false);
        check("0".equals(getLimitValueAsString.invoke(smR, eel)), "back to default after legacy property cleared");

        System.out.println("PASS (" + passed + " assertions)");
    }

    static void restore(String name, String prev) {
        if (prev == null) System.clearProperty(name);
        else System.setProperty(name, prev);
    }
}
