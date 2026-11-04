/*
 * @test
 * @summary javax.xml.validation.SchemaFactory conformance: schema factory.
 * @run main SchemaFactoryTest
 */


public class SchemaFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        javax.xml.validation.SchemaFactory sf = javax.xml.validation.SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        check(sf != null, "factory non-null");
        check(sf.isSchemaLanguageSupported("http://www.w3.org/2001/XMLSchema"), "xsd supported");
        javax.xml.validation.Schema schema = sf.newSchema();
        check(schema != null, "schema non-null");
        javax.xml.validation.Validator v = schema.newValidator();
        check(v != null, "validator non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
