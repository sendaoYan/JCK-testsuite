# Policy — 设计案

## 1. API 功能
系统安全策略抽象类：getPolicy/setPolicy（全局策略对象）、getPermissions(CodeSource/ProtectionDomain)、implies(ProtectionDomain, Permission)、getInstance(type, params[, provider])、refresh、getType/getProvider/getParameters、嵌套标记接口 Policy.Parameters、静态 UNSUPPORTED_EMPTY_COLLECTION。

## 2. 使用方式
`Policy.getPolicy()`（默认 sun.security.provider.PolicyFile）；子类化覆写 getPermissions/implies；`Policy.getInstance("JavaPolicy", null)`（JDK-8 SUN 提供）；setPolicy 可安装并恢复。

## 3. 参考来源
- javadoc `java.security.Policy`。
- JCK runtime-8b `api/java_security/Policy/`。

## 4. 测试用例设计 + 易错规范细节
- UNSUPPORTED_EMPTY_COLLECTION：implies 任何权限 false；implies(null) NPE；elements 空；add → SecurityException；**JDK-8 上 isReadOnly()==false**（JDK-27 为 true，需分叉）。
- 默认策略：PolicyFile 类名、getType()==null、getProvider()==null、refresh 可调用、对空域 implies false。
- setPolicy/getPolicy 往返（finally 恢复原策略，绝不能污染全局）；自定义策略的默认 implies 委托 getPermissions(CodeSource)。
- getInstance("JavaPolicy", null)：type="JavaPolicy"、provider=SUN、params 透传 null；Bogus 类型 NSAE（消息含类型名）；null type NPE。
- URIParameter 实现 Policy.Parameters；DomainLoadStoreParameter 不是 Policy.Parameters。
- 易错点：JDK-27 setPolicy UOE、JavaPolicy NSAE、implies 不再委托 getPermissions —— 全部需分叉记录。
