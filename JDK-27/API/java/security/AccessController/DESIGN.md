# AccessController — 设计案

## 1. API 功能
访问控制入口（静态方法）。getContext + doPrivileged(PrivilegedAction[, AccessControlContext]) + doPrivileged(PrivilegedExceptionAction) + checkPermission。

## 2. 使用方式
`AccessController.doPrivileged(() -> "result")`；无需 SecurityManager 即可测试 doPrivileged 与 getContext。

## 3. 参考来源
- javadoc `java.security.AccessController`。
- JCK runtime-8b `api/java_security/AccessController/`。

## 4. 测试用例设计 + 易错规范细节
- getContext 非空；doPrivileged(action) 返回结果。
- doPrivileged(action, ctx) 返回结果。
- doPrivileged(exceptionAction) 成功返回；抛异常时包成 PrivilegedActionException（getException 取原异常）。
- checkPermission(null) → NPE（JDK-8；JDK-27 已改为 AccessControlException，需分叉）。
- 易错点：JDK-27 起该类 deprecated for removal，checkPermission 语义改变。
