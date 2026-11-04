# java.lang.reflect.MalformedParameterizedTypeException — 测试设计案（JDK-8）

## 1. API 功能

- `MalformedParameterizedTypeException extends RuntimeException`（非受检）：
  - **JDK-8 只有无参构造器**（javap 实测 getConstructors().length == 1）；`(String)` 消息构造器是 JDK 21 才加入（版本差异断言锁定）。
  - 消息恒 null（JDK-8 无消息通道），cause 需 initCause 后补。

## 2. 用户如何使用

- 泛型签名语义非法（语法错误抛 GenericSignatureFormatError，语义层面的参数化类型畸形抛本异常）时由核心反射抛出；用户代码只捕获，几乎不构造。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/reflect/MalformedParameterizedTypeException.html）。
- OpenJDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/MalformedParameterizedTypeException.java`。
- OpenJDK 8 回归 `~/git/jdk8u-dev/jdk/test/java/lang/reflect/`。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/reflect/MalformedParameterizedTypeException/`（注：8b 另有 MalformedParamTypeException 一节易混淆）。

## 4. 测试用例设计（映射到 MalformedParameterizedTypeExceptionTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | RuntimeException instanceof（保留原有） | testMalformedParameterizedTypeException |
| P0 | 层次普查：RuntimeException 直系/Exception/Serializable、恰 1 个公有构造器（8 形态） | testHierarchyCensus |
| P0 | bare 状态：消息/本地消息/cause null、toString 精确 FQCN | testBareState |
| P0 | 非受检抛出：catch RuntimeException 与精确型两条路径 | testUncheckedThrow |
| P1 | Throwable 机制：initCause 接线、addSuppressed、fillInStackTrace==this、setStackTrace 程序化栈 | testThrowableMechanics |
| P1 | 序列化 round-trip：类型/null 消息/cause/toString 精确保留 | testSerialization |
| P2 | printStackTrace 渲染类型与 cause | testPrinting |

## 易错规范细节（已锁入断言）

- JDK-8 → JDK-21 差异：消息构造器从无到有（getConstructors() 1 → 2），双侧各自断言。
- bare toString 精确等于 `java.lang.reflect.MalformedParameterizedTypeException`（无消息后缀）。
- 与 GenericSignatureFormatError 的分工：签名字法错 → Error 家族；参数化类型语义畸形 → 本 RuntimeException。
