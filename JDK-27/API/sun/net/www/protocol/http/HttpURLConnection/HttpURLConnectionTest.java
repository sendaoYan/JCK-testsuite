/*
 * @test
 * @summary sun.net.www.protocol.http.HttpURLConnection conformance: internal package reflection.
 * @modules java.base/sun.net.www.protocol.http:+open
 * @run main HttpURLConnectionTest
 */
public class HttpURLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.HttpURLConnection", false, HttpURLConnectionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.net.HttpURLConnection"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "addRequestProperty"), "method addRequestProperty");
        check(hasMethod(c, "authObj"), "method authObj");
        check(hasMethod(c, "connect"), "method connect");
        check(hasMethod(c, "disconnect"), "method disconnect");
        check(hasMethod(c, "doTunneling"), "method doTunneling");
        check(hasMethod(c, "getAuthCache"), "method getAuthCache");
        check(hasMethod(c, "getConnectTimeout"), "method getConnectTimeout");
        check(hasMethod(c, "getCookieHandler"), "method getCookieHandler");
        check(hasMethod(c, "getErrorStream"), "method getErrorStream");
        check(hasMethod(c, "getHeaderField"), "method getHeaderField");
        check(hasMethod(c, "getHeaderFieldKey"), "method getHeaderFieldKey");
        check(hasMethod(c, "getHeaderFields"), "method getHeaderFields");
        check(hasMethod(c, "getHttpCallerInfo"), "method getHttpCallerInfo");
        check(hasMethod(c, "getHttpLogger"), "method getHttpLogger");
        check(hasMethod(c, "getInputStream"), "method getInputStream");
        check(hasMethod(c, "getNewHttpClient"), "method getNewHttpClient");
        check(hasMethod(c, "getOutputStream"), "method getOutputStream");
        check(hasMethod(c, "getReadTimeout"), "method getReadTimeout");
        check(hasMethod(c, "getRequestProperties"), "method getRequestProperties");
        check(hasMethod(c, "getRequestProperty"), "method getRequestProperty");
        check(hasMethod(c, "isLockHeldByCurrentThread"), "method isLockHeldByCurrentThread");
        check(hasMethod(c, "noResponseBody"), "method noResponseBody");
        check(hasMethod(c, "plainConnect"), "method plainConnect");
        check(hasMethod(c, "plainConnect0"), "method plainConnect0");
        check(hasMethod(c, "proxiedConnect"), "method proxiedConnect");
        check(hasMethod(c, "setAuthenticationProperty"), "method setAuthenticationProperty");
        check(hasMethod(c, "setAuthenticator"), "method setAuthenticator");
        check(hasMethod(c, "setConnectTimeout"), "method setConnectTimeout");
        check(hasMethod(c, "setNewClient"), "method setNewClient");
        check(hasMethod(c, "setProxiedClient"), "method setProxiedClient");
        check(hasMethod(c, "setReadTimeout"), "method setReadTimeout");
        check(hasMethod(c, "setRequestMethod"), "method setRequestMethod");
        check(hasMethod(c, "setRequestProperty"), "method setRequestProperty");
        check(hasMethod(c, "setTunnelState"), "method setTunnelState");
        check(hasMethod(c, "streaming"), "method streaming");
        check(hasMethod(c, "usingProxy"), "method usingProxy");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "EXCLUDE_HEADERS"), "field EXCLUDE_HEADERS");
        check(hasField(c, "EXCLUDE_HEADERS2"), "field EXCLUDE_HEADERS2");
        check(hasField(c, "HTTP_CONNECT"), "field HTTP_CONNECT");
        check(hasField(c, "RETRY_MSG1"), "field RETRY_MSG1");
        check(hasField(c, "RETRY_MSG2"), "field RETRY_MSG2");
        check(hasField(c, "RETRY_MSG3"), "field RETRY_MSG3");
        check(hasField(c, "SET_COOKIE"), "field SET_COOKIE");
        check(hasField(c, "SET_COOKIE2"), "field SET_COOKIE2");
        check(hasField(c, "acceptString"), "field acceptString");
        check(hasField(c, "allowRestrictedHeaders"), "field allowRestrictedHeaders");
        check(hasField(c, "authCache"), "field authCache");
        check(hasField(c, "authObj"), "field authObj");
        check(hasField(c, "authenticator"), "field authenticator");
        check(hasField(c, "bufSize4ES"), "field bufSize4ES");
        check(hasField(c, "cacheHandler"), "field cacheHandler");
        check(hasField(c, "cachedHeaders"), "field cachedHeaders");
        check(hasField(c, "cachedInputStream"), "field cachedInputStream");
        check(hasField(c, "cachedResponse"), "field cachedResponse");
        check(hasField(c, "cdata"), "field cdata");
        check(hasField(c, "connectTimeout"), "field connectTimeout");
        check(hasField(c, "connecting"), "field connecting");
        check(hasField(c, "connectionLock"), "field connectionLock");
        check(hasField(c, "cookieHandler"), "field cookieHandler");
        check(hasField(c, "currentProxyCredentials"), "field currentProxyCredentials");
        check(hasField(c, "currentServerCredentials"), "field currentServerCredentials");
        check(hasField(c, "defaultmaxRedirects"), "field defaultmaxRedirects");
        check(hasField(c, "digestparams"), "field digestparams");
        check(hasField(c, "disabledProxyingSchemes"), "field disabledProxyingSchemes");
        check(hasField(c, "disabledTunnelingSchemes"), "field disabledTunnelingSchemes");
        check(hasField(c, "doingNTLM2ndStage"), "field doingNTLM2ndStage");
        check(hasField(c, "doingNTLMp2ndStage"), "field doingNTLMp2ndStage");
        check(hasField(c, "domain"), "field domain");
        check(hasField(c, "enableESBuffer"), "field enableESBuffer");
        check(hasField(c, "errorStream"), "field errorStream");
        check(hasField(c, "failedOnce"), "field failedOnce");
        check(hasField(c, "filteredHeaders"), "field filteredHeaders");
        check(hasField(c, "handler"), "field handler");
        check(hasField(c, "http"), "field http");
        check(hasField(c, "httpVersion"), "field httpVersion");
        check(hasField(c, "inputStream"), "field inputStream");
        check(hasField(c, "instProxy"), "field instProxy");
        check(hasField(c, "isUserProxyAuth"), "field isUserProxyAuth");
        check(hasField(c, "isUserServerAuth"), "field isUserServerAuth");
        check(hasField(c, "lastProxy"), "field lastProxy");
        check(hasField(c, "logger"), "field logger");
        check(hasField(c, "maxHeaderSize"), "field maxHeaderSize");
        check(hasField(c, "maxRedirects"), "field maxRedirects");
        check(hasField(c, "needToCheck"), "field needToCheck");
        check(hasField(c, "poster"), "field poster");
        check(hasField(c, "proxyAuthKey"), "field proxyAuthKey");
        check(hasField(c, "ps"), "field ps");
        check(hasField(c, "readTimeout"), "field readTimeout");
        check(hasField(c, "rememberedException"), "field rememberedException");
        check(hasField(c, "rememberedExceptionOut"), "field rememberedExceptionOut");
        check(hasField(c, "requestURI"), "field requestURI");
        check(hasField(c, "requests"), "field requests");
        check(hasField(c, "responses"), "field responses");
        check(hasField(c, "restrictedHeaderSet"), "field restrictedHeaderSet");
        check(hasField(c, "restrictedHeaders"), "field restrictedHeaders");
        check(hasField(c, "reuseClient"), "field reuseClient");
        check(hasField(c, "serverAuthKey"), "field serverAuthKey");
        check(hasField(c, "setRequests"), "field setRequests");
        check(hasField(c, "setUserCookies"), "field setUserCookies");
        check(hasField(c, "strOutputStream"), "field strOutputStream");
        check(hasField(c, "timeout4ESBuffer"), "field timeout4ESBuffer");
        check(hasField(c, "tryTransparentNTLMProxy"), "field tryTransparentNTLMProxy");
        check(hasField(c, "tryTransparentNTLMServer"), "field tryTransparentNTLMServer");
        check(hasField(c, "tunnelState"), "field tunnelState");
        check(hasField(c, "useProxyResponseCode"), "field useProxyResponseCode");
        check(hasField(c, "userAgent"), "field userAgent");
        check(hasField(c, "userCookies"), "field userCookies");
        check(hasField(c, "userCookies2"), "field userCookies2");
        check(hasField(c, "userHeaders"), "field userHeaders");
        check(hasField(c, "usingProxy"), "field usingProxy");
        check(hasField(c, "validateProxy"), "field validateProxy");
        check(hasField(c, "validateServer"), "field validateServer");
        check(hasField(c, "version"), "field version");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
