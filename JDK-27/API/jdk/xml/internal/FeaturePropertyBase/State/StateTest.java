/*
 * @test
 * @summary jdk.xml.internal.FeaturePropertyBase.State conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main StateTest
 */
public class StateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.FeaturePropertyBase$State", false, StateTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.FeaturePropertyBase$State"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 0, "interface-count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 5, "enum-constant-count");
        check(ec.contains("DEFAULT"), "enum-DEFAULT");
        check(ec.contains("FSP"), "enum-FSP");
        check(ec.contains("JAXPDOTPROPERTIES"), "enum-JAXPDOTPROPERTIES");
        check(ec.contains("SYSTEMPROPERTY"), "enum-SYSTEMPROPERTY");
        check(ec.contains("APIPROPERTY"), "enum-APIPROPERTY");
        check(fd.size() == 0, "field-count");
        check(md.size() == 2, "method-count");
        check(md.contains("valueOf"), "method-valueOf");
        check(md.contains("values"), "method-values");
        deep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void deep() throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.FeaturePropertyBase$State", false, StateTest.class.getClassLoader());
        Object[] vals = c.getEnumConstants();
        check(vals.length == 5, "values-length-5");
        // exact declaration order: the overriding precedence order
        String[] order = {"DEFAULT", "FSP", "JAXPDOTPROPERTIES", "SYSTEMPROPERTY", "APIPROPERTY"};
        for (int i = 0; i < order.length; i++) {
            check(vals[i].toString().equals(order[i]), "order-" + order[i]);
            check(((Enum<?>) vals[i]).ordinal() == i, "ordinal-" + order[i]);
        }
        // valueOf round trip for every constant
        for (Object v : vals) {
            Object rt = java.lang.Enum.valueOf((Class<Enum>) (Class) c, v.toString());
            check(rt == v, "valueOf-roundtrip-" + v);
        }
        // compareTo implements the precedence: later constant wins ties in setValue
        for (int i = 0; i < vals.length - 1; i++) {
            check(((Comparable) vals[i]).compareTo(vals[i + 1]) < 0, "cmp-" + order[i] + "-lt-" + order[i + 1]);
            check(((Comparable) vals[i + 1]).compareTo(vals[i]) > 0, "cmp-" + order[i + 1] + "-gt-" + order[i]);
        }
        check(((Comparable) vals[0]).compareTo(vals[0]) == 0, "cmp-self-zero");
        // name() values are the API literals; no extra fields carried
        check(((Enum<?>) vals[1]).name().equals("FSP"), "name-FSP");
        try { java.lang.Enum.valueOf((Class<Enum>) (Class) c, "NO_SUCH_STATE"); check(false, "valueOf-bogus"); }
        catch (IllegalArgumentException e) { check(true, "valueOf-bogus-IAE"); }
        check(c.getDeclaredField("DEFAULT").isEnumConstant(), "field-DEFAULT-enum-const");
        check(c.isInstance(vals[4]), "isInstance-last");
        check(((Enum<?>) vals[0]).getDeclaringClass() == c, "declaring-class");
    }
}
