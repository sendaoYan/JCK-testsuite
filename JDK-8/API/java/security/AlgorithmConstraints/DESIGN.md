# AlgorithmConstraints — 设计案

## 1. API 功能
算法约束接口（TLS/禁用算法策略消费）：三个 permits 重载——(Set&lt;CryptoPrimitive&gt;, String, AlgorithmParameters)、(Set&lt;CryptoPrimitive&gt;, Key)、(Set&lt;CryptoPrimitive&gt;, String, Key, AlgorithmParameters)，均返回 boolean。

## 2. 使用方式
实现方对 usage 集合 + 算法名/密钥/参数组合给出允许/拒绝；平台把它装进 SSLParameters / TLS 上下文。本测试用记录型/拒绝型/放行型/选择性实现断接口契约。

## 3. 参考来源
- javadoc `java.security.AlgorithmConstraints`。
- javap 双侧（三重载签名完全一致）。

## 4. 测试用例设计 + 易错规范细节
- 结构：恰 3 个 permits 重载（反射按参数类型精确匹配三种签名）、全 boolean 返回、无 default。
- 实参透传：记录型实现按序收到调用方算法名/密钥算法（"s:SHA256withRSA","k:RSA","sk:RSA:RSA" 序列断言）。
- 语义矩阵：deny-all 三重载恒 false（含 null usage set 也可受理）；allow-all 恒 true（EnumSet.allOf 用法集）；选择性实现按前缀/密钥算法独立判定（SHA 允许/MD5 拒绝/RSA 钥允许/DSA 钥拒绝/null 算法按实现拒绝）。
- 真实密钥：RSA-512 与 DSA-1024 生成钥匙作判据。
- 易错点：null 参数（usage/algorithm/specs/key）**接口层不校验**——由实现解释，测试用自定义实现固定语义；不要对平台内置约束（如 tls 客户端约束）做断言，它们是 jdk.tls.disabledAlgorithms 驱动的环境相关行为。
