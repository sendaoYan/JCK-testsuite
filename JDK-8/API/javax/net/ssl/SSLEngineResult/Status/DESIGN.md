# Status 契约测试设计（JDK-8）

## 1. API 功能
javax.net.ssl.SSLEngineResult.Status：wrap/unwrap 状态枚举（BUFFER_UNDERFLOW/BUFFER_OVERFLOW/OK/CLOSED）。

## 2. 使用方式
- 纯枚举断言，无网络、无证书。

## 3. 参考来源
- Oracle javadoc（javax.net.ssl.SSLEngineResult 嵌套枚举）。
- 双侧 javap 实测值集。

## 4. 测试用例设计
- 白盒：枚举修饰符与值集精确断言。
- 黑盒：值集断言 + valueOf round-trip。4 值精确；无版本差异。
- 正例统一 check/PASS 计数。
