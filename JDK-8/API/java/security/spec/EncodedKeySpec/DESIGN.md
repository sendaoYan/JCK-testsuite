# java.security.spec.EncodedKeySpec — 设计案

## 1. API 功能
编码密钥规范的抽象基类

抽象基类，持有编码字节的私有拷贝，定义 getEncoded（防御拷贝）与抽象 getFormat；子类 PKCS8EncodedKeySpec/X509EncodedKeySpec 指定格式名。JDK-9+ 增加 (byte[],String algorithm) 受保护构造器与 getAlgorithm()。
KeyFactory 以 X509EncodedKeySpec/PKCS8EncodedKeySpec 包装 DER 编码往返还原密钥。

## 2. 使用方式
以编码字节承载密钥

## 3. 参考来源
- Oracle javadoc `java.security.spec.EncodedKeySpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
构造 null→NPE；ctor/getEncoded 双重防御拷贝（改入参/改返回值均隔离）；getEncoded 每次新数组；自定义子类（getFormat 覆写）验证扩展性；getFormat 抽象/getEncoded 具体（反射断言）；无 equals 覆盖→身份语义；非 Serializable。JDK-27 追加算法名通道 + null 算法 NPE。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
