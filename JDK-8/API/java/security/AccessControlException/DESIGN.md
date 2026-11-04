# AccessControlException — 设计案

## 1. API 功能
访问控制拒绝异常（extends SecurityException）。两个公共构造器：(String)、(String, Permission)；getPermission() 返回被拒绝的权限。

## 2. 使用方式
`throw new AccessControlException("denied", perm)`；或由 AccessController/AccessControlContext.checkPermission 在 JDK-8 真实拒绝时抛出，携带确切的 Permission 对象。

## 3. 参考来源
- javadoc `java.security.AccessControlException`。
- JCK runtime-8b `api/java_security/AccessControlException/`。

## 4. 测试用例设计 + 易错规范细节
- (String) 构造器：getPermission()==null、getCause()==null。
- (String, Permission) 构造器：message 与 permission 均按恒等（==）保存；(String, null) 合法。
- 恰好 2 个公共构造器（无 cause 链构造器）；类型层：RuntimeException ← SecurityException。
- toString 以 `java.security.AccessControlException: msg` 开头；printStackTrace 输出包含类全名。
- initCause 只能一次：第一次成功，第二次 IllegalStateException。
- 序列化往返保留 message 与 permission（按值）。
- 引擎接线（JDK-8）：AccessController.checkPermission 拒绝时 getPermission() 返回传入的同一对象（==），message 含权限名；AccessControlContext.checkPermission 同理。
- 易错点：JDK-27（JEP 486）引擎拒绝时 getPermission()==null —— 与 JDK-8 相反，测试需分叉。
