# java.security.spec.InvalidParameterSpecException — 设计案

## 1. API 功能
无效参数规范异常

仅 2 构造（()/String）——无 cause 构造器（与 InvalidKeySpecException 的关键差异）；继承 GeneralSecurityException，Serializable。
AlgorithmParameters.init/getParameterSpec 在规范不匹配时抛出。

## 2. 使用方式
ParameterSpec 族受检异常

## 3. 参考来源
- Oracle javadoc `java.security.spec.InvalidParameterSpecException`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
构造器数==2 的契约断言；cause 只能经 initCause 附加且一次性；suppressed/stackTrace/序列化往返；toString/getLocalizedMessage。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
