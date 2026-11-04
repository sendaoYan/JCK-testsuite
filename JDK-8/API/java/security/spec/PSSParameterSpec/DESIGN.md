# java.security.spec.PSSParameterSpec — 设计案

## 1. API 功能
RSASSA-PSS 签名参数规范

TRAILER_FIELD_BC==1；DEFAULT=（SHA-1,MGF1,MGF1ParameterSpec.SHA1 同引用,20,1）；int 构造=（SHA-1,MGF1,SHA1 单例,saltLen,1）；5 参构造校验：null digest/mgf→NPE、saltLen<0→IAE、trailerField<0→IAE（非 1 值如 2 不拒绝）、null mgfSpec 合法；toString 双侧格式不同（JDK-8 多行 MD:/SaltLength:，JDK-9+ PSSParameterSpec[...]）。
Signature.setParameter 用于 SHA256withRSA 等的 PSS 变体。

## 2. 使用方式
摘要/MGF/盐长/trailer 五元组

## 3. 参考来源
- Oracle javadoc `java.security.spec.PSSParameterSpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
DEFAULT/int 构造默认矩阵（含 mgfParameters==SHA1 单例同引用）；构造校验矩阵与边界（saltLen 0 合法）；trailerField 2 接受的实际行为；身份 equals；版本化 toString 断言。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
