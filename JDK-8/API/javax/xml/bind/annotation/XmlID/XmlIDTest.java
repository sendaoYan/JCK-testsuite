/*
 * @test
 * @summary javax.xml.bind.annotation.XmlID conformance: annotation interface and members.
 * @run main XmlIDTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlIDTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlID");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        testMeta();
        testReadBack();
        testMarshalLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMeta() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlID");
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
        check(c.getDeclaredMethods().length == 0, "marker annotation: no declared members");
        check(c == javax.xml.bind.annotation.XmlID.class, "Class.forName identity with compile-time reference");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "entity")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class Entity {
        @javax.xml.bind.annotation.XmlAttribute
        @javax.xml.bind.annotation.XmlID
        String id;
        @javax.xml.bind.annotation.XmlElement
        String name;
    }

    static class NotId {
        @javax.xml.bind.annotation.XmlID
        String s;
        String none;
    }

    @javax.xml.bind.annotation.XmlID
    static String annotatedMethod() { return null; }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlID a =
                Entity.class.getDeclaredField("id").getAnnotation(javax.xml.bind.annotation.XmlID.class);
        check(a != null, "@XmlID retrievable from FIELD target");
        check(a.annotationType() == javax.xml.bind.annotation.XmlID.class, "annotationType() identity");
        check(Entity.class.getDeclaredField("id")
                .getAnnotation(javax.xml.bind.annotation.XmlAttribute.class) != null,
                "@XmlID field is typically co-annotated with @XmlAttribute");
        javax.xml.bind.annotation.XmlID m =
                XmlIDTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlID.class);
        check(m != null, "@XmlID retrievable from METHOD target");
        check(m.equals(a), "marker instances from different targets are equals()");
        check(m.hashCode() == a.hashCode(), "hashCode consistent for marker instances");
        check(a.toString().contains("XmlID"), "toString mentions XmlID");
        javax.xml.bind.annotation.XmlID other =
                NotId.class.getDeclaredField("s").getAnnotation(javax.xml.bind.annotation.XmlID.class);
        check(other != null && other.equals(a), "instances from unrelated classes still equals()");
        check(NotId.class.getDeclaredField("none")
                .getAnnotation(javax.xml.bind.annotation.XmlID.class) == null,
                "unannotated field has no @XmlID");
        check(Entity.class.getAnnotation(javax.xml.bind.annotation.XmlID.class) == null,
                "TYPE-level retrieval is null (not a legal target)");
        check(Entity.class.getDeclaredField("id").getType() == String.class,
                "@XmlID property must be String (spec)");
    }

    static void testMarshalLoop() throws Exception {
        Entity e = new Entity();
        e.id = "e-1";
        e.name = "alpha";
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Entity.class);
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(e, w);
        String xml = w.toString();
        check(xml.contains("<entity"), "root element present");
        check(xml.contains("id=\"e-1\""), "@XmlID value serialized as an attribute of the element");
        check(xml.contains("<name>alpha</name>"), "plain field is a child element");
        Entity back = (Entity) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml));
        check("e-1".equals(back.id), "@XmlID attribute round-trip");
        check("alpha".equals(back.name), "name round-trip");
        Entity noId = (Entity) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(
                "<?xml version=\"1.0\"?><entity><name>x</name></entity>"));
        check(noId.id == null, "absent id attribute unmarshals to null (no crash)");
        check("x".equals(noId.name), "sibling field still populated");
        javax.xml.bind.annotation.XmlID again =
                Entity.class.getDeclaredField("id").getAnnotation(javax.xml.bind.annotation.XmlID.class);
        check(javax.xml.bind.annotation.XmlID.class.getDeclaredFields().length >= 0 && again != null
                && again.equals(Entity.class.getDeclaredField("id").getAnnotation(javax.xml.bind.annotation.XmlID.class)),
                "repeated retrieval yields equal instance");
    }
}
