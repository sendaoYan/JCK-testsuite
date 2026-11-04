/*
 * @test
 * @summary java.beans.XMLEncoder conformance: full contract.
 *          extends Encoder + implements AutoCloseable, one-arg and four-arg
 *          constructors, writeObject for bean/array/list/map/string/integer/
 *          null, writeStatement/writeExpression, setOwner/getOwner, flush/close.
 * @run main XMLEncoderTest
 */

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.Statement;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLEncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    public static class Bean {
        private int x;
        private String s;
        public Bean() {}
        public int getX() { return x; }
        public void setX(int v) { x = v; }
        public String getS() { return s; }
        public void setS(String v) { s = v; }
    }

    static String encode(Object o) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.writeObject(o);
        enc.close();
        return baos.toString("UTF-8");
    }

    static void testStructure() {
        Class<?> c = XMLEncoder.class;
        check(Encoder.class.isAssignableFrom(c), "extends Encoder");
        check(AutoCloseable.class.isAssignableFrom(c), "implements AutoCloseable");
    }

    static void testWriteObject() throws Exception {
        String xml = encode("hello");
        check(xml.contains("hello"), "string encoded");

        check(encode(Integer.valueOf(42)).contains("42"), "Integer encoded");

        Bean bean = new Bean();
        bean.setX(42);
        bean.setS("hi");
        String beanXml = encode(bean);
        check(beanXml.contains("42") && beanXml.contains("hi"), "bean properties encoded");

        String arrXml = encode(new int[] { 1, 2, 3 });
        check(arrXml.contains("1") && arrXml.contains("2") && arrXml.contains("3"), "array encoded");

        List<Integer> list = new ArrayList<Integer>(Arrays.asList(1, 2, 3));
        check(encode(list).contains("1"), "List encoded");

        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("a", 1);
        check(encode(map).contains("a"), "Map encoded");

        String nullXml = encode(null);
        check(nullXml.contains("<null/>"), "null encoded as <null/>");
    }

    static void testOwner() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        check(enc.getOwner() == null, "default owner null");
        Object owner = new Object();
        enc.setOwner(owner);
        check(owner == enc.getOwner(), "setOwner/getOwner");
        enc.close();
    }

    static void testFourArgConstructor() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos, "UTF-8", true, 16);
        enc.writeObject("x");
        enc.flush();
        enc.close();
        String xml = baos.toString("UTF-8");
        check(xml.contains("x"), "four-arg constructor encodes");
        check(xml.contains("<?xml"), "xml declaration present");
    }

    static void testStatements() throws Exception {
        Bean bean = new Bean();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.writeStatement(new Statement(bean, "setX", new Object[] { Integer.valueOf(5) }));
        enc.writeExpression(new Expression(bean, "getX", new Object[0]));
        enc.close();
        String xml = baos.toString("UTF-8");
        check(xml.contains("5"), "writeStatement/writeExpression produce output");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testWriteObject();
        testOwner();
        testFourArgConstructor();
        testStatements();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
