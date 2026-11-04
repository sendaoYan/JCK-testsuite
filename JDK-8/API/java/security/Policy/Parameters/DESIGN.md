# Policy.Parameters — 设计案

## 1. API 功能
策略参数标记接口（Policy 的嵌套接口）：零方法、零父接口；作为 Policy.getInstance(type, params) 的参数类型约束。

## 2. 使用方式
`Policy.getInstance("JavaPolicy", new URIParameter(uri))`；唯一公共实现 java.security.URIParameter。

## 3. 参考来源
- javadoc `java.security.Policy.Parameters`、`java.security.URIParameter`。
- JCK runtime-8b `api/java_security/Policy/`。

## 4. 测试用例设计 + 易错规范细节
- 零声明方法（纯标记）；零父接口；非注解；嵌套于 Policy。
- URIParameter 实现之：getURI() 保存构造 URI；equals 恒等（同 URI 的两个实例不相等）；**不可序列化**。
- 匿名实现可实例化（可被声明为参数类型）。
- 负类型断言：DomainLoadStoreParameter、PolicySpi、Policy 本身都不是 Policy.Parameters。
- 易错点：与 Configuration.Parameters 家族（DomainLoadStoreParameter）混淆 —— 它们互不相关。
