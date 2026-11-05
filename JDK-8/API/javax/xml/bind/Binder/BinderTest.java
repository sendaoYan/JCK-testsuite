/*
 * @test
 * @summary javax.xml.bind.Binder conformance: in-place DOM marshal/unmarshal closure,
 *          updateXML/updateJAXB sync, property and schema contract.
 * @run main BinderTest
 */

import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;


public class BinderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Person {
        @XmlElement
        String name;
        @XmlElement
        int age;
    }

    static final String XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><person><name>alice</name><age>30</age></person>";

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.Binder");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("unmarshal"), "has method unmarshal");
        check(names.contains("marshal"), "has method marshal");
        check(names.contains("getXMLNode"), "has method getXMLNode");
        check(names.contains("getJAXBNode"), "has method getJAXBNode");
        check(names.contains("updateXML"), "has method updateXML");
        check(names.contains("updateJAXB"), "has method updateJAXB");
        check(names.contains("setSchema"), "has method setSchema");
        check(names.contains("getSchema"), "has method getSchema");
        check(names.contains("setEventHandler"), "has method setEventHandler");
        check(names.contains("getEventHandler"), "has method getEventHandler");
        check(names.contains("setProperty"), "has method setProperty");
        check(names.contains("getProperty"), "has method getProperty");
        // marshal/unmarshal are generic-shaped: Binder<T> binds a specific XML node type
        boolean hasMarshal = false;
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals("marshal")) hasMarshal = true;
        check(hasMarshal, "marshal declared on the abstract class");
        // --- appended deep coverage ------------------------------------------
        domClosure();
        syncContract();
        propertyAndSchema();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static DocumentBuilder db() throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    static void domClosure() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(Person.class);
        Binder<org.w3c.dom.Node> b = ctx.createBinder(org.w3c.dom.Node.class);
        check(b != null, "createBinder(Node.class) non-null");
        // marshal into an empty document appends the root element
        Document d = db().newDocument();
        Person p = new Person();
        p.name = "alice";
        p.age = 30;
        b.marshal(p, d);
        Element root = d.getDocumentElement();
        check(root != null, "marshal attaches a root element to the document");
        check("person".equals(root.getLocalName()), "root element name is derived, got " + root.getLocalName());
        check(root.getElementsByTagName("name").getLength() == 1, "one <name> child in the DOM");
        check(root.getElementsByTagName("age").getLength() == 1, "one <age> child in the DOM");
        // unmarshal from the DOM node restores the object
        Person back = (Person) b.unmarshal(root);
        check(back != null && "alice".equals(back.name) && back.age == 30, "unmarshal(DOM) closure restores fields");
        // typed form returns a JAXBElement payload
        javax.xml.bind.JAXBElement<Person> je = b.unmarshal(root, Person.class);
        check(je != null && "alice".equals(je.getValue().name), "unmarshal(DOM, Class) restores value");
        // unmarshal from a parsed document too
        Document d2 = db().parse(new InputSource(new StringReader(XML)));
        Person p2 = (Person) b.unmarshal(d2);
        check("alice".equals(p2.name) && p2.age == 30, "unmarshal(Document) restores fields");
        // association: the unmarshalled object maps back to its XML node
        Object xmlNode = b.getXMLNode(back);
        check(xmlNode instanceof org.w3c.dom.Node, "getXMLNode returns the associated DOM node");
        check(xmlNode == root || (xmlNode instanceof Element
              && "person".equals(((Element) xmlNode).getLocalName())), "associated node is the marshalled root");
        // updateJAXB re-reads the DOM into the associated object
        root.getElementsByTagName("name").item(0).setTextContent("carol");
        Person refreshed = (Person) b.updateJAXB(root);
        check(refreshed != null, "updateJAXB returns the refreshed object");
        check("carol".equals(refreshed.name), "updateJAXB picks up DOM edits, got " + refreshed.name);
    }

    static void syncContract() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(Person.class);
        Binder<org.w3c.dom.Node> b = ctx.createBinder(org.w3c.dom.Node.class);
        Document d = db().newDocument();
        Person p = new Person();
        p.name = "alice";
        p.age = 30;
        b.marshal(p, d);
        Element root = d.getDocumentElement();
        Person bound = (Person) b.unmarshal(root);
        bound.name = "bob";
        b.updateXML(bound);
        // measured: updateXML rewrites the DOM by replacing the root; look it up fresh
        Element afterUpdate = d.getDocumentElement();
        check(afterUpdate != null, "document still has a root after updateXML");
        check(afterUpdate.getElementsByTagName("name").getLength() == 1, "one <name> after updateXML");
        check("bob".equals(afterUpdate.getElementsByTagName("name").item(0).getTextContent()),
              "updateXML propagates object edits into the DOM");
        check(afterUpdate.getElementsByTagName("age").getLength() == 1
              && "30".equals(afterUpdate.getElementsByTagName("age").item(0).getTextContent()),
              "untouched fields survive updateXML");
        // a foreign node (never bound) has no JAXB association
        Document d3 = db().newDocument();
        org.w3c.dom.Element orphan = d3.createElement("orphan");
        check(b.getJAXBNode(orphan) == null, "getJAXBNode(unbound node) is null");
        check(b.getXMLNode(new Person()) == null, "getXMLNode(unbound object) is null");
        // XMLStreamReader flavor of createBinder exists in the API but is rejected by the JDK-8 RI
        try {
            ctx.createBinder(javax.xml.stream.XMLStreamReader.class);
            check(false, "createBinder(XMLStreamReader) measured: unexpectedly supported");
        } catch (UnsupportedOperationException e) {
            check(true, "createBinder(XMLStreamReader.class) throws UnsupportedOperationException (measured)");
        }
    }

    static void propertyAndSchema() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(Person.class);
        Binder<org.w3c.dom.Node> b = ctx.createBinder(org.w3c.dom.Node.class);
        check(Boolean.FALSE.equals(b.getProperty(Marshaller.JAXB_FORMATTED_OUTPUT)),
              "binder formatted output defaults to false");
        b.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        check(Boolean.TRUE.equals(b.getProperty(Marshaller.JAXB_FORMATTED_OUTPUT)),
              "binder property set/get round-trip");
        b.setProperty(Marshaller.JAXB_ENCODING, "UTF-16");
        check("UTF-16".equals(b.getProperty(Marshaller.JAXB_ENCODING)), "binder encoding round-trip");
        try {
            b.setProperty("bogus", "x");
            check(false, "binder setProperty(unknown) must throw");
        } catch (PropertyException e) {
            check(true, "binder setProperty(unknown) throws PropertyException");
        }
        try {
            b.getProperty("bogus");
            check(false, "binder getProperty(unknown) must throw");
        } catch (PropertyException e) {
            check(true, "binder getProperty(unknown) throws PropertyException");
        }
        check(b.getSchema() == null, "binder schema defaults to null");
        b.setSchema(null);
        check(b.getSchema() == null, "binder setSchema(null) keeps null");
        javax.xml.bind.ValidationEventHandler h = b.getEventHandler();
        check(h != null, "binder event handler defaults to non-null");
        javax.xml.bind.ValidationEventHandler mine = new javax.xml.bind.ValidationEventHandler() {
            public boolean handleEvent(javax.xml.bind.ValidationEvent ev) { return true; }
        };
        b.setEventHandler(mine);
        check(b.getEventHandler() == mine, "binder event handler identity");
        b.setEventHandler(null);
        check(b.getEventHandler() != mine, "setEventHandler(null) installs a fresh default handler (measured)");
        check(b.getEventHandler() != null, "default handler non-null after reset");
    }
}
