# UnresolvedPermission — 设计案

## 1. API 功能
未解析权限（持有底层权限的类名/名称/动作/证书）。构造器 + getUnresolvedType/Name/Actions + getActions(恒 "") + implies(恒 false) + newPermissionCollection。

## 2. 使用方式
`new UnresolvedPermission("java.io.FilePermission", "read", "read", null)`。

## 3. 参考来源
- javadoc `java.security.UnresolvedPermission`。
- JCK runtime-8b `api/java_security/UnresolvedPermission/`。

## 4. 测试用例设计 + 易错规范细节
- getters：getUnresolvedType=type、getUnresolvedName=name、getUnresolvedActions=actions、getActions=""、getName()==type（构造器 super(type)）。
- null type→NPE；null name/actions 合法；certs 防御性拷贝。
- implies 恒 false（含集合 add 后仍 false）；equals/hashCode；toString；newPermissionCollection 非空。
