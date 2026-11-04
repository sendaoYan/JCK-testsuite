# java.lang.invoke.StringConcatFactory — 设计方案

## 1. API 功能
字符串拼接 invokedynamic 引导（final 工具类）：`makeConcat(lookup, name, MethodType)` 按动态参数顺序拼接；`makeConcatWithConstants(lookup, name, MethodType, recipe, constants...)` 以 \u0001 占动态参数、\u0002 占常量的配方拼接。

## 2. Java 用户如何使用
```java
CallSite cs = StringConcatFactory.makeConcatWithConstants(lookup, "c",
        MethodType.methodType(String.class, String.class, String.class), "<\u0001+\u0001>");
String r = (String) cs.dynamicInvoker().invoke("x", "y"); // "<x+y>"
```

## 3. 开源代码实际用法调研
- JDK `src/.../StringConcatFactory.java`（recipe 语法、策略选择）与 `test/jdk/java/lang/String/concat/`（千行行为矩阵）。
- javac 把 `a + b` 编译成 makeConcat/makeConcatWithConstants 的 invokedynamic。

## 4. 测试用例设计 + 自动化实现
白盒：final、无私有构造、两工厂方法存在且返回 CallSite。
黑盒：makeConcat("a",1)=="a1"、空参 ==""、七种类型混合精确串；\u0001 两槽 "<x+y>"；三槽依序；纯常量配方 "just text"；\u0002 常量代入 "a=VALUE"；dynamicInvoker 可复用；槽/参数数失配→StringConcatException；null MethodType→NPE；void 参数→IAE、void 返回→StringConcatException。
对应 `StringConcatFactoryTest.java` 的 `testBlackBox()`。

## 深化批次（2026-11-05）
新增 `testPrimitiveMatrix/testConstantsRecipes/testLinkageIdentity/testNegativeLinkage`（旧 24 断言 → 新 47）：
- 原始类型矩阵：(String,int)/(int,String)/(char,char)/(byte,short) 序列、float 1.5f→"1.5"、double 2.25d→"2.25"、long/boolean/char 混合 "1.52.259falseZ"、负数 "-7true"、10 参数保序；null 引用 "xnull"、孤立 null "null"。
- 配方代数（static final SLOT_1="\u0001"/SLOT_2="\u0002" 转义常量）：多常量 "x=A/B"；**\u0003 是普通字面文本、无未知转义拒绝**；空配方 0 参 ""；文本与占位交错 "p1mKs2t"。
- 链接同一性：同 MethodType 两次 makeConcat 独立 CallSite（!=）且 type()==mt；**name 仅 null 检查：含 '.' 与空串均照常链接（观察行为锁定）**。
- 负例精确表：null name→NPE "Name is null"；null recipe→NPE "Recipe is null"；null 常量数组→NPE "Constants are null"；常量元素 null→NPE "Cannot accept null constants"；常量数与 \u0002 槽不配平（多/少）→StringConcatException "Mismatched number of concat constants"；槽/元数失配→StringConcatException "Mismatched number of concat arguments"；Integer 返回→StringConcatException "The return type should be compatible with String"。