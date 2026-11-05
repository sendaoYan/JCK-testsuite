/*
 * @test
 * @summary javax.management.modelmbean.DescriptorSupport conformance: setField/getFieldValue/isValid.
 * @run main DescriptorSupportTest
 */

import javax.management.modelmbean.*;
import javax.management.RuntimeOperationsException;
public class DescriptorSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        DescriptorSupport d = new DescriptorSupport();
        d.setField("name", "value");
        check("value".equals(d.getFieldValue("name")), "getFieldValue");
        check(d.getFieldNames().length == 1, "field names");
        testConstructorMatrix();
        testMutabilityOperations();
        testFieldTypeRules();
        testIsValidMatrix();
        testEqualsHashCodeToStringClone();
        testSerializationRoundTrip();
        testContrastWithImmutableDescriptor();
        testXmlConstructorAndToXmlString();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch h): full contract coverage ----

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void checkRoe(ThrowingRunnable r, String msg, final String expectMsgPart) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected RuntimeOperationsException but nothing was thrown");
        } catch (RuntimeOperationsException roe) {
            if (expectMsgPart != null)
                check(roe.getMessage() != null && roe.getMessage().contains(expectMsgPart),
                        msg + ": message [" + roe.getMessage() + "] must contain [" + expectMsgPart + "]");
            else
                check(roe.getMessage() != null, msg + ": message non-null");
            passed++;
        } catch (AssertionError ae) {
            throw ae;
        } catch (Throwable t) {
            throw new AssertionError(msg + ": expected RuntimeOperationsException but got " + t.getClass().getName(), t);
        }
    }

    static void testConstructorMatrix() throws Exception {
        // no-arg: empty descriptor
        DescriptorSupport e = new DescriptorSupport();
        check(e.getFieldNames().length == 0, "no-arg ctor: 0 fields");
        check(e.getFields().length == 0, "no-arg ctor: 0 field strings");
        // capacity ctor
        DescriptorSupport cap = new DescriptorSupport(5);
        check(cap.getFieldNames().length == 0, "capacity ctor: empty");
        checkRoe(new ThrowingRunnable() { public void run() throws Exception { new DescriptorSupport(0); } },
                "capacity 0 rejected", "Descriptor field limit invalid");
        checkRoe(new ThrowingRunnable() { public void run() throws Exception { new DescriptorSupport(-3); } },
                "negative capacity rejected", "Descriptor field limit invalid");
        // String... fields constructor ("name=value" parse)
        DescriptorSupport v = new DescriptorSupport(new String[]{"a=1", "b=2"});
        check("1".equals(v.getFieldValue("a")) && "2".equals(v.getFieldValue("b")), "vararg ctor parse");
        check(v.getFieldNames().length == 2, "vararg ctor field count");
        check("1".equals(new DescriptorSupport("a=1", "b=2").getFieldValue("a")),
                "multi-arg literal resolves to varargs ctor");
        check(new DescriptorSupport(new String[]{}).getFieldNames().length == 0,
                "empty vararg array: empty descriptor");
        check(new DescriptorSupport((String[]) null).getFieldNames().length == 0,
                "null vararg array: empty descriptor");
        DescriptorSupport skipEmpty = new DescriptorSupport(new String[]{"", null, "k=v"});
        check(skipEmpty.getFieldNames().length == 1 && "v".equals(skipEmpty.getFieldValue("k")),
                "empty/null strings skipped in vararg ctor");
        checkRoe(new ThrowingRunnable() { public void run() { new DescriptorSupport(new String[]{"novalue"}); } },
                "no equals sign rejected", "no equals sign");
        checkRoe(new ThrowingRunnable() { public void run() { new DescriptorSupport(new String[]{"=v"}); } },
                "empty field name rejected", "no fieldName");
        // trailing '=' yields an empty-string value (legal for unrestricted names)
        check("".equals(new DescriptorSupport(new String[]{"k="}).getFieldValue("k")),
                "trailing '=' gives empty-string value (free-form name)");
        // (String[], Object[]) constructor
        DescriptorSupport so = new DescriptorSupport(new String[]{"n", "dt"}, new Object[]{"x", "mbean"});
        check("x".equals(so.getFieldValue("n")), "(names,values) ctor");
        checkRoe(new ThrowingRunnable() { public void run() {
            new DescriptorSupport(new String[]{"a"}, new Object[]{});
        } }, "names/values length mismatch", null);
        checkRoe(new ThrowingRunnable() { public void run() {
            new DescriptorSupport(new String[]{null}, new Object[]{"v"});
        } }, "null field name rejected", "Field name to be set is null or empty");
        // duplicate names: last value wins
        DescriptorSupport dup = new DescriptorSupport(new String[]{"a", "a"}, new Object[]{"1", "2"});
        check("2".equals(dup.getFieldValue("a")) && dup.getFieldNames().length == 1, "duplicate name: last wins");
        // copy constructor
        DescriptorSupport orig = new DescriptorSupport(new String[]{"name", "descriptorType"}, new Object[]{"x", "mbean"});
        DescriptorSupport copy = new DescriptorSupport(orig);
        check(copy.equals(orig), "copy ctor equals");
        copy.setField("name", "y");
        check("x".equals(orig.getFieldValue("name")), "copy ctor independent");
        check(new DescriptorSupport((DescriptorSupport) null).getFieldNames().length == 0,
                "copy ctor accepts null -> empty");
    }

    static void testMutabilityOperations() throws Exception {
        final DescriptorSupport d = new DescriptorSupport();
        d.setField("s", "str");
        check("str".equals(d.getFieldValue("s")), "setField add");
        d.setField("s", "str2");
        check("str2".equals(d.getFieldValue("s")) && d.getFieldNames().length == 1, "setField overwrite");
        // batch setFields
        d.setFields(new String[]{"a", "b"}, new Object[]{"1", "2"});
        check("1".equals(d.getFieldValue("a")) && "2".equals(d.getFieldValue("b")), "setFields batch");
        d.setFields(new String[]{"a"}, new Object[]{"9"});
        check("9".equals(d.getFieldValue("a")), "setFields overwrite existing");
        checkRoe(new ThrowingRunnable() { public void run() {
            d.setFields(new String[]{"c"}, new Object[]{});
        } }, "setFields length mismatch", null);
        checkRoe(new ThrowingRunnable() { public void run() {
            d.setFields(null, null);
        } }, "setFields null arrays", null);
        // removeField
        d.removeField("a");
        check(d.getFieldValue("a") == null, "removeField existing -> null");
        d.removeField("nope"); // silent no-op
        check(true, "removeField nonexistent: silent no-op");
        d.removeField(null);
        d.removeField("");
        check(true, "removeField null/empty name: no-op");
        // getFieldValue edge cases
        check(d.getFieldValue("nope") == null, "getFieldValue nonexistent -> null");
        checkRoe(new ThrowingRunnable() { public void run() { d.getFieldValue(null); } },
                "getFieldValue null name", null);
        checkRoe(new ThrowingRunnable() { public void run() { d.getFieldValue(""); } },
                "getFieldValue empty name", null);
        // setField null/empty name and null value for restricted name
        checkRoe(new ThrowingRunnable() { public void run() { d.setField(null, "v"); } },
                "setField null name", "Field name to be set is null or empty");
        checkRoe(new ThrowingRunnable() { public void run() { d.setField("", "v"); } },
                "setField empty name", "Field name to be set is null or empty");
        checkRoe(new ThrowingRunnable() { public void run() { d.setField("name", null); } },
                "setField restricted name null value", "Field value invalid");
        // free-form name accepts null value
        d.setField("freeForm", null);
        check(d.getFieldValue("freeForm") == null, "free-form name accepts null value");
        // getFieldNames: sorted alphabetically
        DescriptorSupport sn = new DescriptorSupport(new String[]{"c=3", "a=1", "b=2"});
        String[] names = sn.getFieldNames();
        check(names.length == 3 && "a".equals(names[0]) && "b".equals(names[1]) && "c".equals(names[2]),
                "getFieldNames sorted");
        // first-seen field name case is preserved on later case-differing set
        DescriptorSupport cp = new DescriptorSupport();
        cp.setField("CamelCase", "v1");
        cp.setField("camelcase", "v2");
        check(cp.getFieldNames().length == 1 && "CamelCase".equals(cp.getFieldNames()[0]),
                "field name case preserved on overwrite");
        check("v2".equals(cp.getFieldValue("camelcase")), "case-insensitive lookup sees new value");
        // getFields: "name=value" strings, non-String values parenthesized, null value empty
        DescriptorSupport gf = new DescriptorSupport();
        gf.setField("s", "str");
        gf.setField("n", Integer.valueOf(5));
        gf.setField("f", null);
        java.util.List<String> fs = java.util.Arrays.asList(gf.getFields());
        check(fs.contains("s=str"), "getFields string value form");
        check(fs.contains("n=(5)"), "getFields non-string value parenthesized: " + fs);
        check(fs.contains("f="), "getFields null value empty");
        // getFieldValues with missing names -> null elements
        Object[] vals = gf.getFieldValues("s", "missing", "n");
        check("str".equals(vals[0]) && vals[1] == null && Integer.valueOf(5).equals(vals[2]),
                "getFieldValues includes null for missing");
        // getFieldValues() passes String[0] (not null): returns zero values; explicit null returns all
        check(gf.getFieldValues().length == 0, "getFieldValues() with empty vararg returns zero values");
        check(gf.getFieldValues((String[]) null).length == 3, "getFieldValues((String[])null) returns all");
        Object[] withBlanks = gf.getFieldValues("s", null, "");
        check("str".equals(withBlanks[0]) && withBlanks[1] == null && withBlanks[2] == null,
                "getFieldValues maps null/empty names to null elements");
    }

    static void testFieldTypeRules() throws Exception {
        // restricted names require non-null String values
        String[] stringOnly = {"name", "descriptorType", "getMethod", "setMethod", "role", "class"};
        for (final String fn : stringOnly) {
            DescriptorSupport ok = new DescriptorSupport();
            ok.setField(fn, "SomeValue");
            check("SomeValue".equals(ok.getFieldValue(fn)), "setField(" + fn + ") accepts String");
            checkRoe(new ThrowingRunnable() { public void run() {
                new DescriptorSupport().setField(fn, Integer.valueOf(1));
            } }, "setField(" + fn + ") rejects Integer", null);
            checkRoe(new ThrowingRunnable() { public void run() {
                new DescriptorSupport().setField(fn, null);
            } }, "setField(" + fn + ") rejects null", null);
        }
        // name/descriptorType additionally require non-empty
        checkRoe(new ThrowingRunnable() { public void run() {
            new DescriptorSupport().setField("name", "");
        } }, "setField(name,\"\") rejected", null);
        checkRoe(new ThrowingRunnable() { public void run() {
            new DescriptorSupport().setField("descriptorType", "");
        } }, "setField(descriptorType,\"\") rejected", null);
        // visibility: 1..4 as String or Integer
        DescriptorSupport vis = new DescriptorSupport();
        vis.setField("visibility", "4");
        vis.setField("visibility", Integer.valueOf(1));
        check(true, "visibility accepts 1..4 (String and Integer)");
        for (final Object bad : new Object[]{"0", "5", Integer.valueOf(0), Integer.valueOf(5), null}) {
            checkRoe(new ThrowingRunnable() { public void run() {
                new DescriptorSupport().setField("visibility", bad);
            } }, "visibility rejects " + bad, null);
        }
        // severity: 0..6
        DescriptorSupport sev = new DescriptorSupport();
        sev.setField("severity", "0");
        sev.setField("severity", Integer.valueOf(6));
        check(true, "severity accepts 0..6");
        for (final Object bad : new Object[]{"-1", "7", Integer.valueOf(7)}) {
            checkRoe(new ThrowingRunnable() { public void run() {
                new DescriptorSupport().setField("severity", bad);
            } }, "severity rejects " + bad, null);
        }
        // log: T/true/F/false or Boolean
        DescriptorSupport log = new DescriptorSupport();
        log.setField("log", "T");
        log.setField("log", "true");
        log.setField("log", Boolean.FALSE);
        check(true, "log accepts T/true/Boolean");
        checkRoe(new ThrowingRunnable() { public void run() {
            new DescriptorSupport().setField("log", "yes");
        } }, "log rejects other strings", null);
        // currencyTimeLimit & friends: numeric String or Number >= -1
        DescriptorSupport ctl = new DescriptorSupport();
        ctl.setField("currencyTimeLimit", "-1");
        ctl.setField("currencyTimeLimit", "10");
        ctl.setField("currencyTimeLimit", Integer.valueOf(30));
        ctl.setField("persistPeriod", Long.valueOf(5));
        check(true, "currencyTimeLimit/persistPeriod accept numeric >= -1");
        checkRoe(new ThrowingRunnable() { public void run() {
            new DescriptorSupport().setField("currencyTimeLimit", "-2");
        } }, "currencyTimeLimit < -1 rejected", null);
        checkRoe(new ThrowingRunnable() { public void run() {
            new DescriptorSupport().setField("currencyTimeLimit", "abc");
        } }, "currencyTimeLimit non-numeric rejected", null);
        // persistPolicy: fixed enumeration
        DescriptorSupport pp = new DescriptorSupport();
        pp.setField("persistPolicy", "OnUpdate");
        pp.setField("persistPolicy", "Never");
        pp.setField("persistPolicy", "NoMoreOftenThan");
        check(true, "persistPolicy accepts legal values");
        checkRoe(new ThrowingRunnable() { public void run() {
            new DescriptorSupport().setField("persistPolicy", "bogus");
        } }, "persistPolicy rejects illegal value", null);
        // unrestricted names accept any value type
        Object[] free = {"s", Integer.valueOf(1), Long.valueOf(2L), Float.valueOf(3f), Double.valueOf(4d),
                Byte.valueOf((byte) 5), Character.valueOf('c'), Short.valueOf((short) 6), Boolean.TRUE,
                new int[]{1, 2}, new String[]{"x"}, String.class, new Object()};
        for (Object val : free) {
            DescriptorSupport f = new DescriptorSupport();
            f.setField("myField", val);
            check(java.util.Objects.equals(val, f.getFieldValue("myField")),
                    "free-form field accepts " + val.getClass().getName());
        }
    }

    static void testIsValidMatrix() throws Exception {
        check(!new DescriptorSupport().isValid(), "empty descriptor invalid");
        check(!new DescriptorSupport(new String[]{"descriptorType=mbean"}).isValid(),
                "missing name -> invalid");
        check(!new DescriptorSupport(new String[]{"name=x"}).isValid(),
                "missing descriptorType -> invalid");
        check(new DescriptorSupport(new String[]{"name=x", "descriptorType=mbean"}).isValid(),
                "name+descriptorType -> valid");
        check(new DescriptorSupport(new String[]{"name=x", "descriptorType=bogus"}).isValid(),
                "descriptorType value itself is not validated by isValid");
        check(new DescriptorSupport(new String[]{"name=123", "descriptorType=mbean"}).isValid(),
                "numeric name string still valid");
    }

    static void testEqualsHashCodeToStringClone() throws Exception {
        DescriptorSupport a = new DescriptorSupport(new String[]{"a", "b"}, new Object[]{"1", "2"});
        DescriptorSupport b = new DescriptorSupport(new String[]{"b", "a"}, new Object[]{"2", "1"});
        check(a.equals(b), "equals ignores field order");
        check(a.hashCode() == b.hashCode(), "hashCode consistent with equals");
        check(!a.equals(new DescriptorSupport(new String[]{"a"}, new Object[]{"1"})), "different content not equal");
        check(!a.equals(null), "not equal to null");
        // cross-implementation contract with ImmutableDescriptor
        javax.management.ImmutableDescriptor id = new javax.management.ImmutableDescriptor(
                new String[]{"b", "a"}, new Object[]{"2", "1"});
        check(a.equals(id), "DescriptorSupport.equals(ImmutableDescriptor) same content -> true");
        check(id.equals(a), "ImmutableDescriptor.equals(DescriptorSupport) same content -> true");
        check(a.hashCode() == id.hashCode(), "hashCode equal across implementations");
        javax.management.ImmutableDescriptor id2 = new javax.management.ImmutableDescriptor("a=9");
        check(!a.equals(id2) && !id2.equals(a), "cross-implementation different content not equal");
        // toString: comma+space joined name=value
        DescriptorSupport t = new DescriptorSupport(new String[]{"descriptorType=mbean", "name=n1"});
        String ts = t.toString();
        check(ts.contains("descriptorType=mbean") && ts.contains("name=n1") && ts.contains(", "),
                "toString contains descriptorType= and name= joined by comma+space: " + ts);
        check("".equals(new DescriptorSupport().toString()), "empty toString is empty string");
        // clone: independent mutable copy
        DescriptorSupport c = (DescriptorSupport) a.clone();
        check(c.equals(a) && c != a, "clone equals but not same instance");
        c.setField("a", "changed");
        check("1".equals(a.getFieldValue("a")), "clone mutation does not affect original");
        c.removeField("b");
        check(a.getFieldValue("b") != null, "clone removal does not affect original");
    }

    static void testSerializationRoundTrip() throws Exception {
        DescriptorSupport d = new DescriptorSupport(new String[]{"name", "descriptorType", "extra"},
                new Object[]{"n1", "mbean", Integer.valueOf(7)});
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(d);
            oos.close();
            Object back = new java.io.ObjectInputStream(
                    new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
            check(back instanceof DescriptorSupport, "round-trip class preserved");
            DescriptorSupport rd = (DescriptorSupport) back;
            check(rd.equals(d), "round-trip equals");
            check(Integer.valueOf(7).equals(rd.getFieldValue("extra")), "round-trip non-string value");
            // mutability is preserved after deserialization
            rd.setField("late", "v");
            check("v".equals(rd.getFieldValue("late")), "deserialized descriptor still mutable");
            check(rd.getFieldNames().length == d.getFieldNames().length + 1, "field count after mutation");
        } catch (java.io.IOException e) {
            throw new AssertionError("serialization failed", e);
        }
    }

    static void testContrastWithImmutableDescriptor() throws Exception {
        // core contrast: identical operations succeed on DescriptorSupport but throw on ImmutableDescriptor
        DescriptorSupport ds = new DescriptorSupport(new String[]{"k=v"});
        javax.management.ImmutableDescriptor id = new javax.management.ImmutableDescriptor("k=v");
        ds.setField("k", "v2");
        check("v2".equals(ds.getFieldValue("k")), "DescriptorSupport.setField mutates in place");
        try {
            id.setField("k", "v2");
            throw new AssertionError("ImmutableDescriptor.setField must throw");
        } catch (RuntimeOperationsException roe) {
            check(true, "ImmutableDescriptor.setField -> RuntimeOperationsException (immutable)");
        }
        ds.removeField("k");
        check(ds.getFieldValue("k") == null, "DescriptorSupport.removeField succeeds");
        try {
            id.removeField("k");
            throw new AssertionError("ImmutableDescriptor.removeField must throw");
        } catch (RuntimeOperationsException roe) {
            check(true, "ImmutableDescriptor.removeField -> RuntimeOperationsException (immutable)");
        }
        // both still report equal content semantics while contents still match
        javax.management.ImmutableDescriptor id3 = new javax.management.ImmutableDescriptor("k2=v");
        DescriptorSupport ds3 = new DescriptorSupport(new String[]{"k2=v"});
        check(ds3.equals(id3) && id3.equals(ds3), "cross-implementation equals still holds");
    }

    static void testXmlConstructorAndToXmlString() throws Exception {
        // toXMLString form and round-trip (JDK-8 specific API; removed in JDK-27)
        DescriptorSupport x = new DescriptorSupport(new String[]{"name", "descriptorType"},
                new Object[]{"n1", "mbean"});
        String xml = x.toXMLString();
        check(xml.startsWith("<Descriptor") && xml.endsWith("</Descriptor>"),
                "toXMLString wrapped in <Descriptor>: " + xml);
        check(xml.contains("<field name=\"name\" value=\"n1\">"), "toXMLString field element form");
        DescriptorSupport rt = new DescriptorSupport(xml);
        check("n1".equals(rt.getFieldValue("name")) && "mbean".equals(rt.getFieldValue("descriptorType")),
                "XML round-trip restores fields");
        check(rt.equals(x), "XML round-trip equals original");
        // empty descriptor XML round-trip
        String emptyXml = new DescriptorSupport().toXMLString();
        check(new DescriptorSupport(emptyXml).getFieldNames().length == 0, "empty XML round-trip");
        // non-String values are encoded as (type/value) and restored by round-trip
        DescriptorSupport mixed = new DescriptorSupport(new String[]{"name", "descriptorType", "n"},
                new Object[]{"n1", "mbean", Integer.valueOf(5)});
        String mx = mixed.toXMLString();
        check(mx.contains("value=\"(java.lang.Integer/5)\""),
                "non-String value encoded as (type/value): " + mx);
        check(new DescriptorSupport(mx).getFieldValue("n").equals(Integer.valueOf(5)),
                "XML round-trip restores the Integer value");
        // negative XML inputs
        try {
            new DescriptorSupport("<bad");
            throw new AssertionError("bad XML must throw XMLParseException");
        } catch (XMLParseException xe) {
            check(xe.getMessage() != null && xe.getMessage().contains("No <descriptor>"),
                    "bad XML message: " + xe.getMessage());
        }
        try {
            new DescriptorSupport("plainstring");
            throw new AssertionError("single plain String resolves to XML ctor and must throw");
        } catch (XMLParseException xe) {
            check(true, "single-String literal goes to XML constructor (no equals pair)");
        }
        checkRoe(new ThrowingRunnable() { public void run() throws Exception { new DescriptorSupport((String) null); } },
                "XML ctor null string", "String in parameter is null");
        // self-closing <field/> elements are not understood by the parser: only open/close pairs work
        DescriptorSupport sc = new DescriptorSupport(
                "<Descriptor><field name=\"a\" value=\"1\"/></Descriptor>");
        check(sc.getFieldValue("a") == null, "self-closing field elements are not parsed");
    }
}
