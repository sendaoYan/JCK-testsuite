/*
 * @test
 * @summary JLS 5.4: string contexts apply to the operand of the binary +
 *          operator that is not a String when the other operand is a String;
 *          the non-String operand undergoes string conversion.
 * @run main StringContexts
 */

public class StringContexts {
    public static void main(String[] args) {
        String s = "value=";

        // The non-String operand (int) is converted to a String.
        String r1 = s + 42;
        if (!r1.equals("value=42")) throw new AssertionError("String + int");

        String r2 = 42 + s;
        if (!r2.equals("42value=")) throw new AssertionError("int + String");

        // Non-String operands include any type: boolean, char, reference, null.
        if (!(s + true).equals("value=true")) throw new AssertionError("String + boolean");
        if (!(s + 'A').equals("value=A")) throw new AssertionError("String + char");
        Object o = null;
        if (!(s + o).equals("value=null")) throw new AssertionError("String + null reference");

        System.out.println("PASS");
    }
}
