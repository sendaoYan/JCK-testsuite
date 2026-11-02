/*
 * @test
 * @summary com.sun.tools.example.debug.expr.ExpressionParserConstants conformance: internal package reflection.
 * @modules jdk.jdi/com.sun.tools.example.debug.expr:+open
 * @run main ExpressionParserConstantsTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpressionParserConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static String[] methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(m.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] enumConstants(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.example.debug.expr.ExpressionParserConstants", false, ExpressionParserConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.tools.example.debug.expr.ExpressionParserConstants"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass");
        check(Arrays.equals(interfaces(c), new String[]{}), "interfaces");
        check(Arrays.equals(methods(c), new String[]{}), "methods");
        check(Arrays.equals(fields(c), new String[]{"ABSTRACT","ANDASSIGN","ASSIGN","BANG","BIT_AND","BIT_OR","BOOLEAN","BREAK","BYTE","CASE","CATCH","CHAR","CHARACTER_LITERAL","CLASS","COLON","COMMA","CONST","CONTINUE","DECIMAL_LITERAL","DECR","DEFAULT","DIGIT","DO","DOT","DOUBLE","ELSE","EOF","EQ","EXPONENT","EXTENDS","FALSE","FINAL","FINALLY","FLOAT","FLOATING_POINT_LITERAL","FOR","FORMAL_COMMENT","GE","GOTO","GT","HEX_LITERAL","HOOK","IDENTIFIER","IF","IMPLEMENTS","IMPORT","INCR","INSTANCEOF","INT","INTEGER_LITERAL","INTERFACE","LBRACE","LBRACKET","LE","LETTER","LONG","LPAREN","LSHIFT","LSHIFTASSIGN","LT","MINUS","MINUSASSIGN","MULTI_LINE_COMMENT","NATIVE","NE","NEW","NULL","OCTAL_LITERAL","ORASSIGN","PACKAGE","PLUS","PLUSASSIGN","PRIVATE","PROTECTED","PUBLIC","RBRACE","RBRACKET","REM","REMASSIGN","RETURN","RPAREN","RSIGNEDSHIFT","RSIGNEDSHIFTASSIGN","RUNSIGNEDSHIFT","RUNSIGNEDSHIFTASSIGN","SC_AND","SC_OR","SEMICOLON","SHORT","SINGLE_LINE_COMMENT","SLASH","SLASHASSIGN","STAR","STARASSIGN","STATIC","STRING_LITERAL","SUPER","SWITCH","SYNCHRONIZED","THIS","THROW","THROWS","TILDE","TRANSIENT","TRUE","TRY","VOID","VOLATILE","WHILE","XOR","XORASSIGN","_DEFAULT","tokenImage"}), "fields");
        check(Arrays.equals(enumConstants(c), new String[]{}), "enumConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
