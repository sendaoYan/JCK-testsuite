/*
 * @test
 * @summary jdk.xml.internal.JdkXmlFeatures.State conformance: enum constants and methods.
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
        Class<?> c = Class.forName("jdk.xml.internal.JdkXmlFeatures$State");
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

        // ---- declaration order is the overriding order (DEFAULT lowest .. APIPROPERTY highest) ----
        java.lang.reflect.Method values = c.getMethod("values");
        Object[] order = (Object[]) values.invoke(null);
        check(((java.lang.Enum<?>) order[0]).name().equals("DEFAULT"), "ordinal 0 DEFAULT");
        check(((java.lang.Enum<?>) order[1]).name().equals("FSP"), "ordinal 1 FSP");
        check(((java.lang.Enum<?>) order[2]).name().equals("JAXPDOTPROPERTIES"), "ordinal 2 JAXPDOTPROPERTIES");
        check(((java.lang.Enum<?>) order[3]).name().equals("SYSTEMPROPERTY"), "ordinal 3 SYSTEMPROPERTY");
        check(((java.lang.Enum<?>) order[4]).name().equals("APIPROPERTY"), "ordinal 4 APIPROPERTY");
        for (int i = 0; i < order.length; i++) {
            check(((java.lang.Enum<?>) order[i]).ordinal() == i, "ordinal of " + ((java.lang.Enum<?>) order[i]).name() + " == " + i);
        }

        // ---- compareTo encodes precedence chain ----
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
        check(def.compareTo(def) == 0, "same state compareTo == 0");

        // ---- literal(): package-private, exact strings used in error reports ----
        java.lang.reflect.Method literal = c.getDeclaredMethod("literal");
        literal.setAccessible(true);
        check("default".equals(literal.invoke(order[0])), "literal DEFAULT");
        check("FEATURE_SECURE_PROCESSING".equals(literal.invoke(order[1])), "literal FSP");
        check("jaxp.properties".equals(literal.invoke(order[2])), "literal JAXPDOTPROPERTIES");
        check("system property".equals(literal.invoke(order[3])), "literal SYSTEMPROPERTY");
        check("property".equals(literal.invoke(order[4])), "literal APIPROPERTY");

        // ---- valueOf round-trip for every constant ----
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

        // ---- linkage: JdkXmlFeatures(fsp=true) enforces the FSP state semantics ----
        Class<?> jfC = Class.forName("jdk.xml.internal.JdkXmlFeatures");
        Class<?> xfC = Class.forName("jdk.xml.internal.JdkXmlFeatures$XmlFeature");
        Object extF = xfC.getMethod("valueOf", String.class).invoke(null, "ENABLE_EXTENSION_FUNCTION");
        Object jfT = jfC.getConstructor(boolean.class).newInstance(true);
        check(!((Boolean) jfC.getMethod("getFeature", xfC).invoke(jfT, extF)).booleanValue(), "fsp=true enforces ext func off");
        Object jfF = jfC.getConstructor(boolean.class).newInstance(false);
        check(((Boolean) jfC.getMethod("getFeature", xfC).invoke(jfF, extF)).booleanValue(), "fsp=false keeps ext func default");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
