# java.lang.constant.MethodTypeDesc — 设计方案

## 1. API 功能
方法类型描述（interface ConstantDesc + OfMethod）：`ofDescriptor(String)`、`of(ret, params...)`、`of(ret, List)`；访问器 returnType/parameterCount/parameterType/parameterList/parameterArray；变换 changeReturnType/changeParameterType/dropParameterTypes/insertParameterTypes；descriptorString/displayDescriptor/resolveConstantDesc。

## 2. Java 用户如何使用
```java
MethodTypeDesc mtd = MethodTypeDesc.of(CD_String, CD_int, CD_boolean);
mtd.descriptorString(); // "(IZ)Ljava/lang/String;"
mtd.resolveConstantDesc(lookup); // MethodType
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/constant/MethodTypeDesc.java` + `MethodTypeDescImpl`。
- MethodHandleDesc.lookupDescriptor 即本类型 descriptorString；javadoc/asm 常用。
- JDK `test/jdk/java/lang/constant/MethodTypeDescTest.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口、两个父接口、14 方法、displayDescriptor default。
黑盒：(IZ)String 全访问器与 display "(int,boolean)String"；List 重载等价；()V 与 MTD_void 等价；ofDescriptor 往返；resolve 与 MethodType 同一；四种变换的精确描述符且原对象不变（值语义）；equals/hashCode；空描述符/未闭合描述符 IAE。
对应 `MethodTypeDescTest.java` 的 `testBlackBox()`。

## 5. 契约深化（JDK-27 实测锁定）
- **畸形描述符矩阵（全 IAE）**：`"VI"`（缺括号）、`"()X"`（非法类型字符）、`"(I;)V"`、`"Ljava/lang/String"`（缺 ;）、`"(IX)V"`、`"()"`、`"(V)V"`（V 不能做参数）、`"I"`；null → NPE。
- of(null)/of(ret, null 数组) → NPE；drop/insert 越界 → IndexOutOfBoundsException；changeParameterType/parameterType 越界 → ArrayIndexOutOfBoundsException。
- **与 MethodType 全等**：6 个描述符表驱动断言 `MethodType.fromMethodDescriptorString(d,null).toMethodDescriptorString()==d` 且 MTD resolve 后同样成立。
- drop(0,0) no-op；insert 尾部追加；displayDescriptor 数组形如 `(int[])void`。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
