# DatagramSocketImpl 设计文档

## 一、API 功能

UDP 套接字实现的抽象基类，封装 create/bind/send/receive/join/leave 等原生操作。

## 二、Java 用户如何使用

自定义 UDP 实现时继承并实现各方法，通过 setDatagramSocketImplFactory 注入。

## 三、开源代码实际用法调研

JDK 内建 DatagramSocketImpl 派生自此类；自定义 UDP 协议栈使用。

## 四、测试用例设计

验证 abstract 且实现 SocketOptions；声明的方法与 localPort/fd 字段；通过真实 DatagramSocket(0) 验证端口分配。

---

# 深化批次 G 追加（2026-11-05，~24 → 86 断言级契约覆盖）

## 覆盖缺口分析

缺：每个声明方法的 abstract/protected/包私有精确分类、legacy setTTL/getTTL 移除的
负向验证、最小可编译子类对全部 13 个抽象成员的分派验证、protected 访问器默认值、
SocketOption 三元组（JDK-13+）、抽象类不可实例化。

## 新增测试方法

| 方法 | 覆盖 |
|---|---|
| testMemberTableDeep | 13 个抽象方法（create/bind/send/peek/peekData/receive/setTimeToLive/getTimeToLive/join/leave/joinGroup/leaveGroup/close）均 protected abstract；connect/disconnect/getLocalPort/getFileDescriptor protected 具体；setOption(SocketOption,T)/getOption/supportedOptions protected 具体（JDK-13+）；setTTL/getTTL 已移除；protected int localPort + protected FileDescriptor fd；包私有 dataAvailable 仍在、socket 字段与 set/getDatagramSocket 已移除 |
| testMinimalSubclass | 13 抽象成员全部可实现的具名子类；fresh getLocalPort()==0、getFileDescriptor()==null、默认 supportedOptions() 非 null；localPort 子类可写并经 getLocalPort() 读回；每个抽象方法经子类包装调用计数验证分派；默认 disconnect() → UncheckedIOException("disconnect not implemented")；SocketOptions.setOption(int,Object)/getOption(int) 经子类分派 |
| testNonInstantiable | 抽象类反射实例化 → InstantiationException |

## 版本差异（探针实测）

1. JDK-8 有 abstract setTTL(byte)/getTTL()（共 15 个抽象方法）；JDK-27 移除（13 个）。
2. JDK-8 的 setOption(SocketOption,T)/getOption(SocketOption) 是**包私有**（8u backport）；JDK-27 升为 protected，并新增 protected supportedOptions()。
3. JDK-8 默认 disconnect() 是空 no-op；JDK-27 默认抛 UncheckedIOException("disconnect not implemented")。
4. JDK-8 有包私有 socket 字段与 setDatagramSocket/getDatagramSocket；JDK-27 无。
5. 两侧均有包私有 dataAvailable()。SocketOptions.setOption(int,Object)/getOption(int) 均不经本类重声明、由接口保持抽象。
