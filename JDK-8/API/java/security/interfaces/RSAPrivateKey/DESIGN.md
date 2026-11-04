# java.security.interfaces.RSAPrivateKey — 设计案

## 1. API 功能
RSA 私钥接口：getPrivateExponent()，继承 RSAKey 与 PrivateKey。serialVersionUID=5187144804936595022L。

## 2. 使用方式
生成后读 d 与 n；KeyFactory 用 RSAPrivateKeySpec(n, d) 最小规格还原（丢 CRT 信息）。

## 3. 参考来源
- javadoc `java.security.interfaces.RSAPrivateKey`。
- PKCS#1：d·e ≡ 1 (mod λ(n))，λ = lcm(p-1, q-1)。

## 4. 测试用例设计 + 易错规范细节
- 常量：serialVersionUID 精确值。
- 记录型最小实现：d/模数/算法/格式/编码透传。
- 继承格：RSAKey + PrivateKey → Key、Serializable。
- 真实闭环（RSA-2048）：d ∈ (0, n)；**d·e ≡ 1 (mod lcm(p-1,q-1))（注意是 λ 而非 φ：JDK-27 SunRsaSign 用 λ，φ 在 JDK-27 不必成立）**；(n,d) spec round-trip 值相同但**非 CRT 实例、与原 CRT 钥编码不等**；序列化（KeyRep）round-trip equals。
- equals/hashCode：自反、公私不等、稳定。
- 易错点：用 (p-1)(q-1) 断言 d·e 会偶发失败（两素数高度复合时 d 按 λ 求逆）；从 (n,d) 还原的钥 equals 原钥为 false 是契约（编码不同）。
