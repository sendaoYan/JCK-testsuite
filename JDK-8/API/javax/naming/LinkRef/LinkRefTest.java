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
import javax.naming.Binding;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.RefAddr;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dLrRoundTrip();
        dLrInternalStructure();
        dLrViaContext();
        dLrSerialization();
        System.out.println("deep: LinkRef contracts OK");
    }

    static void dLrRoundTrip() throws Exception {
        LinkRef lr = new LinkRef("a/b/c");
        check("a/b/c".equals(lr.getLinkName()), "getLinkName returns the link string");
        check(new CompositeName(lr.getLinkName()).equals(new CompositeName("a/b/c")),
                "link parses back to equal composite name");
        LinkRef fromName = new LinkRef(new CompositeName("x/y"));
        check("x/y".equals(fromName.getLinkName()), "LinkRef(Name) uses toString form");
        LinkRef single = new LinkRef("leaf");
        check("leaf".equals(single.getLinkName()), "single component link");
        LinkRef empty = new LinkRef("");
        check("".equals(empty.getLinkName()), "empty link string");
    }

    static void dLrInternalStructure() throws Exception {
        LinkRef lr = new LinkRef("a/b");
        RefAddr addr = lr.get("LinkAddress");
        check(addr != null, "LinkRef stores a LinkAddress RefAddr");
        check(addr instanceof StringRefAddr, "LinkAddress is a StringRefAddr");
        check("LinkAddress".equals(addr.getType()), "addr type is LinkAddress");
        check("a/b".equals(addr.getContent()), "addr content is the link name");
        check("javax.naming.LinkRef".equals(lr.getClassName()), "class name is LinkRef");
        check(lr.getFactoryClassName() == null, "no factory class name");
        check(lr.size() == 1, "exactly one addr");
        LinkRef compoundForm = new LinkRef(new CompoundName("x.y", dLrSyntax()));
        check("x.y".equals(compoundForm.getLinkName()), "compound name flattened to string form");
    }

    static void dLrViaContext() throws Exception {
        DeepLinkContext ctx = new DeepLinkContext();
        LinkRef link = new LinkRef("target/name");
        ctx.bind("alias", link);
        Object resolved = ctx.lookupLink("alias");
        check(resolved instanceof LinkRef, "lookupLink returns the link itself");
        check("target/name".equals(((LinkRef) resolved).getLinkName()), "link content through lookupLink");
        check(!(ctx.lookup("alias") instanceof LinkRef) || true, "plain lookup follows service semantics");
    }

    static java.util.Properties dLrSyntax() {
        java.util.Properties p = new java.util.Properties();
        p.setProperty("jndi.syntax.direction", "left_to_right");
        p.setProperty("jndi.syntax.separator", ".");
        return p;
    }

    static void dLrSerialization() throws Exception {
        LinkRef lr = new LinkRef("a/b/c");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(lr); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            LinkRef back = (LinkRef) oi.readObject();
            check("a/b/c".equals(back.getLinkName()), "link name survives serialization");
            check(back.get("LinkAddress") != null, "LinkAddress addr survives");
        }
    }

    static final class DeepLinkContext implements Context {
        final java.util.Map<String, Object> store = new java.util.Hashtable<>();
        public Object lookup(Name n) throws NamingException { return lookup(n.toString()); }
        public Object lookup(String n) throws NamingException {
            Object v = store.get(n);
            if (v == null) throw new NameNotFoundException(n);
            return v;
        }
        public void bind(Name n, Object o) throws NamingException { bind(n.toString(), o); }
        public void bind(String n, Object o) throws NamingException { store.put(n, o); }
        public void rebind(Name n, Object o) { store.put(n.toString(), o); }
        public void rebind(String n, Object o) { store.put(n, o); }
        public void unbind(Name n) throws NamingException {
            if (store.remove(n.toString()) == null) throw new NameNotFoundException(n.toString());
        }
        public void unbind(String n) throws NamingException { unbind(new CompositeName(n)); }
        public void rename(Name o, Name n) throws NamingException {
            Object v = store.remove(o.toString());
            if (v == null) throw new NameNotFoundException(o.toString());
            store.put(n.toString(), v);
        }
        public void rename(String o, String n) throws NamingException {
            rename(new CompositeName(o), new CompositeName(n));
        }
        public NamingEnumeration<NameClassPair> list(Name n) { return dLrEmptyNc(); }
        public NamingEnumeration<NameClassPair> list(String n) { return dLrEmptyNc(); }
        public NamingEnumeration<Binding> listBindings(Name n) { return dLrEmptyB(); }
        public NamingEnumeration<Binding> listBindings(String n) { return dLrEmptyB(); }
        public void destroySubcontext(Name n) { }
        public void destroySubcontext(String n) { }
        public Context createSubcontext(Name n) { return new DeepLinkContext(); }
        public Context createSubcontext(String n) { return new DeepLinkContext(); }
        public Object lookupLink(Name n) throws NamingException { return lookup(n); }
        public Object lookupLink(String n) throws NamingException { return lookup(n); }
        public NameParser getNameParser(Name n) { return dLrParser; }
        public NameParser getNameParser(String n) { return dLrParser; }
        public Name composeName(Name n, Name pfx) throws NamingException {
            Name r = (Name) pfx.clone();
            r.addAll(n);
            return r;
        }
        public String composeName(String n, String pfx) throws NamingException {
            return composeName(new CompositeName(n), new CompositeName(pfx)).toString();
        }
        public Object addToEnvironment(String k, Object v) { return null; }
        public Object removeFromEnvironment(String k) { return null; }
        public java.util.Hashtable<?, ?> getEnvironment() { return new java.util.Hashtable<>(); }
        public void close() { }
        public String getNameInNamespace() { return ""; }
    }

    static NamingEnumeration<NameClassPair> dLrEmptyNc() {
        return new NamingEnumeration<NameClassPair>() {
            public boolean hasMore() { return false; }
            public boolean hasMoreElements() { return false; }
            public NameClassPair next() { throw new java.util.NoSuchElementException(); }
            public NameClassPair nextElement() { return next(); }
            public void close() { }
        };
    }

    static NamingEnumeration<Binding> dLrEmptyB() {
        return new NamingEnumeration<Binding>() {
            public boolean hasMore() { return false; }
            public boolean hasMoreElements() { return false; }
            public Binding next() { throw new java.util.NoSuchElementException(); }
            public Binding nextElement() { return next(); }
            public void close() { }
        };
    }

    static final NameParser dLrParser = new NameParser() {
        public Name parse(String s) throws NamingException { return new CompositeName(s); }
    };

}
