# java.lang.reflect.GenericSignatureFormatError — 测试设计案（JDK-8）

## 1. API 功能

- `GenericSignatureFormatError extends ClassFormatError extends LinkageError extends Error`：
  - 构造器 `()` 与 `(String message)`（javap 实测恰 2 个）。
  - 触发：反射惰性解析类/方法/字段的 `Signature` 属性遇到字法非法时抛出（`getGenericSuperclass/getTypeParameters/getGenericInterfaces` 等）。

## 2. 用户如何使用

- 用户代码不构造；AOP/字节码增强损坏签名后，首次泛型反射处爆出。按 Error/LinkageError 捕获并读 message（含 "Signature Parse error" 与剩余输入）。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/reflect/GenericSignatureFormatError.html）。
- OpenJDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/sun/reflect/generics/parser/SignatureParser.java`（抛出点与消息格式）。
- OpenJDK 8 回归 `~/git/jdk8u-dev/jdk/test/java/lang/reflect/Generics/MalformedSignatureTest.java`（同构触发手法，代码自写）。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/reflect/GenericSignatureFormatError/`。

## 4. 测试用例设计（映射到 GenericSignatureFormatErrorTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 构造器/层次（保留原有） | testGenericSignatureFormatError |
| P0 | 层次普查：ClassFormatError 直系、LinkageError/Error 可赋值、!RuntimeException/!Exception 静态断言、Serializable、2 构造器 | testHierarchyCensus |
| P0 | 真实触发：手写 v52 最小类文件（类属性 Signature="i_am_not_a_signature"）经自定义 loader 定义 → 类可加载，getGenericSuperclass 抛 GSFE（消息含 "Signature Parse error"，8/27 一致） | testRealTrigger |
| P0 | 其他惰性入口同样抛：getTypeParameters/getGenericInterfaces；非泛型入口（getSuperclass/fields/methods/kind）同类上健康 | testRealTrigger |
| P0 | 对照组：`static class Generic extends ArrayList<String>` 解析出 ParameterizedType<ArrayList, String> | testRealTrigger |
| P0 | Error 层语义：可按 Error/LinkageError 捕获、绝不被 catch(RuntimeException) 命中 | testErrorSemantics |
| P1 | 构造器状态：bare 消息 null/toString 精确、带消息 toString 精确、initCause 允许、suppressed | testConstructorState |
| P1 | 序列化 round-trip：消息/cause 保真 | testSerialization |
| P2 | printStackTrace 渲染 | testPrinting |

## 易错规范细节（已锁入断言）

- GSFE 是 Error：`!RuntimeException.class.isAssignableFrom(GSFE)` 静态断言 + try/catch 行为断言双保险。
- 惰性性：坏签名类可正常加载，首次泛型访问才抛；同一坏类上非泛型访问不受影响。
- 触发用类文件用 DataOutputStream 手工发射（v52、常量池 7 项、单类属性 Signature），JDK-8 与 JDK-27 加载行为一致；27 侧另一份测试用 java.lang.classfile API 注入，两法互为印证。
