# com.sun.security.auth.module.NTSystem 测试设计案

## 1. API 功能
`NTSystem` 提供当前用户 Windows NT 安全信息。JDK-27 javap 实测公开方法：
- 构造器 `NTSystem()`。
- `String getName()/getDomain()/getDomainSID()/getUserSID()/getPrimaryGroupID()`。
- `String[] getGroupIDs()`（防御性拷贝）。
- `long getImpersonationToken()`。

## 2. Java 用户如何使用
Windows 上：`new NTSystem()` 后逐项读取账户/SID/组信息。
非 Windows 上：JDK-27 只为 Windows 编译 `jaas` 原生库（`nt.c`），构造会抛 `UnsatisfiedLinkError`（不是返回 null）。

## 3. 开源代码实际用法调研
- 实现基准：`~/git/jdk25u-dev/src/jdk.security.auth/share/classes/com/sun/security/auth/module/NTSystem.java`。
- 原生实现仅在 `src/jdk.security.auth/windows/native/libjaas/nt.c`；Unix 侧无对应实现。
（网络不可用，以 JDK 源码为权威来源；brief 中「非 Windows 返回 null」经实测不成立，已修正为断 UnsatisfiedLinkError。）

## 4. 测试用例设计 + 自动化实现
白盒断言在 `testWhiteBox()`：类非接口、七类访问方法齐全。
黑盒断言在 `testBlackBox()`：
- Windows：构造后访问全部 accessor 不抛（值可为 null，因未加域）。
- 非 Windows：构造抛 `UnsatisfiedLinkError`（降级通过，跨平台一致）。
