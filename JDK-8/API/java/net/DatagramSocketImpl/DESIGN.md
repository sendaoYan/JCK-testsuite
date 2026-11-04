# DatagramSocketImpl 设计文档（JDK-8）

## 一、API 功能

UDP 套接字实现的抽象基类：15 个抽象成员（含 JDK-8 特有 setTTL(byte)/getTTL()）、
protected connect/disconnect/getLocalPort/getFileDescriptor 具体默认、protected 状态
（int localPort、FileDescriptor fd）、实现 SocketOptions；8u 另有包私有的
setOption/getOption(SocketOption) backport、dataAvailable 与 socket 回引。

## 二、Java 用户如何使用

自定义 UDP 协议栈时继承并实现全部抽象成员，经
`DatagramSocket.setDatagramSocketImplFactory` 注入（每 JVM 一次）。

## 三、参考来源

- Oracle Java SE 8 javadoc（java/net/DatagramSocketImpl.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/src/share/classes/java/net/DatagramSocketImpl.java`（成员分类）。
- 双 JDK 探针实测（/tmp/probe-g，2026-11-05）。

## 四、测试用例设计（DatagramSocketImplTest.java，77 断言）

| 方法 | 覆盖 |
|---|---|
| testLegacy | 保留原断言（abstract/非接口/父类/直接接口/getOption+setOption 存在） |
| testMemberTable | 15 个抽象方法（含 setTTL/getTTL）均 protected abstract；connect/disconnect/getLocalPort/getFileDescriptor protected 具体；protected int localPort、protected FileDescriptor fd；包私有 dataAvailable/setDatagramSocket/getDatagramSocket/setOption(SocketOption,T)/getOption(SocketOption)/socket 字段存在；setOption(int,Object) 不在本类重声明（由 SocketOptions 保持抽象） |
| testMinimalSubclass | 具名最小子类（15 抽象成员 + SocketOptions 两方法）；fresh getLocalPort()==0、getFileDescriptor()==null；localPort 子类可写读回；全部抽象方法经子类包装调用计数分派（create/bind(写 localPort)/send/peek/peekData/receive/setTTL-getTTL/setTimeToLive-getTimeToLive/join/leave/joinGroup/leaveGroup/close）；默认 disconnect() no-op；setOption(int,Object)/getOption(int,Object) 分派 |
| testNonInstantiable | 抽象类 Class.newInstance() → InstantiationException；真实 DatagramSocket(0) 端口分配走平台 impl |

## 五、易错规范细节与版本差异

- JDK-8 抽象成员 15 个（含 setTTL(byte)/getTTL()）；JDK-27 移除该对，剩 13 个。
- 8u 的 setOption/getOption(SocketOption) 是**包私有**（javap -p 证实），JDK-27 升为 protected 并新增 supportedOptions()。
- 默认 disconnect()：JDK-8 空 no-op；JDK-27 抛 UncheckedIOException("disconnect not implemented")。
- 子类从外部包访问 protected 成员必须经子类自身的包装方法（编译器规则，测试即验证此契约）。
