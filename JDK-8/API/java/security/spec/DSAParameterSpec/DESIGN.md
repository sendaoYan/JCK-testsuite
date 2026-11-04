# java.security.spec.DSAParameterSpec — 设计案

## 1. API 功能
DSA 参数规范

3 参构造（全 null 合法），getter 原引用返回；实现 AlgorithmParameterSpec；非 Serializable、无 equals 覆盖。
AlgorithmParameters.getInstance("DSA").init(spec) → getParameterSpec(DSAParameterSpec.class) 往返；DSAParameterSpec 也是 DSAPublicKeySpec/DSAPrivateKeySpec 的参数部分。

## 2. 使用方式
p/q/g 三参数容器

## 3. 参考来源
- Oracle javadoc `java.security.spec.DSAParameterSpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
getter 同引用；null 构造→getter null；身份 equals/hashCode 稳定；引擎闭环：init(spec)→getParameterSpec 值还原 + getEncoded 非 null。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
