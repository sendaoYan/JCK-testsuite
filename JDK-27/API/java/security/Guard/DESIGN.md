# java.security.Guard 黑盒行为测试设计

## 1. API 功能
- 接口 `Guard`：`checkGuard(Object)`（允许则返回，拒绝则抛 SecurityException）。

## 2. Java 用户如何使用
实现 Guard 控制对象访问：`checkGuard` 内做权限判断，拒绝时 `throw new SecurityException(...)`；JDK 中 `java.security.Permission` 即实现了 Guard。

## 3. 开源代码实际用法调研
- `src/java.base/share/classes/java/security/Permission.java`：Permission implements Guard。
- `src/java.base/share/classes/java/security/GuardedObject.java`：调用 `guard.checkGuard(object)`。

## 4. 测试用例设计 + 自动化实现
- `AllPermission` instanceof Guard（结构契约）。
- 自定义允许 Guard 不抛异常；自定义拒绝 Guard 抛 SecurityException 且 message 精确。
- 白盒：接口 + checkGuard 方法，无父接口。
- 注：JDK-27 移除 SecurityManager 后，Permission.checkGuard 统一抛 "checking permissions is not supported"，故用自定义 Guard 验证“允许/拒绝”契约。

## 5. 追加契约维度（深化批）
- throws 子句显式声明 SecurityException（unchecked 但声明，反射恰 1 项）。
- 实参身份透传（含 null 目标）；非 Security 的 unchecked 异常原样传播。
- Permission.checkGuard 无 SM：抛**普通 SecurityException**（非 AccessControlException，JEP 486 路径，实测锁定）。
- GuardedObject 联动：每次 getObject 都咨询 guard（计数）。
