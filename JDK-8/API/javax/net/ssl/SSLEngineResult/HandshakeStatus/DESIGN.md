# HandshakeStatus 契约测试设计（JDK-8）

## 1. API 功能
javax.net.ssl.SSLEngineResult.HandshakeStatus：握手状态枚举。

## 2. 使用方式
- 纯枚举断言，无网络、无证书。

## 3. 参考来源
- Oracle javadoc（javax.net.ssl.SSLEngineResult 嵌套枚举）。
- 双侧 javap 实测值集。

## 4. 测试用例设计
- 白盒：枚举修饰符与值集精确断言。
- 黑盒：值集精确断言 + valueOf/name round-trip。8 侧 5 值、27 侧 6 值（NEED_UNWRAP_AGAIN 为 DTLS 1.2 添加，版本差异）。
- 正例统一 check/PASS 计数。

## 5. JDK-8 版本差异
- 枚举仅 5 值，无 NEED_UNWRAP_AGAIN（HandshakeStatus 时适用；Status 双侧一致）。
