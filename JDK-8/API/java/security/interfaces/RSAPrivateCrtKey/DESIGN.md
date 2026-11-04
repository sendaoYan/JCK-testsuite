# java.security.interfaces.RSAPrivateCrtKey — 设计案

## 1. API 功能
RSA CRT 私钥接口：七个 getter（p、q、dP、dQ、qInv、e、d）+ 继承 RSAPrivateKey/RSAKey/PrivateKey。serialVersionUID=-5682214253527700368L。

## 2. 使用方式
SunRsaSign 生成的私钥即 CRT 实现；KeyFactory 用 RSAPrivateCrtKeySpec 八参还原。

## 3. 参考来源
- javadoc `java.security.interfaces.RSAPrivateCrtKey`（CRT 字段定义）。
- PKCS#1 CRT 方程：dP=d mod (p-1)、dQ=d mod (q-1)、qInv=q⁻¹ mod p、n=pq。

## 4. 测试用例设计 + 易错规范细节
- 常量：serialVersionUID 精确值。
- 记录型最小实现：七个 CRT getter 全透传（含完整 Key 面）。
- 继承格：→ RSAPrivateKey → RSAKey → PrivateKey → Key、Serializable。
- 真实数学闭环（RSA-2048）：p/q 各 1024 位素数且互异；n=pq；dP/dQ/qInv 三方程逐一成立；e=65537；PKCS#8。
- RSAPrivateCrtKeySpec 八参 round-trip：全字段还原且 equals。
- 签名闭环：CRT 钥 SHA256withRSA 签名、公钥验签成功。
- 易错点：qInv = q modInverse p（顺序不能反）；CRT 字段间方程是确定性断言。
