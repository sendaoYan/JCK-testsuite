# UnmodifiableClassException — 设计方案

## 1. API 功能
`java.lang.instrument.UnmodifiableClassException`（继承 `Exception`）表示 `retransformClasses`/`redefineClasses` 尝试修改不可修改的类。构造器 `()` 与 `(String message)`。

## 2. Java 用户如何使用
```java
try { inst.retransformClasses(cls); }
catch (UnmodifiableClassException e) { /* 该类不可重转换 */ }
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/instrument` 用例断言对数组/原始类型类 retransform 抛该异常。
- agent 框架对不可修改类捕获该异常降级处理。

## 4. 测试用例设计 + 自动化实现
白盒：具体类 + 继承 Exception + 两个构造器。
黑盒：no-arg message null；String 构造 message/cause；instanceof Exception。
对应 `UnmodifiableClassExceptionTest.java` 白盒 + 黑盒部分。

## 5. 契约深化（本批）
- 构造器面：()/(String)/(String,Throwable)/(Throwable) 四形态全有（与 IllegalClassFormatException 的关键差异：UCE 有 cause 通道）。
- getMessage/getCause 往返：(String,Throwable) 构造后两值一致；(Throwable) 单参 → message == cause.toString()。
- 继承 Exception；serialVersionUID 未显式声明（与 ICFE 同）。
