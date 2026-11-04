# java.lang.invoke.TypeDescriptor.OfField — 设计方案

## 1. API 功能
字段类型描述符（TypeDescriptor 嵌套接口）：`isArray()/isPrimitive()/componentType()`（default 实现：基本类型与 Object 描述符短路）。

## 2. Java 用户如何使用
```java
OfField<Class<?>> f = (OfField<Class<?>>) (Object) String.class;
if (f.isArray()) { OfField<Class<?>> c = f.componentType(); }
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/TypeDescriptorTest.java`（OfField 部分）。
- Class 与 ClassDesc 共享该接口使反射与常量池 API 可互换（javadoc 给出对照表）。

## 4. 测试用例设计 + 自动化实现
白盒：嵌套接口存在、三方法声明、componentType default。
黑盒：Class 实现 OfField（int[].class isArray、componentType==int.class、String.class 非）；ClassDesc 同套断言（CD_int isPrimitive、CD_String 数组 componentType 回 CD_String）；基本类型 componentType==null/empty；default 与实现一致。
对应 `OfFieldTest.java` 的 `testBlackBox()`。