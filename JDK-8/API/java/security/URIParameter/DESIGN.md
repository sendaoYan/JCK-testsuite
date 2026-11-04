# URIParameter — 设计案

## 1. API 功能
Policy/Configuration 的 URI 参数：构造 (URI)（null → NPE）+ getURI()。**非 final**，implements Policy.Parameters 与 javax.security.auth.login.Configuration.Parameters，但**不是** AlgorithmParameterSpec。

## 2. 使用方式
`Policy.getInstance("JavaPolicy", new URIParameter(uri))` / `Configuration.getInstance(..., URIParameter)` 指向策略文件位置。

## 3. 参考来源
- javadoc `java.security.URIParameter`。
- 双侧探针（非 final、identity equals）。

## 4. 测试用例设计 + 易错规范细节
- 构造：null → NPE；URI 各形态全收（层次/不透明 mailto/相对/空 URI/带 fragment），fragment 保留。
- getter：getURI 返回构造实例（身份保持），重复调用同实例。
- 接口归属：Policy.Parameters + Configuration.Parameters 为 true；**AlgorithmParameterSpec 为 false**（与 spec/ 家族区分）。
- equals/hashCode：identity 语义——同 URI 两个参数对象不等；自反、equals(null)/异类型 false；**类非 final**（无 final 性规范，实测）。
- 结构：恰 1 个 getter；非 Serializable。
- 易错点：别把它当 AlgorithmParameterSpec 传给 AlgorithmParameters（类型不通）；equals 未覆写。
