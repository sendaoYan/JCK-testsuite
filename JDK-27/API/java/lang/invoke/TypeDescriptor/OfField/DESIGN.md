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

## 深化批次（2026-11-05）
新增 `testArrayChains/testPrimitiveMatrix/testClassDescParity/testUserImplementation`（旧 21 断言 → 新 89）：
- 数组链：int[][].class componentType==int[].class、arrayType==int[][][].class；int[][][] 逐级下降；Integer 非基本型且 arrayType==Integer[]；String[][][] 三步 componentType 到 String、arrayType 描述符 "[[[[Ljava/lang/String;"。
- 基本型矩阵：9 基本型+void 全部 isPrimitive/componentType==null/描述符精确/数组类型配对（8 个数组类）。
- ClassDesc 对齐：arrayType().componentType() 描述符下降一维；ofDescriptor("I") isPrimitive 且无 component；"V" 描述符。
- 用户实现：自造 Box implements OfField&lt;Box&gt; 验证契约可自实现（arrayType/componentType 互逆、"[I"/"[[I" 链）。