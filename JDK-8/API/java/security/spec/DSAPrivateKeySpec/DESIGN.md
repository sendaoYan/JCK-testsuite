# java.security.spec.DSAPrivateKeySpec — 设计案

## 1. API 功能
DSA 私钥规范

4 参构造（全 null 合法），实现 KeySpec；无 equals 覆盖、非 Serializable。
KeyFactory("DSA").getKeySpec(priv, DSAPrivateKeySpec.class) 产出；generatePrivate 消费。

## 2. 使用方式
x/p/q/g 四参数容器

## 3. 参考来源
- Oracle javadoc `java.security.spec.DSAPrivateKeySpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
getter 同引用；null 构造；身份 equals；KeyFactory 闭环：DSA-1024 私钥→getKeySpec 全字段非 null、p.bitLength==1024、generatePrivate equals 还原。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
