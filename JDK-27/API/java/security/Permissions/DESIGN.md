# Permissions — 设计案

## 1. API 功能
PermissionCollection 实现，聚合多个权限类。add/implies/elements。

## 2. 使用方式
`Permissions` → `add(new BasicPermission("a.b"))` → `implies(...)`；`elements()` 枚举。

## 3. 参考来源
- javadoc `java.security.Permissions`。
- JCK runtime-8b `api/java_security/Permissions/`。

## 4. 测试用例设计 + 易错规范细节
- 继承 PermissionCollection。
- add/implies：空集合 implies false；添加后 true；添加 AllPermission 后 implies 任意 true。
- 多权限类混合 elements 枚举齐全；相同权限去重（elements 数量 1）。
- null：add(null)→NPE、implies(null)→NPE。
