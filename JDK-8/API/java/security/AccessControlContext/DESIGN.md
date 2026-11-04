# AccessControlContext — 设计案

## 1. API 功能
访问控制上下文。构造器(ProtectionDomain[]) 与 (AccessControlContext, DomainCombiner) + getDomainCombiner + equals/hashCode + checkPermission。

## 2. 使用方式
`new AccessControlContext(new ProtectionDomain[0])`。

## 3. 参考来源
- javadoc `java.security.AccessControlContext`。
- JCK runtime-8b `api/java_security/AccessControlContext/`。

## 4. 测试用例设计 + 易错规范细节
- getDomainCombiner 默认 null；2-arg 构造器传 null combiner 亦 null。
- equals/hashCode 值语义：相同空域上下文相等。
- checkPermission(null) → NPE（JDK-8；JDK-27 改抛 AccessControlException，需分叉）。
- 易错点：JDK-27 起 checkPermission(任意权限) 直接抛 AccessControlException（默认策略拒绝）。
