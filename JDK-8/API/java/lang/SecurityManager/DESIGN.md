# java.lang.SecurityManager 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。无 SM 安装断言（getSecurityManager()==null）；实例化后按
> 「授予通过 / 拒绝抛 ACE 且携带被拒 Permission」二分实测各 check*；getClassContext 真实调用链；
> null 参数精确消息。全程**不调用 System.setSecurityManager**（红线），拒绝类检查全部用
> 绝对路径/未授予权限构造。

## 1. API 功能

- **类**：`java.lang.SecurityManager`（JDK-8 非 deprecated、非 final、单公开构造器）。
- **查询**：`getSecurityContext()`（默认返回 `AccessControlContext`）、`getClassContext()`（真实调用链，首帧为 SM 子类自身）、`getThreadGroup()`。
- **检查族**：`checkPermission/checkExit/checkCreateClassLoader/checkRead/checkWrite/checkDelete/checkConnect/checkListen/checkLink/checkExec/checkPackageAccess/checkPropertyAccess/checkAccess/checkExit…`。

## 2. Java 用户如何使用

- 用户几乎不直接 `new`；通常由 `System.getSecurityManager()` 拿到已安装实例后委托检查。
- 子类化并覆写 `checkXxx` 定制策略（本测试用 `CtxSM extends SecurityManager` 观测 `getClassContext`）。

```java
SecurityManager sm = new SecurityManager();
sm.checkRead("/etc/passwd");  // 无策略授权时抛 AccessControlException（携带 FilePermission）
```

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/SecurityManager.html`：各 check* 对应的 Permission 与默认实现）。
- **OpenJDK 8u-dev 源码** `jdk/src/share/classes/java/lang/SecurityManager.java`。
- **实测 JDK-8 运行时**（Temurin 8u372，探针 /tmp/lcF）：
  - `ProtectionDomain.getPermissions()` 实测 app classloader 授予本 codebase `RuntimePermission("exitVM")`、`RuntimePermission("stopThread")`、`FilePermission("<codebase>/-","read")`；
  - 默认策略额外授予标准 PropertyPermission 读 → `checkPropertyAccess("java.version")`、`checkPackageAccess("java.lang")`、`checkAccess(Thread)` 等不抛；
  - 其余检查抛 `AccessControlException` 且 `getPermission()` 携带精确被拒权限。

## 4. 测试用例设计（映射到 SecurityManagerTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 非 deprecated/非 final/单构造器/getSecurityManager()==null | testWhiteBox |
| P0 | 11 项不抛检查（exitVM/stopThread/属性读/包访问/线程访问） | testNoThrowChecks |
| P0 | 13 项拒绝检查抛 ACE 且 getPermission 携带精确权限（FilePermission 路径/read、createClassLoader、loadLibrary.libfoo.so…） | testDeniedChecks |
| P1 | getSecurityContext：非 ACC 上下文 → SE；ACC 上下文 → ACE；已授予权限 + ctx 通过 | testContextChecks |
| P1 | null 参数：checkPermission/checkRead/checkWrite/checkDelete 的 NPE 精确消息（`permission can't be null`/`name can't be null`…） | testNullArguments |
| P1 | getClassContext[0] == SM 子类自身 | testClassContext |

## 关键规范细节（易错，已锁入断言）

- **app codebase 隐式授予**：jtreg/应用 classloader 会授予自身 codebase `exitVM`/`stopThread`/codebase 读——**拒绝类检查必须用 codebase 之外的绝对路径**（如 `/jck/lcF/abs/somefile`）才能稳定抛 ACE。
- **`checkPropertyAccess` 只对默认策略授予的标准属性集不抛**（`java.version` 过、`java.home` 不过）——不要假设全部系统属性可读。
- **抛出的多是 `AccessControlException`（SecurityManager 子类语义）而非裸 SE**，且 `getPermission()` 可精确断言；ACE 消息前缀 `access denied (`。
- **null 参数消息（JDK-8 精确）**：`permission can't be null`、`name can't be null`、`thread can't be null`、`thread group can't be null`。
- **`getClassContext()[0]` 是 SM 子类自身**（默认包下形如 `SecurityManagerTest$CtxSM`）。

## 注意事项

- **绝不 `System.setSecurityManager`**（红线：27 侧已移除；这里只断言 `getSecurityManager()==null`）。
- 拒绝检查不可依赖 codebase 相对路径；全部用 `/jck/lcF/abs/...` 形式绝对路径。
