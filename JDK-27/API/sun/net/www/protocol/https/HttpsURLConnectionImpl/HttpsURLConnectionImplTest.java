/*
 * @test
 * @summary sun.net.www.protocol.https.HttpsURLConnectionImpl conformance: internal package reflection.
 * @modules java.base/sun.net.www.protocol.https:+open
 * @run main HttpsURLConnectionImplTest
 */
public class HttpsURLConnectionImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.https.HttpsURLConnectionImpl", false, HttpsURLConnectionImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.net.ssl.HttpsURLConnection"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "addRequestProperty"), "method addRequestProperty");
        check(hasMethod(c, "connect"), "method connect");
        check(hasMethod(c, "disconnect"), "method disconnect");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getAllowUserInteraction"), "method getAllowUserInteraction");
        check(hasMethod(c, "getCipherSuite"), "method getCipherSuite");
        check(hasMethod(c, "getConnectTimeout"), "method getConnectTimeout");
        check(hasMethod(c, "getContent"), "method getContent");
        check(hasMethod(c, "getContentEncoding"), "method getContentEncoding");
        check(hasMethod(c, "getContentLength"), "method getContentLength");
        check(hasMethod(c, "getContentLengthLong"), "method getContentLengthLong");
        check(hasMethod(c, "getContentType"), "method getContentType");
        check(hasMethod(c, "getDate"), "method getDate");
        check(hasMethod(c, "getDefaultUseCaches"), "method getDefaultUseCaches");
        check(hasMethod(c, "getDoInput"), "method getDoInput");
        check(hasMethod(c, "getDoOutput"), "method getDoOutput");
        check(hasMethod(c, "getErrorStream"), "method getErrorStream");
        check(hasMethod(c, "getExpiration"), "method getExpiration");
        check(hasMethod(c, "getHeaderField"), "method getHeaderField");
        check(hasMethod(c, "getHeaderFieldDate"), "method getHeaderFieldDate");
        check(hasMethod(c, "getHeaderFieldInt"), "method getHeaderFieldInt");
        check(hasMethod(c, "getHeaderFieldKey"), "method getHeaderFieldKey");
        check(hasMethod(c, "getHeaderFieldLong"), "method getHeaderFieldLong");
        check(hasMethod(c, "getHeaderFields"), "method getHeaderFields");
        check(hasMethod(c, "getIfModifiedSince"), "method getIfModifiedSince");
        check(hasMethod(c, "getInputStream"), "method getInputStream");
        check(hasMethod(c, "getInstanceFollowRedirects"), "method getInstanceFollowRedirects");
        check(hasMethod(c, "getLastModified"), "method getLastModified");
        check(hasMethod(c, "getLocalCertificates"), "method getLocalCertificates");
        check(hasMethod(c, "getLocalPrincipal"), "method getLocalPrincipal");
        check(hasMethod(c, "getOutputStream"), "method getOutputStream");
        check(hasMethod(c, "getPeerPrincipal"), "method getPeerPrincipal");
        check(hasMethod(c, "getPermission"), "method getPermission");
        check(hasMethod(c, "getReadTimeout"), "method getReadTimeout");
        check(hasMethod(c, "getRequestMethod"), "method getRequestMethod");
        check(hasMethod(c, "getRequestProperties"), "method getRequestProperties");
        check(hasMethod(c, "getRequestProperty"), "method getRequestProperty");
        check(hasMethod(c, "getResponseCode"), "method getResponseCode");
        check(hasMethod(c, "getResponseMessage"), "method getResponseMessage");
        check(hasMethod(c, "getSSLSession"), "method getSSLSession");
        check(hasMethod(c, "getServerCertificates"), "method getServerCertificates");
        check(hasMethod(c, "getURL"), "method getURL");
        check(hasMethod(c, "getUseCaches"), "method getUseCaches");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "isConnected"), "method isConnected");
        check(hasMethod(c, "setAllowUserInteraction"), "method setAllowUserInteraction");
        check(hasMethod(c, "setAuthenticator"), "method setAuthenticator");
        check(hasMethod(c, "setChunkedStreamingMode"), "method setChunkedStreamingMode");
        check(hasMethod(c, "setConnectTimeout"), "method setConnectTimeout");
        check(hasMethod(c, "setConnected"), "method setConnected");
        check(hasMethod(c, "setDefaultUseCaches"), "method setDefaultUseCaches");
        check(hasMethod(c, "setDoInput"), "method setDoInput");
        check(hasMethod(c, "setDoOutput"), "method setDoOutput");
        check(hasMethod(c, "setFixedLengthStreamingMode"), "method setFixedLengthStreamingMode");
        check(hasMethod(c, "setIfModifiedSince"), "method setIfModifiedSince");
        check(hasMethod(c, "setInstanceFollowRedirects"), "method setInstanceFollowRedirects");
        check(hasMethod(c, "setNewClient"), "method setNewClient");
        check(hasMethod(c, "setProxiedClient"), "method setProxiedClient");
        check(hasMethod(c, "setReadTimeout"), "method setReadTimeout");
        check(hasMethod(c, "setRequestMethod"), "method setRequestMethod");
        check(hasMethod(c, "setRequestProperty"), "method setRequestProperty");
        check(hasMethod(c, "setUseCaches"), "method setUseCaches");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "usingProxy"), "method usingProxy");
        check(hasField(c, "delegate"), "field delegate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
