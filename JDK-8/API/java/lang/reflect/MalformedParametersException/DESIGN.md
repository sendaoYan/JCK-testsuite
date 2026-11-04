# java.lang.reflect.MalformedParametersException — 测试设计案（JDK-8）

## 1. API 功能

- `MalformedParametersException extends RuntimeException`（非受检）：
  - 构造器 `()` 与 `(String message)` —— 1.8 起两个公有构造器齐备（javap 实测）。
  - `MethodParameters` 类文件属性畸形（名字长度/访问标志不一致）时由核心反射抛出。

## 2. 用户如何使用

- 用户代码几乎不构造；读取 `-parameters` 编译产物或字节码增强后的 `Method.getParameters()` 时可能遭遇，按 RuntimeException 捕获诊断。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/reflect/MalformedParametersException.html）。
- OpenJDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/MalformedParametersException.java`、`java/lang/reflect/Parameter.java`（抛出点）。
- OpenJDK 8 回归 `~/git/jdk8u-dev/jdk/test/java/lang/reflect/Parameter/`。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/reflect/MalformedParametersException/`。

## 4. 测试用例设计（映射到 MalformedParametersExceptionTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | RuntimeException/消息构造器（保留原有） | testMalformedParametersException |
| P0 | 层次普查：RuntimeException 直系/Exception/Serializable、2 构造器 | testHierarchyCensus |
| P0 | bare 与带消息 toString 精确两种形态、localized 默认同 message | testBareState |
| P0 | 非受检抛出：带消息 catch RuntimeException、bare catch 精确型 | testUncheckedThrow |
| P1 | Throwable 机制：initCause、addSuppressed、fillInStackTrace、程序化栈 | testThrowableMechanics |
| P1 | 序列化 round-trip：消息/精确 toString/cause 保真 | testSerialization |
| P2 | 上下文健全性：健康方法 getParameterCount/getParameters 不抛 | testContextSanity |
| P2 | printStackTrace 渲染 | testPrinting |

## 易错规范细节（已锁入断言）

- 带消息 toString 精确 `java.lang.reflect.MalformedParametersException: <msg>`；bare 无冒号后缀。
- 与 MalformedParameterizedTypeException 的分工：本异常针对 MethodParameters 属性（参数元数据），后者针对泛型参数化类型（类型元数据）。
- getLocalizedMessage 未覆写，默认返回 getMessage。
