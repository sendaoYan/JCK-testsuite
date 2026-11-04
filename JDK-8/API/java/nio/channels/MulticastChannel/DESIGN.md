# java.nio.channels.MulticastChannel 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。join(group,nif)/join(group,nif,source)/drop、成员键 getter、
> 组播回环送达、IP_MULTICAST_* 选项；多播网卡能力守护。

## 1. API 提供了哪些功能

- `join(InetAddress,NetworkInterface)`（任意源）、`join(InetAddress,NetworkInterface,InetAddress)`
 （特定源）、MembershipKey.drop()；DatagramChannel 是唯一实现。
- 选项：IP_MULTICAST_IF/IP_MULTICAST_TTL/IP_MULTICAST_LOOP。

## 2. Java 用户如何使用

- open(INET) → bind(group, 0)（绑定组播地址）→ join(group, nif) → receive；
  发送侧 setOption(IP_MULTICAST_IF, nif) 后 send(group:port)。

## 3. 开源代码实际用法调研

- mDNS/服务发现（jmDNS）、Elasticcast/pongs 等组播发现组件、SDP 协商；
  坑：lo 网卡可能不支持多播，须 NetworkInterface.supportsMulticast() 守护。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`MulticastChannelTest.java`。

| 测试点 | 用例方法 |
|---|---|
| join 幂等（同组同网卡返回同一 key）、drop 后重 join 得新 key | testJoinAndGetters |
| getter 同一性（channel/group/nif/sourceAddress==null） | testJoinAndGetters |
| 真实组播回环：bind 组地址 + join + IP_MULTICAST_IF + send → select==1 → receive 精确 | testGroupRoundTrip |
| 特定源 join 与任意源共存 → IllegalStateException；关闭后 join → CCE；经接口 close | testJoinNegativesAndClose |
| IP_MULTICAST_TTL/LOOP 往返 | testMulticastOptions |
| 多播网卡选择器（up + supportsMulticast + IPv4，优先非 lo；无则 NOTE+跳过） | pickMulticastInterface |

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 负例经 `checkThrows` 断言**精确异常类**（`t.getClass() != ex` 即失败，子类不算）。
- 全部套接字用本地回环 127.0.0.1 + `bind(new InetSocketAddress(LO, 0))` 临时端口，不硬编码端口；
  辅助线程一律 daemon + `join(10_000)` 带超时；不使用 sleep 轮询（等待用 latch/select(超时)/parkNanos(300ms) 有界稳定）。
- 通道全部 try-with-resources / finally 关闭，无资源残留；headless 安全，不加 @key。

- 组播依赖运行环境网卡能力：picker 找不到可用网卡时打印 NOTE 并跳过行为部分，不判失败。
- 本机实测断言数：38 条。
