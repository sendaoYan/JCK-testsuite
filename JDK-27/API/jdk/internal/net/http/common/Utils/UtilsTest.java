/*
 * @test
 * @summary jdk.internal.net.http.common.Utils conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main UtilsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class UtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.Utils", false, UtilsTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.Utils"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("PROXY_TUNNEL_RESTRICTED"), "method:PROXY_TUNNEL_RESTRICTED");
        check(methods.contains("accumulateBuffers"), "method:accumulateBuffers");
        check(methods.contains("addSuppressed"), "method:addSuppressed");
        check(methods.contains("addressConflict"), "method:addressConflict");
        check(methods.contains("asHexString"), "method:asHexString");
        check(methods.contains("asString"), "method:asString");
        check(methods.contains("charsetFrom"), "method:charsetFrom");
        check(methods.contains("close"), "method:close");
        check(methods.contains("configureChannelBuffers"), "method:configureChannelBuffers");
        check(methods.contains("copy"), "method:copy");
        check(methods.contains("copyAligned"), "method:copyAligned");
        check(methods.contains("copyProxy"), "method:copyProxy");
        check(methods.contains("copySSLParameters"), "method:copySSLParameters");
        check(methods.contains("createPseudoHeaders"), "method:createPseudoHeaders");
        check(methods.contains("debugDeadline"), "method:debugDeadline");
        check(methods.contains("describeOps"), "method:describeOps");
        check(methods.contains("dump"), "method:dump");
        check(methods.contains("encode"), "method:encode");
        check(methods.contains("exceptionNow"), "method:exceptionNow");
        check(methods.contains("flipToMark"), "method:flipToMark");
        check(methods.contains("getBooleanProperty"), "method:getBooleanProperty");
        check(methods.contains("getBuffer"), "method:getBuffer");
        check(methods.contains("getBufferWithAtMost"), "method:getBufferWithAtMost");
        check(methods.contains("getCancelCause"), "method:getCancelCause");
        check(methods.contains("getCompletionCause"), "method:getCompletionCause");
        check(methods.contains("getDebugLogger"), "method:getDebugLogger");
        check(methods.contains("getHpackLogger"), "method:getHpackLogger");
        check(methods.contains("getIOException"), "method:getIOException");
        check(methods.contains("getIntegerNetProperty"), "method:getIntegerNetProperty");
        check(methods.contains("getIntegerProperty"), "method:getIntegerProperty");
        check(methods.contains("getLongProperty"), "method:getLongProperty");
        check(methods.contains("getNetProperty"), "method:getNetProperty");
        check(methods.contains("getProperty"), "method:getProperty");
        check(methods.contains("getSoReceiveBufferSize"), "method:getSoReceiveBufferSize");
        check(methods.contains("getSoSendBufferSize"), "method:getSoSendBufferSize");
        check(methods.contains("getWebSocketLogger"), "method:getWebSocketLogger");
        check(methods.contains("hasRemaining"), "method:hasRemaining");
        check(methods.contains("immutableSession"), "method:immutableSession");
        check(methods.contains("interestOps"), "method:interestOps");
        check(methods.contains("isHostnameVerificationDisabled"), "method:isHostnameVerificationDisabled");
        check(methods.contains("isValidLowerCaseName"), "method:isValidLowerCaseName");
        check(methods.contains("isValidName"), "method:isValidName");
        check(methods.contains("isValidValue"), "method:isValidValue");
        check(methods.contains("millis"), "method:millis");
        check(methods.contains("mismatch"), "method:mismatch");
        check(methods.contains("newIAE"), "method:newIAE");
        check(methods.contains("next"), "method:next");
        check(methods.contains("pow2Size"), "method:pow2Size");
        check(methods.contains("proxyHasDisabledSchemes"), "method:proxyHasDisabledSchemes");
        check(methods.contains("read"), "method:read");
        check(methods.contains("readContentLength"), "method:readContentLength");
        check(methods.contains("readStatusCode"), "method:readStatusCode");
        check(methods.contains("readyOps"), "method:readyOps");
        check(methods.contains("remaining"), "method:remaining");
        check(methods.contains("resolveAddress"), "method:resolveAddress");
        check(methods.contains("safeExecutor"), "method:safeExecutor");
        check(methods.contains("setUserAuthFlags"), "method:setUserAuthFlags");
        check(methods.contains("setWebSocketUpgradeHeaders"), "method:setWebSocketUpgradeHeaders");
        check(methods.contains("sliceOrCopy"), "method:sliceOrCopy");
        check(methods.contains("sliceWithLimitedCapacity"), "method:sliceWithLimitedCapacity");
        check(methods.contains("stackTrace"), "method:stackTrace");
        check(methods.contains("stringOf"), "method:stringOf");
        check(methods.contains("synchronizedRemaining"), "method:synchronizedRemaining");
        check(methods.contains("toConnectException"), "method:toConnectException");
        check(methods.contains("toIOException"), "method:toIOException");
        check(methods.contains("trySetReceiveBufferSize"), "method:trySetReceiveBufferSize");
        check(methods.contains("trySetSendBufferSize"), "method:trySetSendBufferSize");
        check(methods.contains("unchecked"), "method:unchecked");
        check(methods.contains("useVTForSelector"), "method:useVTForSelector");
        check(methods.contains("wrapForDebug"), "method:wrapForDebug");
        check(methods.contains("wrapWithExtraDetail"), "method:wrapWithExtraDetail");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ACCEPT_ALL"), "field:ACCEPT_ALL");
        check(fields.contains("ALLOWED_HEADERS"), "field:ALLOWED_HEADERS");
        check(fields.contains("ALLOWED_REDIRECT_HEADERS"), "field:ALLOWED_REDIRECT_HEADERS");
        check(fields.contains("ASSERTIONSENABLED"), "field:ASSERTIONSENABLED");
        check(fields.contains("BUFSIZE"), "field:BUFSIZE");
        check(fields.contains("DEBUG"), "field:DEBUG");
        check(fields.contains("DEBUG_CONFIG"), "field:DEBUG_CONFIG");
        check(fields.contains("DEBUG_HPACK_CONFIG"), "field:DEBUG_HPACK_CONFIG");
        check(fields.contains("DEBUG_WS"), "field:DEBUG_WS");
        check(fields.contains("DEBUG_WS_CONFIG"), "field:DEBUG_WS_CONFIG");
        check(fields.contains("EMPTY_BB_ARRAY"), "field:EMPTY_BB_ARRAY");
        check(fields.contains("EMPTY_BB_LIST"), "field:EMPTY_BB_LIST");
        check(fields.contains("EMPTY_BYTEBUFFER"), "field:EMPTY_BYTEBUFFER");
        check(fields.contains("NO_PROXY_HEADERS_FILTER"), "field:NO_PROXY_HEADERS_FILTER");
        check(fields.contains("PROXY_FILTER"), "field:PROXY_FILTER");
        check(fields.contains("PROXY_TUNNEL_FILTER"), "field:PROXY_TUNNEL_FILTER");
        check(fields.contains("SLICE_THRESHOLD"), "field:SLICE_THRESHOLD");
        check(fields.contains("TESTING"), "field:TESTING");
        check(fields.contains("VALIDATE_USER_HEADER"), "field:VALIDATE_USER_HEADER");
        check(fields.contains("isHostnameVerificationDisabled"), "field:isHostnameVerificationDisabled");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
