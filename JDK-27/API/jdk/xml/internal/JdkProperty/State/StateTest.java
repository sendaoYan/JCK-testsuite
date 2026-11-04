/*
 * @test
 * @summary jdk.xml.internal.JdkProperty.State conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main StateTest
 */
public class StateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkProperty$State", false, StateTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.JdkProperty$State"), "name");
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
        check(ec.size() == 7, "enum-constant-count");
        check(ec.contains("DEFAULT"), "enum-DEFAULT");
        check(ec.contains("FSP"), "enum-FSP");
        check(ec.contains("JAXPDOTPROPERTIES"), "enum-JAXPDOTPROPERTIES");
        check(ec.contains("LEGACY_SYSTEMPROPERTY"), "enum-LEGACY_SYSTEMPROPERTY");
        check(ec.contains("SYSTEMPROPERTY"), "enum-SYSTEMPROPERTY");
        check(ec.contains("LEGACY_APIPROPERTY"), "enum-LEGACY_APIPROPERTY");
        check(ec.contains("APIPROPERTY"), "enum-APIPROPERTY");
        check(fd.size() == 0, "field-count");
        check(md.size() == 3, "method-count");
        check(md.contains("literal"), "method-literal");
        check(md.contains("valueOf"), "method-valueOf");
        check(md.contains("values"), "method-values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
