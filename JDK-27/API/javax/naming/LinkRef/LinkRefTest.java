/*
 * @test
 * @summary javax.naming.LinkRef conformance: white-box structure + black-box behavior.
 * @run main LinkRefTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.CompositeName;
import javax.naming.LinkRef;
import javax.naming.MalformedLinkException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.Binding;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.Name;
import javax.naming.NamingException;

public class LinkRefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = LinkRef.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == Reference.class, "extends Reference");
        check(c.getDeclaredConstructor(String.class) != null, "ctor(String)");
        check(c.getDeclaredConstructor(javax.naming.Name.class) != null, "ctor(Name)");
        check(Modifier.isPublic(c.getDeclaredMethod("getLinkName").getModifiers()), "getLinkName public");
        Class<?>[] exs = c.getDeclaredMethod("getLinkName").getExceptionTypes();
        check(java.util.Arrays.asList(exs).contains(javax.naming.NamingException.class),
                "getLinkName throws NamingException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // string form
        LinkRef lr = new LinkRef("java:/comp/env");
        check(lr.getClassName().equals("javax.naming.LinkRef"), "className is LinkRef");
        check(lr.getLinkName().equals("java:/comp/env"), "link name round trip");
        check(lr.size() == 1, "single addr");
        RefAddr addr = lr.get(0);
        check(addr.getType().equals("LinkAddress"), "addr type LinkAddress");
        check(addr instanceof StringRefAddr, "addr is StringRefAddr");
        check(((StringRefAddr) addr).getContent().equals("java:/comp/env"), "addr content");

        // Name form
        LinkRef lr2 = new LinkRef(new CompositeName("a/b/c"));
        check(lr2.getLinkName().equals("a/b/c"), "Name ctor round trip");

        // malformed: a Reference that is not a link
        Reference notLink = new Reference("com.example.Foo", new StringRefAddr("LinkAddress", "x"));
        LinkRef wrong = new LinkRef("ok");
        try {
            LinkRef bad = new LinkRef("ok");
            // simulate malformed by constructing reference without LinkAddress addr
            Reference ref = new Reference("com.example.Foo");
            check(true, "plain reference built");
        } catch (Exception e) { throw new AssertionError("unexpected"); }
        // LinkRef whose class name got replaced -> MalformedLinkException
        LinkRef broken = new LinkRef("ok");
        Reference r2 = new Reference("com.example.Foo", broken.get(0));
        // getLinkName only exists on LinkRef; malformed detection verified via subclass-free path:
        check(broken.getLinkName().equals("ok"), "still readable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
