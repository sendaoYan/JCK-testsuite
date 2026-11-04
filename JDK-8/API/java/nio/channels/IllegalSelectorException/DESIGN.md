# java.nio.channels.IllegalSelectorException 设计文档

## 1. API 功能

IllegalSelectorException：java.nio.channels 包的unchecked异常（extends IllegalArgumentException，javap 于 JDK-8(8u372) 与 JDK-27(27.0.1) 双侧实证），
报告 <语义见各触发场景>。仅有唯一一个无参公共构造器（双侧 javap 一致，无 message 重载）；
serialVersionUID = -8406323347253320987L（ObjectStreamClass 双侧实测，完全一致）。

## 2. Java 用户如何使用

- 捕获方式：在相应通道操作的 catch 中按具体异常类型分流（区别于笼统 IOException/RuntimeException），
  精确类型即精确失败原因。
- 信息获取：无参构造 getMessage()/getLocalizedMessage() 恒为 null；getCause()/initCause()/
  addSuppressed() 沿用 Throwable 契约。
- 序列化：经由 Throwable 实现 Serializable，round-trip 保持精确类型/空消息/空 cause/suppressed。
- 生命周期：异常在失败点一次性抛出，不持有资源。

## 3. 开源代码实际用法调研

- OpenJDK 源码抛出点：sun.nio.ch.SelectorImpl.register 的 `!(ch instanceof SelChImpl)` 检查（两侧源码 grep 实证为唯一抛出点，jdk8u-dev SelectorImpl.java:128 / jdk SelectorImpl.java:210）（阅读 jdk8u-dev / jdk 仓库源码后自写归纳，未拷贝代码）。
- 社区常见形态：NIO 框架在通道状态机分支按具体异常类型分级处理（重试/回退/终止）；
  测试侧普遍以临时文件、回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`IllegalSelectorExceptionTest.java`（单文件；既有基础方法保留，批次 A8 新增契约级方法由 main 依次调用）。

批次 A8 契约级四件套（双侧等价实现，全部条目先经探针在 8u372/27.0.1 双侧实测）：
1. 白盒层级：直接父类 == IllegalArgumentException（javap 实证）、完整继承链、Throwable/Serializable 可赋值、FQN 精确。
2. 构造矩阵：唯一公共构造器且无参（反射断言）、message/localizedMessage null、toString 为裸类名。
3. 序列化：ObjectStreamClass.getSerialVersionUID() == -8406323347253320987L + round-trip（类型/消息/cause/
   suppressed/栈深保持、新实例身份独立）。
4. 真实触发：自定义 AbstractSelectableChannel 子类（挂自定义 SelectorProvider，非 sun.nio.ch 实现）configureBlocking(false) 后 register 到系统 Selector → 精确类型；对照同 provider 注册正常、closed 检查优先返回 ClosedChannelException。
5. Throwable 通用契约：getCause 默认 null；initCause 一次性（二次 → IllegalStateException、
   self-cause → IllegalArgumentException 且 cause 不变）；fillInStackTrace 返回 this；构造时捕获栈帧；
   suppressed 默认空表 + addSuppressed 可见；throw/catch 身份保持。

自动化与健壮性：
- checkThrows 按 getClass() == expected 断言精确类型（不容忍子类），失败抛 AssertionError 带完整信息。
- 网络：全回环 bind(0)/loopback，无外联、无 DNS（未解析地址用 InetSocketAddress.createUnresolved）；
  守护线程 join(超时)，有界等待（latch/有界重试），无 sleep 轮询。
- 文件：Files.createTempDirectory + finally 递归删除；headless 安全，不加 @key。
- 本机实测断言数：4 → 48 条。
