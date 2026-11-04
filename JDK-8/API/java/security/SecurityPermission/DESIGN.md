# SecurityPermission — 设计案

## 1. API 功能
安全相关权限（extends BasicPermission）。name/name+actions 构造器。

## 2. 使用方式
`new SecurityPermission("getPolicy")`。

## 3. 参考来源
- javadoc `java.security.SecurityPermission`。
- JCK runtime-8b `api/java_security/SecurityPermission/`。

## 4. 测试用例设计 + 易错规范细节
- 继承 BasicPermission；getName/getActions(空)。
- null/空名→NPE/IAE。
- implies 精确与通配；equals/hashCode。
