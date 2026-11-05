/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager.State conformance: enum constants and methods.
 * @run main StateTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class StateTest {
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
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$State");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 5, "5 constants");
        java.util.Set<String> cn = new java.util.HashSet<String>();
        for (Object o : ec) cn.add(((java.lang.Enum<?>) o).name());
        check(cn.contains("DEFAULT"), "constant DEFAULT");
        check(cn.contains("FSP"), "constant FSP");
        check(cn.contains("JAXPDOTPROPERTIES"), "constant JAXPDOTPROPERTIES");
        check(cn.contains("SYSTEMPROPERTY"), "constant SYSTEMPROPERTY");
        check(cn.contains("APIPROPERTY"), "constant APIPROPERTY");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "has method values");
        check(names.contains("valueOf"), "has method valueOf");

        // ---- declaration order is the overriding order ----
        java.lang.reflect.Method values = c.getMethod("values");
        Object[] order = (Object[]) values.invoke(null);
        check(((java.lang.Enum<?>) order[0]).name().equals("DEFAULT"), "ordinal 0 DEFAULT");
        check(((java.lang.Enum<?>) order[1]).name().equals("FSP"), "ordinal 1 FSP");
        check(((java.lang.Enum<?>) order[2]).name().equals("JAXPDOTPROPERTIES"), "ordinal 2 JAXPDOTPROPERTIES");
        check(((java.lang.Enum<?>) order[3]).name().equals("SYSTEMPROPERTY"), "ordinal 3 SYSTEMPROPERTY");
        check(((java.lang.Enum<?>) order[4]).name().equals("APIPROPERTY"), "ordinal 4 APIPROPERTY");
        for (int i = 0; i < order.length; i++) {
            check(((java.lang.Enum<?>) order[i]).ordinal() == i, "ordinal " + i + " matches declaration");
        }

        // ---- compareTo encodes the precedence chain used by setLimit ----
        @SuppressWarnings("rawtypes") java.lang.Enum def = (java.lang.Enum) order[0];
        @SuppressWarnings("rawtypes") java.lang.Enum fsp = (java.lang.Enum) order[1];
        @SuppressWarnings("rawtypes") java.lang.Enum jaxp = (java.lang.Enum) order[2];
        @SuppressWarnings("rawtypes") java.lang.Enum sysp = (java.lang.Enum) order[3];
        @SuppressWarnings("rawtypes") java.lang.Enum api = (java.lang.Enum) order[4];
        check(def.compareTo(fsp) < 0, "DEFAULT < FSP");
        check(fsp.compareTo(jaxp) < 0, "FSP < JAXPDOTPROPERTIES");
        check(jaxp.compareTo(sysp) < 0, "JAXPDOTPROPERTIES < SYSTEMPROPERTY");
        check(sysp.compareTo(api) < 0, "SYSTEMPROPERTY < APIPROPERTY");
        check(api.compareTo(def) > 0, "APIPROPERTY > DEFAULT");
        check(sysp.compareTo(sysp) == 0, "same state compareTo == 0");

        // ---- literal(): exact strings surfaced in diagnostics ----
        java.lang.reflect.Method literal = c.getDeclaredMethod("literal");
        literal.setAccessible(true);
        check("default".equals(literal.invoke(order[0])), "literal DEFAULT");
        check("FEATURE_SECURE_PROCESSING".equals(literal.invoke(order[1])), "literal FSP");
        check("jaxp.properties".equals(literal.invoke(order[2])), "literal JAXPDOTPROPERTIES");
        check("system property".equals(literal.invoke(order[3])), "literal SYSTEMPROPERTY");
        check("property".equals(literal.invoke(order[4])), "literal APIPROPERTY");

        // ---- valueOf round-trip; unknown rejected ----
        java.lang.reflect.Method valueOf = c.getMethod("valueOf", String.class);
        for (Object o : order) {
            String n = ((java.lang.Enum<?>) o).name();
            check(valueOf.invoke(null, n) == o, "valueOf round-trip " + n);
            check(n.equals(((java.lang.Enum<?>) o).toString()), "toString " + n);
        }
        try {
            valueOf.invoke(null, "NO_SUCH_STATE");
            check(false, "valueOf unknown must throw");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            check(ite.getCause() instanceof IllegalArgumentException, "valueOf unknown -> IllegalArgumentException");
        }

        // ---- linkage: live XMLSecurityManager reports these states per limit ----
        Class<?> smC = Class.forName("jdk.xml.internal.XMLSecurityManager");
        Class<?> limC = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit");
        java.lang.reflect.Method getState = smC.getMethod("getState", limC);
        java.lang.reflect.Method getStateLiteral = smC.getMethod("getStateLiteral", limC);
        Object eel = limC.getMethod("valueOf", String.class).invoke(null, "ENTITY_EXPANSION_LIMIT");
        Object smF = smC.getConstructor(boolean.class).newInstance(false);
        check(((java.lang.Enum<?>) getState.invoke(smF, eel)).name().equals("DEFAULT"), "fsp=false -> state DEFAULT");
        check("default".equals(getStateLiteral.invoke(smF, eel)), "fsp=false -> literal default");
        Object smT = smC.getConstructor(boolean.class).newInstance(true);
        check(((java.lang.Enum<?>) getState.invoke(smT, eel)).name().equals("FSP"), "fsp=true -> state FSP");
        check("FEATURE_SECURE_PROCESSING".equals(getStateLiteral.invoke(smT, eel)), "fsp=true -> literal FSP");
        // explicit api write moves state to APIPROPERTY; a later FSP write is ignored
        Object apiSt = order[4];
        Object fspSt = order[1];
        smC.getMethod("setLimit", int.class, stype(), int.class).invoke(smF, 0, apiSt, 5);
        check(((java.lang.Enum<?>) getState.invoke(smF, eel)).name().equals("APIPROPERTY"), "state APIPROPERTY after explicit set");
        check("property".equals(getStateLiteral.invoke(smF, eel)), "literal property after explicit set");
        smC.getMethod("setLimit", int.class, stype(), int.class).invoke(smF, 0, fspSt, 999);
        check(((java.lang.Enum<?>) getState.invoke(smF, eel)).name().equals("APIPROPERTY"), "FSP write cannot demote APIPROPERTY state");
        // system property wins over FSP state at construction time
        String prev = System.setProperty("jdk.xml.entityExpansionLimit", "33");
        try {
            Object smSys = smC.getConstructor(boolean.class).newInstance(true);
            check(((java.lang.Enum<?>) getState.invoke(smSys, eel)).name().equals("SYSTEMPROPERTY"), "sysprop beats FSP state");
            check("system property".equals(getStateLiteral.invoke(smSys, eel)), "literal system property");
        } finally {
            if (prev == null) System.clearProperty("jdk.xml.entityExpansionLimit");
            else System.setProperty("jdk.xml.entityExpansionLimit", prev);
        }

        System.out.println("PASS (" + passed + " assertions)");
    }

    static Class<?> stype() throws Exception {
        return Class.forName("jdk.xml.internal.XMLSecurityManager$State");
    }
}
