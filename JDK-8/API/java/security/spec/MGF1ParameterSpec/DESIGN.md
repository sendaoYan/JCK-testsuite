# java.security.spec.MGF1ParameterSpec — 设计案

## 1. API 功能
MGF1 掩码生成函数参数

单构造器（null→NPE "digest algorithm is null"；空串/任意名不校验）；JDK-8 常量 SHA1/SHA224/SHA256/SHA384/SHA512/SHA512_224/SHA512_256，JDK-9+ 追加 SHA3_224/256/384/512 并覆写 toString。
PSSParameterSpec 的 mgfSpec 参数。

## 2. 使用方式
摘要算法名容器

## 3. 参考来源
- Oracle javadoc `java.security.spec.MGF1ParameterSpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
7 常量精确摘要名 + 全非空且互异；null NPE；身份 equals（无 equals 覆盖）；非 Serializable；JDK-27 追加 SHA3 常量与 toString（含算法名）。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
