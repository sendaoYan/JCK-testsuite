/*
 * @test
 * @summary sun.net.www.http.HttpClient conformance: internal package reflection.
 * @modules java.base/sun.net.www.http:+open
 * @run main HttpClientTest
 */
public class HttpClientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.http.HttpClient", false, HttpClientTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.NetworkClient"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "New"), "method New");
        check(hasMethod(c, "afterConnect"), "method afterConnect");
        check(hasMethod(c, "available"), "method available");
        check(hasMethod(c, "closeIdleConnection"), "method closeIdleConnection");
        check(hasMethod(c, "closeServer"), "method closeServer");
        check(hasMethod(c, "finished"), "method finished");
        check(hasMethod(c, "getAuthCache"), "method getAuthCache");
        check(hasMethod(c, "getDefaultPort"), "method getDefaultPort");
        check(hasMethod(c, "getHttpKeepAliveSet"), "method getHttpKeepAliveSet");
        check(hasMethod(c, "getHttpProxy"), "method getHttpProxy");
        check(hasMethod(c, "getInputStream"), "method getInputStream");
        check(hasMethod(c, "getOutputStream"), "method getOutputStream");
        check(hasMethod(c, "getProxyHostUsed"), "method getProxyHostUsed");
        check(hasMethod(c, "getProxyPortUsed"), "method getProxyPortUsed");
        check(hasMethod(c, "getSpnegoCBT"), "method getSpnegoCBT");
        check(hasMethod(c, "getURLFile"), "method getURLFile");
        check(hasMethod(c, "getUsingProxy"), "method getUsingProxy");
        check(hasMethod(c, "isCachedConnection"), "method isCachedConnection");
        check(hasMethod(c, "isInKeepAliveCache"), "method isInKeepAliveCache");
        check(hasMethod(c, "isKeepingAlive"), "method isKeepingAlive");
        check(hasMethod(c, "lock"), "method lock");
        check(hasMethod(c, "needsTunneling"), "method needsTunneling");
        check(hasMethod(c, "newHttpProxy"), "method newHttpProxy");
        check(hasMethod(c, "openServer"), "method openServer");
        check(hasMethod(c, "parseHTTP"), "method parseHTTP");
        check(hasMethod(c, "putInKeepAliveCache"), "method putInKeepAliveCache");
        check(hasMethod(c, "resetProperties"), "method resetProperties");
        check(hasMethod(c, "setCacheRequest"), "method setCacheRequest");
        check(hasMethod(c, "setDoNotRetry"), "method setDoNotRetry");
        check(hasMethod(c, "setIgnoreContinue"), "method setIgnoreContinue");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "unlock"), "method unlock");
        check(hasMethod(c, "writeRequests"), "method writeRequests");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "HTTP_CONTINUE"), "field HTTP_CONTINUE");
        check(hasField(c, "authcache"), "field authcache");
        check(hasField(c, "cacheNTLMProp"), "field cacheNTLMProp");
        check(hasField(c, "cacheRequest"), "field cacheRequest");
        check(hasField(c, "cacheSPNEGOProp"), "field cacheSPNEGOProp");
        check(hasField(c, "cachedHttpClient"), "field cachedHttpClient");
        check(hasField(c, "capture"), "field capture");
        check(hasField(c, "clientLock"), "field clientLock");
        check(hasField(c, "disableKeepAlive"), "field disableKeepAlive");
        check(hasField(c, "failedOnce"), "field failedOnce");
        check(hasField(c, "host"), "field host");
        check(hasField(c, "httpPortNumber"), "field httpPortNumber");
        check(hasField(c, "ignoreContinue"), "field ignoreContinue");
        check(hasField(c, "inCache"), "field inCache");
        check(hasField(c, "kac"), "field kac");
        check(hasField(c, "keepAliveConnections"), "field keepAliveConnections");
        check(hasField(c, "keepAliveProp"), "field keepAliveProp");
        check(hasField(c, "keepAliveTimeout"), "field keepAliveTimeout");
        check(hasField(c, "keepingAlive"), "field keepingAlive");
        check(hasField(c, "logger"), "field logger");
        check(hasField(c, "port"), "field port");
        check(hasField(c, "poster"), "field poster");
        check(hasField(c, "proxyDisabled"), "field proxyDisabled");
        check(hasField(c, "requests"), "field requests");
        check(hasField(c, "retryPostProp"), "field retryPostProp");
        check(hasField(c, "reuse"), "field reuse");
        check(hasField(c, "spnegoCBT"), "field spnegoCBT");
        check(hasField(c, "streaming"), "field streaming");
        check(hasField(c, "url"), "field url");
        check(hasField(c, "usingProxy"), "field usingProxy");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
