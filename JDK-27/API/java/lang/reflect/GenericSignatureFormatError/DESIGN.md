# java.lang.reflect.GenericSignatureFormatError — 测试设计（JDK-27）

## 1. API 功能
`GenericSignatureFormatError extends ClassFormatError`（Error/LinkageError 层）：
- 构造器：无参、`(String message)`。
- 触发：反射惰性解析 `Signature` 属性遇到语法非法时抛出（`Class.getGenericSuperclass/getTypeParameters/getGenericInterfaces` 等）。

## 2. Java 用户如何使用
正常业务代码不直接构造；类文件被织入/改写（AOP、字节码增强）后签名损坏时，在首次泛型反射处爆出。诊断时按 Error 捕获并读取 message（含 "Signature Parse error" 与剩余输入）。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Generics/MalformedSignatureTest.java`：以 Class-File API 注入 `BASIC_BAD_SIGNATURE_TEXT` 后断言 `getGenericSuperclass` 抛 GSFE（本测试同构，引用其手法）。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/sun/reflect/generics/parser/SignatureParser.java`：抛出点与消息格式权威。
- ASM/ByteBuddy 增强链路对签名损坏的上报（同构场景 1/2 的触发方式）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`GenericSignatureFormatErrorTest.testBlackBox`）：
1. 真实触发：用 `java.lang.classfile`（`ClassFile.of().transformClass` + `SignatureAttribute.of(Utf8Entry)`）把自身嵌套类 `Generic extends ArrayList<String>` 的类级 Signature 换成 "i_am_not_a_signature"，自定义 loader 定义后：类可加载，`getGenericSuperclass` 抛 GSFE（消息含 "Signature"）。
2. 其他惰性泛型入口同样抛：`getTypeParameters`、`getGenericInterfaces`。
3. 对照组：原始类签名正常解析为 `ArrayList<String>` PT。
4. 构造器面：裸构造 null 消息、消息构造往返、toString、无 cause。
5. Error 层语义：可按 `java.lang.Error` 捕获。
白盒保留原有层次断言。产出：`GenericSignatureFormatErrorTest.java`（16 断言通过）。

## 深化批次（2026-11-05）

新增方法（断言 20 → 38）：

| 测试点 | 方法 |
|---|---|
| LinkageError 层普查：!RuntimeException/!Exception 静态断言、Error/LinkageError 可捕获、RuntimeException catch 不命中 | testLinkageCensus |
| 裸/带消息 toString 精确、initCause 允许（cause 未设）、suppressed | testConstructorSurfaceExact |
| 序列化 round-trip：消息/cause 保真 | testSerializationRoundTrip |
| 坏签名类上非泛型访问器（superclass/fields/methods/kind）保持健康 | testHealthyAccessorsOnMalformedClass |

JDK-8 侧同批测试用 DataOutputStream 手写 v52 字节码触发，与本侧 classfile API 注入互为印证；两侧消息片段一致（"Signature Parse error"）。
