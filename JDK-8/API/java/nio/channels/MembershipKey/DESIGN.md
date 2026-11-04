# java.nio.channels.MembershipKey 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。生命周期（valid/drop/通道关闭）、getter、block/unblock 语义
> 与源过滤的真实数据面验证。

## 1. API 提供了哪些功能

- 抽象类（protected 构造器）：`isValid()`、`drop()`（幂等 no-op）、`block(InetAddress)`/
  `unblock(InetAddress)`（返回 this）、`channel()/group()/networkInterface()/sourceAddress()`。

## 2. Java 用户如何使用

- join 返回 key；drop 退出；任意源成员可 block 恶意源；drop 后 getter 仍可读、仅 isValid 翻转。

## 3. 开源代码实际用法调研

- 组播订阅管理器（jmDNS 的组管理）；block/unblock 用于 IGMPv3 源过滤，开源使用罕见，
  行为以 javadoc + 实测锁定（block 重复：javadoc 称 ISE，RI 实为幂等 no-op，以实测为准并注释）。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`MembershipKeyTest.java`。

| 测试点 | 用例方法 |
|---|---|
| join/drop 生命周期 + getter（drop 后仍可读）+ 重 join 新 key + 通道关闭失效 + 关闭后 join → CCE | testLifecycleAndGetters |
| block 返回 this、重复 block 幂等（RI 偏离 javadoc，注释记录）、unblock 未 block → ISE、block(null) → NPE、block(组播地址) → IAE、drop 后 block/unblock → ISE | testBlockContract |
| 特定源成员：sourceAddress 记录、重 join 幂等、特定源 block/unblock → ISE、drop 后任意源 join、任意源在场时特定源 join → ISE | testSourceSpecificMembership |
| 真实源过滤：未 block 收到 → block 发送网卡地址后 select(1s)==0 被滤除 → unblock 恢复 | testSourceFilteringDelivery |

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 负例经 `checkThrows` 断言**精确异常类**（`t.getClass() != ex` 即失败，子类不算）。
- 全部套接字用本地回环 127.0.0.1 + `bind(new InetSocketAddress(LO, 0))` 临时端口，不硬编码端口；
  辅助线程一律 daemon + `join(10_000)` 带超时；不使用 sleep 轮询（等待用 latch/select(超时)/parkNanos(300ms) 有界稳定）。
- 通道全部 try-with-resources / finally 关闭，无资源残留；headless 安全，不加 @key。

- 本机实测断言数：46 条。
