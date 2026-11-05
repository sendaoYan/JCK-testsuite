/*
 * @test
 * @summary javax.xml.bind.annotation.XmlElementDecl.GLOBAL conformance: GLOBAL marker.
 * @run main GLOBALTest
 */


public class GLOBALTest {
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
        javax.xml.bind.annotation.XmlElementDecl.GLOBAL g = new javax.xml.bind.annotation.XmlElementDecl.GLOBAL();
        check(g != null, "constructed");
        testClassContract();
        testUsageAsSentinel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassContract() throws Exception {
        Class<?> c = javax.xml.bind.annotation.XmlElementDecl.GLOBAL.class;
        check(c.getName().equals("javax.xml.bind.annotation.XmlElementDecl$GLOBAL"),
                "binary name is XmlElementDecl$GLOBAL");
        check(c.getSimpleName().equals("GLOBAL"), "simple name GLOBAL");
        check(c.getCanonicalName().equals("javax.xml.bind.annotation.XmlElementDecl.GLOBAL"),
                "canonical name uses dot notation");
        check(c.getEnclosingClass() == javax.xml.bind.annotation.XmlElementDecl.class, "enclosed by XmlElementDecl");
        check(c.getPackage().getName().equals("javax.xml.bind.annotation"), "package javax.xml.bind.annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isStatic(c.getModifiers()), "static nested");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract (instantiable)");
        check(!c.isInterface(), "not an interface");
        check(!c.isAnnotation(), "not an annotation");
        check(!c.isEnum(), "not an enum");
        check(!c.isArray(), "not an array");
        check(!c.isPrimitive(), "not primitive");
        check(c.isMemberClass(), "is a member class");
        check(!c.isLocalClass(), "not a local class");
        check(!c.isAnonymousClass(), "not anonymous");
        check(!c.isSynthetic(), "not synthetic");
        check(c.getSuperclass() == Object.class, "direct superclass Object");
        check(c.getInterfaces().length == 0, "implements no interfaces");
        check(c.getDeclaredMethods().length == 0, "no declared methods");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getConstructors().length == 1, "exactly 1 public constructor (no-arg)");
        check(c.getDeclaredConstructors().length == 1, "exactly 1 declared constructor");
        check(c.getAnnotations().length == 0, "no annotations on the marker class");
        check(c.getDeclaredAnnotations().length == 0, "no declared annotations");
        check(Class.forName("javax.xml.bind.annotation.XmlElementDecl$GLOBAL") == c,
                "Class.forName resolves to same Class object");
    }

    static void testUsageAsSentinel() throws Exception {
        javax.xml.bind.annotation.XmlElementDecl.GLOBAL g1 = new javax.xml.bind.annotation.XmlElementDecl.GLOBAL();
        javax.xml.bind.annotation.XmlElementDecl.GLOBAL g2 = new javax.xml.bind.annotation.XmlElementDecl.GLOBAL();
        check(g1 != g2, "does not override identity semantics (two instances not identical)");
        check(!g1.equals(g2), "default Object.equals: distinct instances not equals()");
        check(g1.equals(g1), "reflexive equals");
        check(g1.toString() != null && g1.toString().contains("GLOBAL"), "toString mentions GLOBAL");
        check(g1.hashCode() == g1.hashCode(), "hashCode stable within instance");
        Class<?> c = javax.xml.bind.annotation.XmlElementDecl.GLOBAL.class;
        check(c.isInstance(g1), "isInstance for its own instance");
        check(!c.isInstance("str"), "isInstance false for unrelated type");
        check(c.isAssignableFrom(c), "isAssignableFrom itself");
        check(!c.isAssignableFrom(String.class), "not assignable from String");
        check(c.cast(g2) == g2, "cast returns same reference");
        check(c.asSubclass(Object.class) == c, "asSubclass(Object) is itself");
        check(javax.xml.bind.annotation.XmlElementDecl.class.getDeclaredMethod("scope").getDefaultValue() == c,
                "XmlElementDecl.scope() default value IS this GLOBAL sentinel class");
        javax.xml.bind.annotation.XmlElementDecl ann =
                Marker.class.getDeclaredMethod("createFoo").getAnnotation(javax.xml.bind.annotation.XmlElementDecl.class);
        check(ann.scope() == javax.xml.bind.annotation.XmlElementDecl.GLOBAL.class,
                "bare @XmlElementDecl read-back yields GLOBAL for scope()");
        check(ann.scope() == javax.xml.bind.JAXBElement.GlobalScope.class == false,
                "GLOBAL sentinel is distinct from JAXBElement.GlobalScope");
    }

    static class Marker {
        @javax.xml.bind.annotation.XmlElementDecl(name = "foo")
        javax.xml.bind.JAXBElement<String> createFoo() { return null; }
    }
}
