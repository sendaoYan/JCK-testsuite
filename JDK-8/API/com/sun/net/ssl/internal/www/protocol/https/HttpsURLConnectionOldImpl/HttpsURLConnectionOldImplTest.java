/*
 * @test
 * @summary com.sun.net.ssl.internal.www.protocol.https.HttpsURLConnectionOldImpl conformance: hierarchy and declared members via reflection.
 * @run main HttpsURLConnectionOldImplTest
 */

public class HttpsURLConnectionOldImplTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.net.ssl.internal.www.protocol.https.HttpsURLConnectionOldImpl", false, null);
        check(c.getName().equals("com.sun.net.ssl.internal.www.protocol.https.HttpsURLConnectionOldImpl"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.net.ssl.HttpsURLConnection"), "superclass com.sun.net.ssl.HttpsURLConnection");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addRequestProperty"), "declares addRequestProperty");
        check(ms.contains("checkURL"), "declares checkURL");
        check(ms.contains("connect"), "declares connect");
        check(ms.contains("disconnect"), "declares disconnect");
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("finalize"), "declares finalize");
        check(ms.contains("getAllowUserInteraction"), "declares getAllowUserInteraction");
        check(ms.contains("getCipherSuite"), "declares getCipherSuite");
        check(ms.contains("getConnectTimeout"), "declares getConnectTimeout");
        check(ms.contains("getContent"), "declares getContent");
        check(ms.contains("getContentEncoding"), "declares getContentEncoding");
        check(ms.contains("getContentLength"), "declares getContentLength");
        check(ms.contains("getContentLengthLong"), "declares getContentLengthLong");
        check(ms.contains("getContentType"), "declares getContentType");
        check(ms.contains("getDate"), "declares getDate");
        check(ms.contains("getDefaultUseCaches"), "declares getDefaultUseCaches");
        check(ms.contains("getDoInput"), "declares getDoInput");
        check(ms.contains("getDoOutput"), "declares getDoOutput");
        check(ms.contains("getErrorStream"), "declares getErrorStream");
        check(ms.contains("getExpiration"), "declares getExpiration");
        check(ms.contains("getHeaderField"), "declares getHeaderField");
        check(ms.contains("getHeaderFieldDate"), "declares getHeaderFieldDate");
        check(ms.contains("getHeaderFieldInt"), "declares getHeaderFieldInt");
        check(ms.contains("getHeaderFieldKey"), "declares getHeaderFieldKey");
        check(ms.contains("getHeaderFieldLong"), "declares getHeaderFieldLong");
        check(ms.contains("getHeaderFields"), "declares getHeaderFields");
        check(ms.contains("getIfModifiedSince"), "declares getIfModifiedSince");
        check(ms.contains("getInputStream"), "declares getInputStream");
        check(ms.contains("getInstanceFollowRedirects"), "declares getInstanceFollowRedirects");
        check(ms.contains("getLastModified"), "declares getLastModified");
        check(ms.contains("getLocalCertificates"), "declares getLocalCertificates");
        check(ms.contains("getOutputStream"), "declares getOutputStream");
        check(ms.contains("getPermission"), "declares getPermission");
        check(ms.contains("getReadTimeout"), "declares getReadTimeout");
        check(ms.contains("getRequestMethod"), "declares getRequestMethod");
        check(ms.contains("getRequestProperties"), "declares getRequestProperties");
        check(ms.contains("getRequestProperty"), "declares getRequestProperty");
        check(ms.contains("getResponseCode"), "declares getResponseCode");
        check(ms.contains("getResponseMessage"), "declares getResponseMessage");
        check(ms.contains("getServerCertificateChain"), "declares getServerCertificateChain");
        check(ms.contains("getServerCertificates"), "declares getServerCertificates");
        check(ms.contains("getURL"), "declares getURL");
        check(ms.contains("getUseCaches"), "declares getUseCaches");
        check(ms.contains("hashCode"), "declares hashCode");
        check(ms.contains("isConnected"), "declares isConnected");
        check(ms.contains("setAllowUserInteraction"), "declares setAllowUserInteraction");
        check(ms.contains("setChunkedStreamingMode"), "declares setChunkedStreamingMode");
        check(ms.contains("setConnectTimeout"), "declares setConnectTimeout");
        check(ms.contains("setConnected"), "declares setConnected");
        check(ms.contains("setDefaultUseCaches"), "declares setDefaultUseCaches");
        check(ms.contains("setDoInput"), "declares setDoInput");
        check(ms.contains("setDoOutput"), "declares setDoOutput");
        check(ms.contains("setFixedLengthStreamingMode"), "declares setFixedLengthStreamingMode");
        check(ms.contains("setIfModifiedSince"), "declares setIfModifiedSince");
        check(ms.contains("setInstanceFollowRedirects"), "declares setInstanceFollowRedirects");
        check(ms.contains("setNewClient"), "declares setNewClient");
        check(ms.contains("setProxiedClient"), "declares setProxiedClient");
        check(ms.contains("setReadTimeout"), "declares setReadTimeout");
        check(ms.contains("setRequestMethod"), "declares setRequestMethod");
        check(ms.contains("setRequestProperty"), "declares setRequestProperty");
        check(ms.contains("setUseCaches"), "declares setUseCaches");
        check(ms.contains("toString"), "declares toString");
        check(ms.contains("usingProxy"), "declares usingProxy");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
