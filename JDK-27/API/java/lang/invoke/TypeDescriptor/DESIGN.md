# java.lang.invoke.TypeDescriptor — 设计方案

## 1. API 功能
类型描述符门面（interface）：嵌套 `OfField<F>`（字段级：isArray/isPrimitive/componentType）与 `OfMethod<F,M>`（方法级：changeReturnType/changeParameterType/dropParameterTypes/insertParameterTypes/parameterList/parameterArray/returnType/descriptorString）；Class/ClassDesc 实现 OfField，MethodType/MethodTypeDesc 实现 OfMethod。

## 2. Java 用户如何使用
```java
String d = MethodType.methodType(int.class, String.class).descriptorString(); // "(Ljava/lang/String;)I"
String f = String.class.descriptorString(); // "Ljava/lang/String;"
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/invoke/TypeDescriptorTest.java`。
- 通用签名（Signature attribute）与 StackMapTable 处理器以 descriptorString 统一 Class/MethodType 与 constant 版本。

## 4. 测试用例设计 + 自动化实现
白盒：接口本体与两个嵌套接口存在、descriptorString 抽象方法。
黑盒：Class/ClassDesc/MethodType/MethodTypeDesc 四类实现者的 descriptorString 精确值；多态列表 OfField<?> 装下 Class 与 ClassDesc；OfMethod 的 parameterCount 等派生行为。
对应 `TypeDescriptorTest.java` 的 `testBlackBox()`。

## 深化批次（2026-11-05）
新增 `testClassDescriptorMatrix/testMethodTypeDescriptors`（旧 12 断言 → 新 44）：
- Class 描述符全矩阵：8 基本型+void "B S C Z J F D V"、Object[]/String[][]/int[][][] 多维、Map.Entry 嵌套 "Ljava/util/Map$Entry;"（$ 二进制名）、本类/嵌套 Simple 类、ArrayList 无匿名子类场景 "Ljava/util/ArrayList;"。
- MethodType 描述符精确 "()V"/"(I)V"/"(ILjava/lang/String;)Ljava/lang/Object;"/"(JZ)Ljava/lang/String;"/"([I[[Ljava/lang/String;)I" + fromMethodDescriptorString round-trip equals。
- 负例："(Q)V"→IllegalArgumentException "bad signature"；null 描述符→NPE。
- 版本备注：TypeDescriptor/OfField/OfMethod 为 JDK-12+ API，JDK-8 侧无对应目录。