/*
 * @test
 * @summary javax.naming.ldap.LdapContext conformance: interface hierarchy and members.
 * @run main LdapContextTest
 */



import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;
public class LdapContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.LdapContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("javax.naming.directory.DirContext");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addToEnvironment"), "has method addToEnvironment");
        check(names.contains("bind"), "has method bind");
        check(names.contains("close"), "has method close");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dLcSurface();
        dLcControlLifecycle();
        dLcExtendedOperation();
        System.out.println("deep: LdapContext contracts OK");
    }

    static void dLcSurface() throws Exception {
        check(DirContext.class.isAssignableFrom(LdapContext.class), "extends DirContext");
        check("java.naming.factory.control".equals(LdapContext.CONTROL_FACTORIES),
                "CONTROL_FACTORIES constant value");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : LdapContext.class.getMethods()) names.add(m.getName());
        for (String want : new String[] {"extendedOperation", "newInstance", "reconnect",
                "getConnectControls", "setRequestControls", "getRequestControls",
                "getResponseControls"})
            check(names.contains(want), "declares " + want);
    }

    /** Recording LdapContext implementation (context part is minimal). */
    public static class RecLdapCtx extends DeepPlainCtx implements LdapContext {
        public final java.util.List<String> log = new java.util.ArrayList<>();
        public Control[] connCtls, reqCtls, newInstanceCtls;
        public int reconnectCalls;
        public ExtendedRequest lastExtReq;
        public ExtendedResponse extResp;
        public ExtendedResponse extendedOperation(ExtendedRequest request) {
            lastExtReq = request;
            log.add("ext:" + request.getID());
            extResp = new DeepExtResp(request.getID(), request.getEncodedValue());
            return extResp;
        }
        public LdapContext newInstance(Control[] requestControls) {
            newInstanceCtls = requestControls;
            return new RecLdapCtx();
        }
        public void reconnect(Control[] connCtls) {
            reconnectCalls++;
            this.connCtls = connCtls;
        }
        public Control[] getConnectControls() { return connCtls; }
        public void setRequestControls(Control[] requestControls) {
            log.add("setReq:" + (requestControls == null ? -1 : requestControls.length));
            this.reqCtls = requestControls;
        }
        public Control[] getRequestControls() { return reqCtls; }
        public Control[] getResponseControls() { return connCtls == null ? new Control[0] : connCtls; }
        // --- DirContext surface (minimal in-memory) ---
        public javax.naming.directory.Attributes getAttributes(String n) { return getAttributes(n, new String[0]); }
        public javax.naming.directory.Attributes getAttributes(Name n) { return getAttributes(n.toString()); }
        public javax.naming.directory.Attributes getAttributes(String n, String[] ids) { return new javax.naming.directory.BasicAttributes(); }
        public javax.naming.directory.Attributes getAttributes(Name n, String[] ids) { return getAttributes(n.toString(), ids); }
        public void bind(String n, Object o, javax.naming.directory.Attributes a) { }
        public void bind(Name n, Object o, javax.naming.directory.Attributes a) { }
        public void rebind(String n, Object o, javax.naming.directory.Attributes a) { }
        public void rebind(Name n, Object o, javax.naming.directory.Attributes a) { }
        public void modifyAttributes(String n, int mod, javax.naming.directory.Attributes a) { }
        public void modifyAttributes(Name n, int mod, javax.naming.directory.Attributes a) { }
        public void modifyAttributes(String n, javax.naming.directory.ModificationItem[] ms) { }
        public void modifyAttributes(Name n, javax.naming.directory.ModificationItem[] ms) { }
        public NamingEnumeration<javax.naming.directory.SearchResult> search(String n, javax.naming.directory.Attributes m) { return null; }
        public NamingEnumeration<javax.naming.directory.SearchResult> search(Name n, javax.naming.directory.Attributes m) { return null; }
        public NamingEnumeration<javax.naming.directory.SearchResult> search(String n, javax.naming.directory.Attributes m, String[] ids) { return null; }
        public NamingEnumeration<javax.naming.directory.SearchResult> search(Name n, javax.naming.directory.Attributes m, String[] ids) { return null; }
        public NamingEnumeration<javax.naming.directory.SearchResult> search(String n, String f, javax.naming.directory.SearchControls c) { return null; }
        public NamingEnumeration<javax.naming.directory.SearchResult> search(Name n, String f, javax.naming.directory.SearchControls c) { return null; }
        public NamingEnumeration<javax.naming.directory.SearchResult> search(String n, String f, Object[] a, javax.naming.directory.SearchControls c) { return null; }
        public NamingEnumeration<javax.naming.directory.SearchResult> search(Name n, String f, Object[] a, javax.naming.directory.SearchControls c) { return null; }
        public javax.naming.directory.DirContext getSchema(String n) { return null; }
        public javax.naming.directory.DirContext getSchema(Name n) { return null; }
        public javax.naming.directory.DirContext getSchemaClassDefinition(String n) { return null; }
        public javax.naming.directory.DirContext getSchemaClassDefinition(Name n) { return null; }
        public String getSchemaName(String n) { return ""; }
        public String getSchemaName(Name n) { return ""; }
        public javax.naming.directory.DirContext createSubcontext(String n, javax.naming.directory.Attributes a) { return null; }
        public javax.naming.directory.DirContext createSubcontext(Name n, javax.naming.directory.Attributes a) { return null; }
    }

    public static class DeepExtResp implements ExtendedResponse {
        final String id; final byte[] val;
        DeepExtResp(String id, byte[] val) { this.id = id; this.val = val; }
        public String getID() { return id; }
        public byte[] getEncodedValue() { return val; }
    }

    static void dLcControlLifecycle() throws Exception {
        RecLdapCtx c = new RecLdapCtx();
        Control[] first = {new BasicControl("1.1", true, null)};
        c.setRequestControls(first);
        check(c.getRequestControls() == first, "request controls stored by reference");
        check(c.log.get(0).equals("setReq:1"), "set logged");
        c.setRequestControls(null);
        check(c.getRequestControls() == null, "null clears request controls");
        check(c.log.get(1).equals("setReq:-1"), "null logged");
        c.reconnect(first);
        check(c.reconnectCalls == 1, "reconnect counted");
        check(c.getConnectControls() == first, "reconnect installs connect controls");
        check(c.getResponseControls().length == 1, "response controls reflect last op");
        LdapContext copy = c.newInstance(first);
        check(copy != c && c.newInstanceCtls == first, "newInstance receives controls");
        check(copy instanceof LdapContext, "newInstance returns LdapContext");
    }

    static void dLcExtendedOperation() throws Exception {
        RecLdapCtx c = new RecLdapCtx();
        ExtendedRequest req = new javax.naming.ldap.ExtendedRequest() {
            public String getID() { return "1.2.3.4"; }
            public byte[] getEncodedValue() { return new byte[]{9}; }
            public ExtendedResponse createExtendedResponse(String id, byte[] berValue,
                    int offset, int length) { return new DeepExtResp(id, berValue); }
        };
        ExtendedResponse resp = c.extendedOperation(req);
        check(c.lastExtReq == req, "request forwarded verbatim");
        check("1.2.3.4".equals(resp.getID()), "response id echoes request");
        check(resp.getEncodedValue()[0] == 9, "response value echoes request payload");
        check(c.log.get(0).equals("ext:1.2.3.4"), "extended op logged");
    }

    /** Minimal Context base so RecLdapCtx only adds LDAP specifics. */
    public static class DeepPlainCtx implements Context {
        public Object lookup(String n) { return null; }
        public Object lookup(Name n) { return null; }
        public void bind(String n, Object o) { }
        public void bind(Name n, Object o) { }
        public void rebind(String n, Object o) { }
        public void rebind(Name n, Object o) { }
        public void unbind(String n) { }
        public void unbind(Name n) { }
        public void rename(String o, String n) { }
        public void rename(Name o, Name n) { }
        public NamingEnumeration<NameClassPair> list(String n) { return null; }
        public NamingEnumeration<NameClassPair> list(Name n) { return null; }
        public NamingEnumeration<Binding> listBindings(String n) { return null; }
        public NamingEnumeration<Binding> listBindings(Name n) { return null; }
        public void destroySubcontext(String n) { }
        public void destroySubcontext(Name n) { }
        public Context createSubcontext(String n) { return null; }
        public Context createSubcontext(Name n) { return null; }
        public Object lookupLink(String n) { return null; }
        public Object lookupLink(Name n) { return null; }
        public String getNameInNamespace() { return ""; }
        public NameParser getNameParser(String n) { return null; }
        public NameParser getNameParser(Name n) { return null; }
        public String composeName(String n, String p) { return null; }
        public Name composeName(Name n, Name p) { return null; }
        public Object addToEnvironment(String k, Object v) { return null; }
        public Object removeFromEnvironment(String k) { return null; }
        public Hashtable<?, ?> getEnvironment() { return new Hashtable<Object, Object>(); }
        public void close() { }
    }

}
