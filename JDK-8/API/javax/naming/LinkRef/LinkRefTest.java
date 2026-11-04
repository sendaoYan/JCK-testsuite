/*
 * @test
 * @summary javax.naming.LinkRef conformance: full contract coverage.
 *          String / Name / empty-Name constructors, getLinkName, the
 *          "LinkAddress" StringRefAddr storage, className, and serialization
 *          round-trip.
 * @run main LinkRefTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.naming.CompositeName;
import javax.naming.LinkRef;
import javax.naming.StringRefAddr;

public class LinkRefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static LinkRef roundTrip(LinkRef lr) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(lr); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (LinkRef) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() throws Exception {
        LinkRef lr = new LinkRef("cn=target");
        check("cn=target".equals(lr.getLinkName()), "getLinkName");
        check("javax.naming.LinkRef".equals(lr.getClassName()), "getClassName");
        LinkRef lr2 = new LinkRef(new CompositeName("a/b"));
        check("a/b".equals(lr2.getLinkName()), "getLinkName from Name");
    }

    static void testLinkAddressStorage() throws Exception {
        LinkRef lr = new LinkRef("cn=target");
        check(lr.size() == 1, "one RefAddr");
        check("LinkAddress".equals(lr.get(0).getType()), "LinkAddress type");
        check("cn=target".equals(((StringRefAddr) lr.get(0)).getContent()), "LinkAddress content");
        check(lr.getFactoryClassName() == null, "factory null");
    }

    static void testEmptyName() throws Exception {
        LinkRef lr = new LinkRef(new CompositeName());
        check("".equals(lr.getLinkName()), "empty name -> empty link");
    }

    static void testSerialization() throws Exception {
        LinkRef lr = new LinkRef("cn=target");
        LinkRef back = roundTrip(lr);
        check("cn=target".equals(back.getLinkName()), "round trip link name");
        check("javax.naming.LinkRef".equals(back.getClassName()), "round trip className");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testLinkAddressStorage();
        testEmptyName();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
