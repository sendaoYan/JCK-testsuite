/*
 * @test
 * @summary java.net.URLConnection conformance: API type verification.
 * @run main URLConnectionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class URLConnectionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
static boolean hasMethod(Class<?> c, String n) {
    for (java.lang.reflect.Method m : c.getMethods()) {
        if (m.getName().equals(n)) return true;
    }
    return false;
}

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.URLConnection");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
check(hasMethod(c, "getFileNameMap"), "getFileNameMap present");
check(hasMethod(c, "setFileNameMap"), "setFileNameMap present");
check(hasMethod(c, "connect"), "connect present");
check(hasMethod(c, "setConnectTimeout"), "setConnectTimeout present");
check(hasMethod(c, "getConnectTimeout"), "getConnectTimeout present");
check(hasMethod(c, "setReadTimeout"), "setReadTimeout present");
check(hasMethod(c, "getReadTimeout"), "getReadTimeout present");
check(hasMethod(c, "getURL"), "getURL present");
check(hasMethod(c, "getContentLength"), "getContentLength present");
check(hasMethod(c, "getContentLengthLong"), "getContentLengthLong present");
check(hasMethod(c, "getContentType"), "getContentType present");
check(hasMethod(c, "getContentEncoding"), "getContentEncoding present");
check(hasMethod(c, "getExpiration"), "getExpiration present");
check(hasMethod(c, "getDate"), "getDate present");
check(hasMethod(c, "getLastModified"), "getLastModified present");
check(hasMethod(c, "getHeaderField"), "getHeaderField present");
check(hasMethod(c, "getHeaderFields"), "getHeaderFields present");
check(hasMethod(c, "getHeaderFieldInt"), "getHeaderFieldInt present");
check(hasMethod(c, "getHeaderFieldLong"), "getHeaderFieldLong present");
check(hasMethod(c, "getHeaderFieldDate"), "getHeaderFieldDate present");
check(hasMethod(c, "getHeaderFieldKey"), "getHeaderFieldKey present");
check(hasMethod(c, "getContent"), "getContent present");
check(hasMethod(c, "getPermission"), "getPermission present");
check(hasMethod(c, "getInputStream"), "getInputStream present");
check(hasMethod(c, "getOutputStream"), "getOutputStream present");
check(hasMethod(c, "toString"), "toString present");
check(hasMethod(c, "setDoInput"), "setDoInput present");
check(hasMethod(c, "getDoInput"), "getDoInput present");
check(hasMethod(c, "setDoOutput"), "setDoOutput present");
check(hasMethod(c, "getDoOutput"), "getDoOutput present");
check(hasMethod(c, "setAllowUserInteraction"), "setAllowUserInteraction present");
check(hasMethod(c, "getAllowUserInteraction"), "getAllowUserInteraction present");
check(hasMethod(c, "setDefaultAllowUserInteraction"), "setDefaultAllowUserInteraction present");
check(hasMethod(c, "getDefaultAllowUserInteraction"), "getDefaultAllowUserInteraction present");
check(hasMethod(c, "setUseCaches"), "setUseCaches present");
check(hasMethod(c, "getUseCaches"), "getUseCaches present");
check(hasMethod(c, "setIfModifiedSince"), "setIfModifiedSince present");
check(hasMethod(c, "getIfModifiedSince"), "getIfModifiedSince present");
check(hasMethod(c, "getDefaultUseCaches"), "getDefaultUseCaches present");
check(hasMethod(c, "setDefaultUseCaches"), "setDefaultUseCaches present");
check(hasMethod(c, "setRequestProperty"), "setRequestProperty present");
check(hasMethod(c, "addRequestProperty"), "addRequestProperty present");
check(hasMethod(c, "getRequestProperty"), "getRequestProperty present");
check(hasMethod(c, "getRequestProperties"), "getRequestProperties present");
check(hasMethod(c, "setDefaultRequestProperty"), "setDefaultRequestProperty present");
check(hasMethod(c, "getDefaultRequestProperty"), "getDefaultRequestProperty present");
check(hasMethod(c, "setContentHandlerFactory"), "setContentHandlerFactory present");
check(hasMethod(c, "guessContentTypeFromName"), "guessContentTypeFromName present");
check(hasMethod(c, "guessContentTypeFromStream"), "guessContentTypeFromStream present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
