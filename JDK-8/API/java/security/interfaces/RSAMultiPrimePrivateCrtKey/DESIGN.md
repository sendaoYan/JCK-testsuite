# java.security.interfaces.RSAMultiPrimePrivateCrtKey — 设计案

## 1. API 功能
RSA 多素数 CRT 私钥接口：RSAPrivateCrtKey 的兄弟（都直接继承 RSAPrivateKey），额外 getOtherPrimeInfo() 返回 RSAOtherPrimeInfo[]。serialVersionUID=618058533534628008L。JDK provider 不产出多素数钥。

## 2. 使用方式
仅由第三方 provider 实现或手工构造记录型实现；KeyFactory 消费 RSAMultiPrimePrivateCrtKeySpec 时会折叠为普通 RSAPrivateKey。

## 3. 参考来源
- javadoc `java.security.interfaces.RSAMultiPrimePrivateCrtKey`、`java.security.spec.RSAOtherPrimeInfo`。
- PKCS#1 多素数 CRT 表示。

## 4. 测试用例设计 + 易错规范细节
- 常量：serialVersionUID 精确值。
- 记录型最小实现：八 getter 透传，getOtherPrimeInfo 恒等透传（单元素、空数组、null 三态）。
- 继承格：是 RSAPrivateKey/RSAKey/PrivateKey/Key/Serializable；**不是 RSAPrivateCrtKey 的子接口（兄弟关系）**。
- 平台判别：SunRsaSign 生成私钥是 RSAPrivateCrtKey 但 instanceof RSAMultiPrimePrivateCrtKey 为 false；n 恰分解为两素数。
- RSAOtherPrimeInfo 构造：三参（prime/exponent/coefficient）。
- 易错点：接口层级（兄弟非父子）是最常搞错的点；空 other-prime 表合法、null 由实现约定。
