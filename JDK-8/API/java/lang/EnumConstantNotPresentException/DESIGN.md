# EnumConstantNotPresentException — 设计方案

## 1. API 功能
`java.lang.EnumConstantNotPresentException(Class<? extends Enum> enumType, String constantName)`：当应用试图按名字访问枚举常量而该常量已从编译后的枚举类型中删除时抛出（典型：注解成员的枚举值引用的常量被移除后反射读取注解）。继承 RuntimeException。

## 2. Java 用户如何使用
```java
// 主要由反射注解机制在读取枚举值成员时抛出；应用代码可直接构造记录
throw new EnumConstantNotPresentException(Thread.State.class, "RUNNING");
// getMessage() == "java.lang.Thread$State.RUNNING"（binary name 形式）
```

## 3. 参考来源
Oracle javadoc；javap -public 双侧同构（单构造器 + enumType() + constantName()）。

## 4. 测试用例设计 + 易错规范细节
- 消息模板 `<enumType.getName()>.<constantName>`（**binary name，$ 分隔**，与 Enum.valueOf 的 canonical name 点分形式相反）。
- null constantName 允许：constantName()==null、消息含字面 "null"。
- **null enumType 在构造器内 NPE**（getName() 调用点）：8 侧消息 null、27 侧 helpful（本文件 MODERN=false 分支）。
- svuid = -6046998521960521108（两侧一致）；序列化往返 enumType/constantName/消息保持。
- instanceof RuntimeException；单 (Class,String) 构造器白盒；cause/suppressed/printStackTrace 管道。
- EnumConstantNotPresentExceptionTest.java 现有 PASS 断言：40。
