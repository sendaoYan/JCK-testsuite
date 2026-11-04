# com.sun.security.auth.module.UnixSystem 测试设计案

## 1. API 功能
`UnixSystem` 提供当前用户 Unix UID/GID/组信息。JDK-27 javap 实测公开方法（**无 getHomeDirectory/getShell**）：
- 构造器 `UnixSystem()`（JDK-27 改用 Foreign Function & Memory API 调 `getuid/getgid/getgroups/getpwuid_r`，不再依赖 `libjaas`）。
- `String getUsername()`；`long getUid()`；`long getGid()`；`long[] getGroups()`（防御性拷贝）。

## 2. Java 用户如何使用
```java
UnixSystem u = new UnixSystem();
String user = u.getUsername();
long uid = u.getUid();
long gid = u.getGid();
long[] groups = u.getGroups();
```

## 3. 开源代码实际用法调研
- 实现基准：`~/git/jdk25u-dev/src/jdk.security.auth/share/classes/com/sun/security/auth/module/UnixSystem.java`（JDK-27 已迁移到 FFM，javap 字段可见 `Linker/SymbolLookup`）。
- 关联用法：`module/UnixLoginModule.java` 构造 `UnixSystem` 生成 `UnixPrincipal/UnixNumericUserPrincipal/UnixNumericGroupPrincipal` 加入 `Subject`。
（网络不可用，以 JDK 源码 + JDK-27 src.zip 为权威来源。）

## 4. 测试用例设计 + 自动化实现
白盒断言在 `testWhiteBox()`：类非接口、四类访问方法齐全。
黑盒断言在 `testBlackBox()`：
- Unix 上：`getUsername()` 非空；`getUid()/getGid() >= 0`；`getGroups()` 非空且每个 gid >= 0。
- `getGroups()` 两次调用返回不同数组对象（防御性拷贝）且长度一致。
- Windows 上降级：构造抛异常（不硬编码具体类型，`catch Throwable`）。
