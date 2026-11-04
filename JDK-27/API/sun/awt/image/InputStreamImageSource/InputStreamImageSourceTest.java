/*
 * @test
 * @summary sun.awt.image.InputStreamImageSource conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.image:+open
 * @run main InputStreamImageSourceTest
 */

import java.util.*;
import java.lang.reflect.*;

public class InputStreamImageSourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.image.InputStreamImageSource", false, InputStreamImageSourceTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.image.InputStreamImageSource".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.image.ImageProducer;sun.awt.image.ImageFetchable".equals(interfaces(c)), "interfaces");
        check("#int#countConsumers(sun.awt.image.ImageConsumerQueue);#void#latchConsumers(sun.awt.image.ImageDecoder);private synchronized#void#removeDecoder(sun.awt.image.ImageDecoder);private synchronized#void#startProduction();private synchronized#void#stopProduction();private#void#badDecoder();private#void#errorAllConsumers(sun.awt.image.ImageConsumerQueue,boolean);private#void#errorConsumer(sun.awt.image.ImageConsumerQueue,boolean);private#void#setDecoder(sun.awt.image.ImageDecoder);protected abstract#sun.awt.image.ImageDecoder#getDecoder();protected#sun.awt.image.ImageDecoder#decoderForType(java.io.InputStream,java.lang.String);protected#sun.awt.image.ImageDecoder#getDecoder(java.io.InputStream);public synchronized#boolean#isConsumer(java.awt.image.ImageConsumer);public synchronized#void#removeConsumer(java.awt.image.ImageConsumer);public#void#addConsumer(java.awt.image.ImageConsumer);public#void#doFetch();public#void#requestTopDownLeftRightResend(java.awt.image.ImageConsumer);public#void#startProduction(java.awt.image.ImageConsumer);synchronized#int#countConsumers();synchronized#void#addConsumer(java.awt.image.ImageConsumer,boolean);synchronized#void#doneDecoding(sun.awt.image.ImageDecoder);synchronized#void#flush();synchronized#void#printQueue(sun.awt.image.ImageConsumerQueue,java.lang.String);synchronized#void#printQueues(java.lang.String)".equals(methods(c)), "methods");
        check("#boolean#awaitingFetch;#sun.awt.image.ImageConsumerQueue#consumers;#sun.awt.image.ImageDecoder#decoder;#sun.awt.image.ImageDecoder#decoders".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 1, "ctors");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        Collections.sort(l);
        return String.join(";", l);
    }

    static String methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            StringBuilder s = new StringBuilder();
            s.append(java.lang.reflect.Modifier.toString(m.getModifiers())).append('#');
            s.append(m.getReturnType().getName()).append('#');
            s.append(m.getName()).append('(');
            Class<?>[] ps = m.getParameterTypes();
            for (int i = 0; i < ps.length; i++) { if (i > 0) s.append(','); s.append(ps[i].getName()); }
            s.append(')');
            l.add(s.toString());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            l.add(java.lang.reflect.Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        if (!c.isEnum()) return "";
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }
}
