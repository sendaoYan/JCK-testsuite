/*
 * @test
 * @summary javax.xml.bind.annotation.XmlElementRef.DEFAULT conformance: DEFAULT marker.
 * @run main DEFAULTTest
 */


public class DEFAULTTest {
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
        javax.xml.bind.annotation.XmlElementRef.DEFAULT d = new javax.xml.bind.annotation.XmlElementRef.DEFAULT();
        check(d != null, "constructed");
        testClassContract();
        testUsageAsSentinel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassContract() throws Exception {
        Class<?> c = javax.xml.bind.annotation.XmlElementRef.DEFAULT.class;
        check(c.getName().equals("javax.xml.bind.annotation.XmlElementRef$DEFAULT"),
                "binary name is XmlElementRef$DEFAULT");
        check(c.getSimpleName().equals("DEFAULT"), "simple name DEFAULT");
        check(c.getCanonicalName().equals("javax.xml.bind.annotation.XmlElementRef.DEFAULT"),
                "canonical name uses dot notation");
        check(c.getEnclosingClass() == javax.xml.bind.annotation.XmlElementRef.class, "enclosed by XmlElementRef");
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
        check(Class.forName("javax.xml.bind.annotation.XmlElementRef$DEFAULT") == c,
                "Class.forName resolves to same Class object");
    }

    static void testUsageAsSentinel() throws Exception {
        javax.xml.bind.annotation.XmlElementRef.DEFAULT d1 = new javax.xml.bind.annotation.XmlElementRef.DEFAULT();
        javax.xml.bind.annotation.XmlElementRef.DEFAULT d2 = new javax.xml.bind.annotation.XmlElementRef.DEFAULT();
        check(d1 != d2, "does not override identity semantics (two instances not identical)");
        check(!d1.equals(d2), "default Object.equals: distinct instances not equals()");
        check(d1.equals(d1), "reflexive equals");
        check(d1.toString() != null && d1.toString().contains("DEFAULT"), "toString mentions DEFAULT");
        check(d1.hashCode() == d1.hashCode(), "hashCode stable within instance");
        Class<?> c = javax.xml.bind.annotation.XmlElementRef.DEFAULT.class;
        check(c.isInstance(d1), "isInstance for its own instance");
        check(!c.isInstance("str"), "isInstance false for unrelated type");
        check(c.isAssignableFrom(c), "isAssignableFrom itself");
        check(!c.isAssignableFrom(String.class), "not assignable from String");
        check(c.cast(d2) == d2, "cast returns same reference");
        check(c.asSubclass(Object.class) == c, "asSubclass(Object) is itself");
        check(javax.xml.bind.annotation.XmlElementRef.class.getDeclaredMethod("type").getDefaultValue() == c,
                "XmlElementRef.type() default value IS this DEFAULT sentinel class");
        javax.xml.bind.annotation.XmlElementRef ann =
                Marker.class.getDeclaredField("f").getAnnotation(javax.xml.bind.annotation.XmlElementRef.class);
        check(ann.type() == javax.xml.bind.annotation.XmlElementRef.DEFAULT.class,
                "bare @XmlElementRef read-back yields DEFAULT for type()");
    }

    static class Marker {
        @javax.xml.bind.annotation.XmlElementRef
        static javax.xml.bind.JAXBElement<String> f;
    }
}
