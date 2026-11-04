# ProtectionDomain — 设计案

## 1. API 功能
保护域。2-arg 构造器(CodeSource, PermissionCollection)、4-arg 构造器(CodeSource, PermissionCollection, ClassLoader, Principal[]) + getCodeSource/getClassLoader/getPrincipals/getPermissions/implies(Permission)/toString。

## 2. 使用方式
`new ProtectionDomain(new CodeSource(url, null), perms)`。

## 3. 参考来源
- javadoc `java.security.ProtectionDomain`。
- JCK runtime-8b `api/java_security/ProtectionDomain/`。

## 4. 测试用例设计 + 易错规范细节
- 2-arg：getClassLoader null、getPrincipals 空数组、getCodeSource/getPermissions 返回传入值。
- implies(Permission) 由权限集合驱动（含 AllPermission 时任意权限 true）。
- 4-arg：getClassLoader/getPrincipals 返回传入值。
- null 语义：null CodeSource→getCodeSource null；null PermissionCollection→getPermissions null（均不抛异常）。
- 易错点：构造器对 null 宽容（不 NPE），与多数安全类不同。
