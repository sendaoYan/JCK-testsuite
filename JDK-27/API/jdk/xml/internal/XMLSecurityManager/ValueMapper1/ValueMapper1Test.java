/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager.ValueMapper1 conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main ValueMapper1Test
 */
public class ValueMapper1Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$ValueMapper1", false, ValueMapper1Test.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.XMLSecurityManager$ValueMapper1"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
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
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 0, "field-count");
        check(md.size() == 3, "method-count");
        check(md.contains("toInt"), "method-toInt");
        check(md.contains("toObject"), "method-toObject");
        check(md.contains("toString"), "method-toString");
        deep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void deep() throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$ValueMapper1", false, ValueMapper1Test.class.getClassLoader());
        Class<?> outer = Class.forName("jdk.xml.internal.XMLSecurityManager", false, ValueMapper1Test.class.getClassLoader());
        // it is a generic INNER class of XMLSecurityManager (not static)
        check(c.isMemberClass(), "is-member-class");
        check(!java.lang.reflect.Modifier.isStatic(c.getModifiers()), "not-static-inner");
        check(c.getEnclosingClass().equals(outer), "enclosing-is-XMLSecurityManager");
        check(c.getTypeParameters().length == 1, "one-type-parameter");
        check(c.getTypeParameters()[0].getName().equals("T"), "type-parameter-named-T");
        // the abstract contract: three public abstract conversion methods, no constructors beyond the synthetic one
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "abstract-" + m.getName());
            check(java.lang.reflect.Modifier.isPublic(m.getModifiers()), "public-" + m.getName());
            names.add(m.getName());
        }
        check(names.size() == 3, "declared-methods-3");
        check(names.contains("toInt") && names.contains("toObject") && names.contains("toString"), "method-names");
        check(c.getMethod("toInt", Object.class).getReturnType() == int.class, "toInt-returns-int");
        check(c.getMethod("toObject", int.class).getTypeParameters().length == 0, "toObject-generic-erasure");
        check(c.getMethod("toString", int.class).getReturnType() == String.class, "toString-returns-String");
        check(c.getDeclaredFields().length == 0, "no-state-fields");
        // inner-class constructor carries the enclosing instance as its only parameter
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "single-ctor");
        check(ctors[0].getParameterCount() == 1, "ctor-takes-enclosing-instance");
        check(ctors[0].getParameterTypes()[0].equals(outer), "ctor-param-is-outer");
        // the abstract class cannot be instantiated even with a valid enclosing instance
        Object xsm = outer.getDeclaredConstructor().newInstance();
        try { ctors[0].newInstance(xsm); check(false, "abstract-newInstance"); }
        catch (InstantiationException ie) { check(true, "abstract-newInstance-IE"); }
        // no enum constants and not cloneable/serializable surface
        check(!c.isEnum() && !java.io.Serializable.class.isAssignableFrom(c), "not-enum-not-serializable");
        // unrelated to the non-generic ValueMapper sibling: neither is a subtype of the other
        Class<?> sibling = Class.forName("jdk.xml.internal.XMLSecurityManager$ValueMapper", false, ValueMapper1Test.class.getClassLoader());
        check(!sibling.isAssignableFrom(c) && !c.isAssignableFrom(sibling), "independent-from-ValueMapper");
    }
}
