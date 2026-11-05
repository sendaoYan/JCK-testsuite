/*
 * @test
 * @summary javax.xml.bind.annotation.XmlIDREF conformance: annotation interface and members.
 * @run main XmlIDREFTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlIDREFTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlIDREF");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        testMeta();
        testReadBack();
        testMarshalAndResolve();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMeta() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlIDREF");
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
        check(c == javax.xml.bind.annotation.XmlIDREF.class, "Class.forName identity with compile-time reference");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "person")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class Person {
        @javax.xml.bind.annotation.XmlAttribute
        @javax.xml.bind.annotation.XmlID
        String id;
        @javax.xml.bind.annotation.XmlElement
        String name;
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "team")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class Team {
        @javax.xml.bind.annotation.XmlElement(name = "member")
        java.util.List<Person> members;
        @javax.xml.bind.annotation.XmlIDREF
        @javax.xml.bind.annotation.XmlElement(name = "lead")
        Person lead;
        @javax.xml.bind.annotation.XmlIDREF
        @javax.xml.bind.annotation.XmlElement(name = "peers")
        java.util.List<Person> peers;
        String note;
    }

    static class LoneRef {
        @javax.xml.bind.annotation.XmlIDREF
        @javax.xml.bind.annotation.XmlElement
        Person p;
        String none;
    }

    @javax.xml.bind.annotation.XmlIDREF
    static Person annotatedMethod() { return null; }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlIDREF a =
                Team.class.getDeclaredField("lead").getAnnotation(javax.xml.bind.annotation.XmlIDREF.class);
        check(a != null, "@XmlIDREF retrievable from FIELD target");
        check(a.annotationType() == javax.xml.bind.annotation.XmlIDREF.class, "annotationType() identity");
        check(Team.class.getDeclaredField("lead")
                .getAnnotation(javax.xml.bind.annotation.XmlElement.class) != null,
                "@XmlIDREF field co-annotated with @XmlElement");
        javax.xml.bind.annotation.XmlIDREF listRef =
                Team.class.getDeclaredField("peers").getAnnotation(javax.xml.bind.annotation.XmlIDREF.class);
        check(listRef != null, "@XmlIDREF also applicable to List of ID-holding type");
        javax.xml.bind.annotation.XmlIDREF m =
                XmlIDREFTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlIDREF.class);
        check(m != null, "@XmlIDREF retrievable from METHOD target");
        check(m.equals(a), "marker instances from different targets are equals()");
        check(m.hashCode() == a.hashCode(), "hashCode consistent for marker instances");
        check(a.toString().contains("XmlIDREF"), "toString mentions XmlIDREF");
        javax.xml.bind.annotation.XmlIDREF other =
                LoneRef.class.getDeclaredField("p").getAnnotation(javax.xml.bind.annotation.XmlIDREF.class);
        check(other != null && other.equals(a), "instances from unrelated classes still equals()");
        check(Team.class.getDeclaredField("note")
                .getAnnotation(javax.xml.bind.annotation.XmlIDREF.class) == null,
                "unannotated field has no @XmlIDREF");
        check(Team.class.getAnnotation(javax.xml.bind.annotation.XmlIDREF.class) == null,
                "TYPE-level retrieval is null (not a legal target)");
        javax.xml.bind.annotation.XmlIDREF again =
                Team.class.getDeclaredField("lead").getAnnotation(javax.xml.bind.annotation.XmlIDREF.class);
        check(a.equals(again) && a.hashCode() == again.hashCode(), "repeated retrieval equals/hashCode consistent");
    }

    static void testMarshalAndResolve() throws Exception {
        Person p1 = new Person();
        p1.id = "a";
        p1.name = "alice";
        Person p2 = new Person();
        p2.id = "b";
        p2.name = "bob";
        Team t = new Team();
        t.members = new java.util.ArrayList<Person>(java.util.Arrays.asList(p1, p2));
        t.lead = p1;
        t.peers = new java.util.ArrayList<Person>(java.util.Arrays.asList(p1, p2));
        t.note = "n";
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Team.class, Person.class);
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(t, w);
        String xml = w.toString();
        check(xml.contains("<team"), "root element present");
        check(xml.contains("<member id=\"a\">"), "member serialized with its @XmlID attribute");
        check(xml.contains("<name>alice</name>"), "member content serialized");
        check(xml.contains("<lead>a</lead>"), "@XmlIDREF marshals as the referenced object's ID string, not a copy");
        check(!xml.contains("<lead><person"), "@XmlIDREF does NOT inline the whole referenced object");
        check(xml.contains("<peers>a</peers>"), "List-valued @XmlIDREF renders one ID token per item");
        check(xml.contains("<peers>b</peers>"), "second list ID token rendered");
        Team back = (Team) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml));
        check(back.members.size() == 2, "members round-trip");
        check(back.lead != null, "unmarshal resolves IDREF to object");
        check("a".equals(back.lead.id) && "alice".equals(back.lead.name), "resolved lead has full content");
        check(back.lead == back.members.get(0), "IDREF resolves to the SAME instance held by members (identity)");
        check(back.peers != null && back.peers.size() == 2, "peer list round-trip size");
        check(back.peers.get(0) == back.members.get(0) && back.peers.get(1) == back.members.get(1),
                "every list IDREF resolves to the canonical member instance");
    }
}
