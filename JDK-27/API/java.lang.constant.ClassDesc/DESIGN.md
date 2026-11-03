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
