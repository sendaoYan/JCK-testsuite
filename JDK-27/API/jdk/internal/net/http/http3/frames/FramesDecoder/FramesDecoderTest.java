/*
 * @test
 * @summary jdk.internal.net.http.http3.frames.FramesDecoder conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.http3.frames:+open
 * @run main FramesDecoderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class FramesDecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.http3.frames.FramesDecoder", false, FramesDecoderTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.http3.frames.FramesDecoder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("clean"), "method:clean");
        check(methods.contains("clear"), "method:clear");
        check(methods.contains("eof"), "method:eof");
        check(methods.contains("isAllowed"), "method:isAllowed");
        check(methods.contains("isAllowedOnClientControlStream"), "method:isAllowedOnClientControlStream");
        check(methods.contains("isAllowedOnControlStream"), "method:isAllowedOnControlStream");
        check(methods.contains("isAllowedOnPromiseStream"), "method:isAllowedOnPromiseStream");
        check(methods.contains("isAllowedOnRequestStream"), "method:isAllowedOnRequestStream");
        check(methods.contains("poll"), "method:poll");
        check(methods.contains("readPayloadBytes"), "method:readPayloadBytes");
        check(methods.contains("submit"), "method:submit");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
