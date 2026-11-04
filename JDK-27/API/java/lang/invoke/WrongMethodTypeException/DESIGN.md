# java.lang.invoke.WrongMethodTypeException — 设计方案

## 1. API 功能
方法句柄类型不匹配运行时异常（RuntimeException 直接子类，非 IAE）：invokeExact 静态类型不符、asType 不可行转换、dynamicInvoker 类型不符时抛出。

## 2. Java 用户如何使用
```java
try {
    Object r = (String) mh.invokeExact(42); // 期望 int 返回
} catch (WrongMethodTypeException expected) { }
```

## 3. 开源代码实际用法调研
- JDK `MethodHandleTest.java`（invokeExact 类型检查章节）。
- invokedynamic 失配链接时 JVM 直接抛出；异常层级为 RuntimeException→WMTE（不含 IllegalArgumentException）。

## 4. 测试用例设计 + 自动化实现
白盒：超类是 RuntimeException（非 IllegalArgumentException）、公有构造 4 个、serialVersionUID。
黑盒：invokeExact 错误静态返回类型→WMTE（非 CCE）；invokeWithArguments 错 arity→WMTE；asType 不可行转换→WMTE；MutableCallSite.dynamicInvoker 错类型→WMTE；message/cause/suppressed 全保留。
对应 `WrongMethodTypeExceptionTest.java` 的 `testBlackBox()`。
## 深化批次（2026-11-05）

新增：序列化 round-trip message 保留、initCause "Can't overwrite cause" ISE、toString 全等、27 侧触发消息 "handle's method type X but found Y"（与 8 的 "expected X but found Y" 分侧锁定）。（17→22 断言）
