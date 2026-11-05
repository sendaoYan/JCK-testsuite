/*
 * @test
 * @summary javax.xml.bind.annotation.XmlElementRef conformance: annotation interface and members.
 * @run main XmlElementRefTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlElementRefTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElementRef");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("type"), "has method type");
        check(names.contains("namespace"), "has method namespace");
        check(names.contains("name"), "has method name");
        check(names.contains("required"), "has method required");
        testMeta();
        testDefaults();
        testReadBack();
        testMarshalLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMeta() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElementRef");
        java.lang.annotation.Retention ret = c.getAnnotation(java.lang.annotation.Retention.class);
        check(ret != null && ret.value() == java.lang.annotation.RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        java.lang.annotation.Target tgt = c.getAnnotation(java.lang.annotation.Target.class);
        check(tgt != null, "@Target present");
        java.lang.annotation.ElementType[] et = tgt.value();
        check(et.length == 2, "@Target has exactly 2 elements (FIELD, METHOD)");
        java.util.Set<java.lang.annotation.ElementType> ts =
                new java.util.HashSet<java.lang.annotation.ElementType>(java.util.Arrays.asList(et));
        check(ts.contains(java.lang.annotation.ElementType.FIELD), "@Target contains FIELD");
        check(ts.contains(java.lang.annotation.ElementType.METHOD), "@Target contains METHOD");
        check(!ts.contains(java.lang.annotation.ElementType.PARAMETER), "@Target does NOT contain PARAMETER");
        check(!ts.contains(java.lang.annotation.ElementType.TYPE), "@Target does NOT contain TYPE");
        check(!c.isAnnotationPresent(java.lang.annotation.Inherited.class), "not @Inherited");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented");
        check(!c.isAnnotationPresent(java.lang.annotation.Repeatable.class), "not @Repeatable");
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) declared.add(m.getName());
        check(declared.size() == 4, "exactly 4 declared members");
        check(declared.containsAll(java.util.Arrays.asList("type", "namespace", "name", "required")),
                "member name set exact");
        check(c.getDeclaredMethod("type").getReturnType() == Class.class, "type() returns Class");
        check(c.getDeclaredMethod("namespace").getReturnType() == String.class, "namespace() returns String");
        check(c.getDeclaredMethod("name").getReturnType() == String.class, "name() returns String");
        check(c.getDeclaredMethod("required").getReturnType() == boolean.class, "required() returns boolean");
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "member " + m.getName() + " is abstract");
        }
    }

    static void testDefaults() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElementRef");
        check(c.getDeclaredMethod("name").getDefaultValue().equals("##default"), "name() default \"##default\"");
        check("".equals(c.getDeclaredMethod("namespace").getDefaultValue()),
                "namespace() default is EMPTY STRING (unlike XmlElement's \"##default\")");
        check(Boolean.TRUE.equals(c.getDeclaredMethod("required").getDefaultValue()),
                "required() default TRUE (unlike XmlElement's false)");
        check(c.getDeclaredMethod("type").getDefaultValue() == javax.xml.bind.annotation.XmlElementRef.DEFAULT.class,
                "type() default is XmlElementRef.DEFAULT.class sentinel");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "plain")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class Plain {
        @javax.xml.bind.annotation.XmlElement
        String v;
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "refHost")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class RefHost {
        @javax.xml.bind.annotation.XmlElementRef(type = Plain.class)
        Plain one;
        String note;
    }

    static class ExplicitRef {
        @javax.xml.bind.annotation.XmlElementRef(name = "r", type = Plain.class, namespace = "urn:z", required = false)
        Plain p;
        String none;
    }

    static class BadRef {
        // no @XmlRootElement on target: illegal @XmlElementRef
        @javax.xml.bind.annotation.XmlElementRef(type = NotRoot.class)
        NotRoot bad;
    }

    static class NotRoot { public String f; }

    @javax.xml.bind.annotation.XmlElementRef(name = "mref")
    static javax.xml.bind.JAXBElement<String> annotatedMethod() { return null; }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlElementRef a =
                RefHost.class.getDeclaredField("one").getAnnotation(javax.xml.bind.annotation.XmlElementRef.class);
        check(a != null, "@XmlElementRef retrievable from FIELD target");
        check(a.type() == Plain.class, "explicit type read back");
        check("##default".equals(a.name()), "unspecified name defaults \"##default\" on read-back");
        check("".equals(a.namespace()), "unspecified namespace defaults empty string on read-back");
        check(a.required(), "unspecified required defaults true on read-back");
        check(a.annotationType() == javax.xml.bind.annotation.XmlElementRef.class, "annotationType() identity");
        javax.xml.bind.annotation.XmlElementRef full =
                ExplicitRef.class.getDeclaredField("p").getAnnotation(javax.xml.bind.annotation.XmlElementRef.class);
        check("r".equals(full.name()), "explicit name read back");
        check("urn:z".equals(full.namespace()), "explicit namespace read back");
        check(!full.required(), "explicit required=false read back");
        check(!a.equals(full), "different member values give non-equal instances");
        check(a.hashCode() != full.hashCode(), "hashCodes differ for different member values");
        javax.xml.bind.annotation.XmlElementRef m =
                XmlElementRefTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlElementRef.class);
        check(m != null, "@XmlElementRef retrievable from METHOD target");
        check("mref".equals(m.name()), "method-level name read back");
        check(m.type() == javax.xml.bind.annotation.XmlElementRef.DEFAULT.class,
                "method-level unspecified type defaults to DEFAULT sentinel");
        check(RefHost.class.getDeclaredField("note")
                .getAnnotation(javax.xml.bind.annotation.XmlElementRef.class) == null,
                "unannotated field has no @XmlElementRef");
        check(RefHost.class.getAnnotation(javax.xml.bind.annotation.XmlElementRef.class) == null,
                "TYPE-level retrieval is null (not a legal target)");
        javax.xml.bind.annotation.XmlElementRef again =
                RefHost.class.getDeclaredField("one").getAnnotation(javax.xml.bind.annotation.XmlElementRef.class);
        check(a.equals(again) && a.hashCode() == again.hashCode(), "repeated retrieval equals/hashCode consistent");
    }

    static void testMarshalLoop() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(RefHost.class, Plain.class);
        RefHost r = new RefHost();
        Plain p = new Plain();
        p.v = "pv";
        r.one = p;
        r.note = "n";
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(r, w);
        String xml = w.toString();
        check(xml.contains("<refHost"), "root element present");
        check(xml.contains("<plain><v>pv</v></plain>"),
                "reference rendered using the TARGET TYPE's @XmlRootElement name (name \"##default\" derivation)");
        check(xml.contains("<note>n</note>"), "sibling plain field marshals normally");
        RefHost back = (RefHost) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml));
        check(back.one != null && "pv".equals(back.one.v), "referenced object round-trip");
        try {
            javax.xml.bind.JAXBContext.newInstance(BadRef.class);
            check(false, "@XmlElementRef to non-@XmlRootElement type must be rejected");
        } catch (javax.xml.bind.JAXBException e) {
            check(e.getMessage() != null && e.getMessage().contains("IllegalAnnotationExceptions"),
                    "ref to non-root type rejected with IllegalAnnotationExceptions");
        }
    }
}
