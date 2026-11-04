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