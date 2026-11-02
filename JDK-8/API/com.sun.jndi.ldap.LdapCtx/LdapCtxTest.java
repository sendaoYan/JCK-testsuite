/*
 * @test
 * @summary com.sun.jndi.ldap.LdapCtx conformance: hierarchy and declared members via reflection.
 * @run main LdapCtxTest
 */

public class LdapCtxTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.ldap.LdapCtx", false, null);
        check(c.getName().equals("com.sun.jndi.ldap.LdapCtx"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.ctx.ComponentDirContext"), "superclass com.sun.jndi.toolkit.ctx.ComponentDirContext");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.naming.event.EventDirContext"), "implements javax.naming.event.EventDirContext");
        check(in.contains("javax.naming.ldap.LdapContext"), "implements javax.naming.ldap.LdapContext");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addControl"), "declares addControl");
        check(ms.contains("addNamingListener"), "declares addNamingListener");
        check(ms.contains("addRdnAttributes"), "declares addRdnAttributes");
        check(ms.contains("addToEnvironment"), "declares addToEnvironment");
        check(ms.contains("addUnsolicited"), "declares addUnsolicited");
        check(ms.contains("adjustDeleteStatus"), "declares adjustDeleteStatus");
        check(ms.contains("appendVector"), "declares appendVector");
        check(ms.contains("buildSchemaTree"), "declares buildSchemaTree");
        check(ms.contains("c_bind"), "declares c_bind");
        check(ms.contains("c_createSubcontext"), "declares c_createSubcontext");
        check(ms.contains("c_destroySubcontext"), "declares c_destroySubcontext");
        check(ms.contains("c_getAttributes"), "declares c_getAttributes");
        check(ms.contains("c_getNameParser"), "declares c_getNameParser");
        check(ms.contains("c_getSchema"), "declares c_getSchema");
        check(ms.contains("c_getSchemaClassDefinition"), "declares c_getSchemaClassDefinition");
        check(ms.contains("c_list"), "declares c_list");
        check(ms.contains("c_listBindings"), "declares c_listBindings");
        check(ms.contains("c_lookup"), "declares c_lookup");
        check(ms.contains("c_lookupLink"), "declares c_lookupLink");
        check(ms.contains("c_modifyAttributes"), "declares c_modifyAttributes");
        check(ms.contains("c_rebind"), "declares c_rebind");
        check(ms.contains("c_rename"), "declares c_rename");
        check(ms.contains("c_search"), "declares c_search");
        check(ms.contains("c_unbind"), "declares c_unbind");
        check(ms.contains("cloneControls"), "declares cloneControls");
        check(ms.contains("cloneSearchControls"), "declares cloneSearchControls");
        check(ms.contains("close"), "declares close");
        check(ms.contains("closeConnection"), "declares closeConnection");
        check(ms.contains("compare"), "declares compare");
        check(ms.contains("composeName"), "declares composeName");
        check(ms.contains("concatNames"), "declares concatNames");
        check(ms.contains("connect"), "declares connect");
        check(ms.contains("containsIgnoreCase"), "declares containsIgnoreCase");
        check(ms.contains("convertControls"), "declares convertControls");
        check(ms.contains("convertToLdapModCode"), "declares convertToLdapModCode");
        check(ms.contains("decEnumCount"), "declares decEnumCount");
        check(ms.contains("doSearch"), "declares doSearch");
        check(ms.contains("doSearchOnce"), "declares doSearchOnce");
        check(ms.contains("ensureCanTransmitCredentials"), "declares ensureCanTransmitCredentials");
        check(ms.contains("ensureOpen"), "declares ensureOpen");
        check(ms.contains("extendedOperation"), "declares extendedOperation");
        check(ms.contains("extractURLs"), "declares extractURLs");
        check(ms.contains("filterToAssertion"), "declares filterToAssertion");
        check(ms.contains("finalize"), "declares finalize");
        check(ms.contains("findControl"), "declares findControl");
        check(ms.contains("fireUnsolicited"), "declares fireUnsolicited");
        check(ms.contains("fullyQualifiedName"), "declares fullyQualifiedName");
        check(ms.contains("getConnectControls"), "declares getConnectControls");
        check(ms.contains("getEnvironment"), "declares getEnvironment");
        check(ms.contains("getMechsAllowedToSendCredentials"), "declares getMechsAllowedToSendCredentials");
        check(ms.contains("getMechsFromPropertyValue"), "declares getMechsFromPropertyValue");
        check(ms.contains("getNameInNamespace"), "declares getNameInNamespace");
        check(ms.contains("getRequestControls"), "declares getRequestControls");
        check(ms.contains("getResponseControls"), "declares getResponseControls");
        check(ms.contains("getSchemaEntry"), "declares getSchemaEntry");
        check(ms.contains("getSchemaTree"), "declares getSchemaTree");
        check(ms.contains("getSearchReply"), "declares getSearchReply");
        check(ms.contains("getTargetName"), "declares getTargetName");
        check(ms.contains("getURL"), "declares getURL");
        check(ms.contains("incEnumCount"), "declares incEnumCount");
        check(ms.contains("initEnv"), "declares initEnv");
        check(ms.contains("isConnectionEncrypted"), "declares isConnectionEncrypted");
        check(ms.contains("mapErrorCode"), "declares mapErrorCode");
        check(ms.contains("newInstance"), "declares newInstance");
        check(ms.contains("p_getEnvironment"), "declares p_getEnvironment");
        check(ms.contains("processReturnCode"), "declares processReturnCode");
        check(ms.contains("reconnect"), "declares reconnect");
        check(ms.contains("removeControl"), "declares removeControl");
        check(ms.contains("removeFromEnvironment"), "declares removeFromEnvironment");
        check(ms.contains("removeNamingListener"), "declares removeNamingListener");
        check(ms.contains("removeUnsolicited"), "declares removeUnsolicited");
        check(ms.contains("searchAux"), "declares searchAux");
        check(ms.contains("searchToCompare"), "declares searchToCompare");
        check(ms.contains("setBatchSize"), "declares setBatchSize");
        check(ms.contains("setBinaryAttributes"), "declares setBinaryAttributes");
        check(ms.contains("setConnectTimeout"), "declares setConnectTimeout");
        check(ms.contains("setDeleteRDN"), "declares setDeleteRDN");
        check(ms.contains("setDerefAliases"), "declares setDerefAliases");
        check(ms.contains("setDomainName"), "declares setDomainName");
        check(ms.contains("setHopCount"), "declares setHopCount");
        check(ms.contains("setParents"), "declares setParents");
        check(ms.contains("setProviderUrl"), "declares setProviderUrl");
        check(ms.contains("setReadTimeout"), "declares setReadTimeout");
        check(ms.contains("setRefSeparator"), "declares setRefSeparator");
        check(ms.contains("setReferralLimit"), "declares setReferralLimit");
        check(ms.contains("setReferralMode"), "declares setReferralMode");
        check(ms.contains("setReplyQueueSize"), "declares setReplyQueueSize");
        check(ms.contains("setRequestControls"), "declares setRequestControls");
        check(ms.contains("setTypesOnly"), "declares setTypesOnly");
        check(ms.contains("setWaitForReply"), "declares setWaitForReply");
        check(ms.contains("targetMustExist"), "declares targetMustExist");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
