# DomainLoadStoreParameter — 设计案

## 1. API 功能
KeyStore 域加载参数（DKS 域 keystores）：构造 (URI, Map&lt;String, ProtectionParameter&gt;) + getConfiguration/getProtectionParams/getProtectionParameter。final 类，implements KeyStore.LoadStoreParameter。

## 2. 使用方式
`KeyStore.getInstance("DKS")` + load(DomainLoadStoreParameter)；URI 指向域配置文件，Map 为各 keystor 条目名→保护参数。

## 3. 参考来源
- javadoc `java.security.DomainLoadStoreParameter`。
- 双侧探针实测（NPE/UOE/快照语义一致）。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵：null URI → NPE；null Map → NPE；空 Map 合法；构造后快照（源 Map 再增删不可见——**防御性拷贝**）。
- getter：getConfiguration 返回构造 URI（身份保持）；getProtectionParameter 恒 null（规范固定）；getProtectionParams 值身份保持、恰 3 个 getter、类 final。
- 视图不可写：put/remove/putAll/clear 全部 UnsupportedOperationException；拒绝后视图不变。
- equals/hashCode：无值语义（同内容不同实例不等）；自反恒真、hashCode 稳定。
- 易错点：返回的 Map 是**不可修改快照**而非源 Map 的活视图——两个方向（不可写 + 快照）都要断。
