# AllPermission — 设计案

## 1. API 功能
全权限（implies 恒 true）。无参/(name,actions) 构造器 + getName/getActions 哨兵值 + implies + newPermissionCollection。

## 2. 使用方式
`new AllPermission()` 授予一切。

## 3. 参考来源
- javadoc `java.security.AllPermission`。
- JCK runtime-8b `api/java_security/AllPermission/`。

## 4. 测试用例设计 + 易错规范细节
- getName "<all permissions>"、getActions "<all actions>"；implies 任意权限 true。
- equals/hashCode（AllPermission 间相等、与其它权限不等）。
- toString 含类名与哨兵值；newPermissionCollection 空 false、add 后 true。
