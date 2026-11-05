# java.lang.constant.ClassDesc — 设计方案

## 1. API 功能
类/接口/基本类型/数组类型的名义描述（interface ConstantDesc + TypeDescriptor.OfField）：`of(fqn)`、`of(pkg,simple)`、`ofInternalName`、`ofDescriptor`；`arrayType()/arrayType(n)`、`nested`、`isArray/isPrimitive/isClassOrInterface`、`componentType`、`packageName`、`displayName`、`descriptorString`、`resolveConstantDesc`。

## 2. Java 用户如何使用
```java
ClassDesc cd = ClassDesc.of("java.lang.String");
if (cd.descriptorString().equals("Ljava/lang/String;")) ...
Class<?> c = cd.resolveConstantDesc(MethodHandles.lookup()); // String.class
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/constant/ClassDesc.java` 与 `jdk.internal.constant.ClassDescImpl`。
- JDK `test/jdk/java/lang/constant/ClassDescTest.java`：descriptor/nested/array 全覆盖。
- 字节码工具（ASM ClassRemapper、javadoc）用 ClassDesc 描述 ldc 可加载常量。

## 4. 测试用例设计 + 自动化实现
白盒：接口、父接口、13 个方法、default/abstract 区分。
黑盒：String 描述符/displayName/packageName/resolve；空包 of("","String")→"LString;"；ofInternalName 等价；基本类型 I；数组 [I/[[I/字符串数组 resolve 到 String[].class；nested $ 链描述符与 displayName；ClassNotFound 负例；斜杠名 IAE、null NPE、尾点 IAE。
对应 `ClassDescTest.java` 的 `testBlackBox()`。

## 5. 契约深化（JDK-27 实测锁定）
- **不做 intern**：`ClassDesc.of("java.lang.String")` 两次调用是不同实例（`!=`），仅 equals/hashCode 按值相等；`ConstantDescs.CD_String` 与 `of()` 结果也非同一实例。
- **`of("int")` 不在工厂期拒绝**：产出 `Lint;`（isClassOrInterface=true、非 primitive），解析时才抛 ClassNotFoundException——工厂只做二进制名形态校验。
- **ofDescriptor 拒绝矩阵（全 IAE）**：`""`、`"(broken"`、`"V;"`、`"L"`、`"Ljava/lang/String"`（缺 `;`）、`"["`、`"II"`；null → NPE。`"(broken"` 消息为 "not a valid reference type descriptor: (broken"。
- **arrayType 边界**：rank 0/负数 → IAE；`V.arrayType()` → IAE（void 不能做组件）；非数组 componentType()==null；`ofDescriptor("[I")` equals `I.arrayType()`。
- **解析闭环**：`of("java.lang.String").arrayType().componentType().resolveConstantDesc(lookup)`==String.class。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
