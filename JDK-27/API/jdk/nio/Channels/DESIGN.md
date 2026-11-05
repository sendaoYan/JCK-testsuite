# jdk.nio.Channels 设计案

## 1. API 功能
SelectableChannel 工厂：readWriteSelectableChannel。

## 2. Java 用户如何使用
Channels.readWriteSelectableChannel(fd, closer)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.nio/share/classes/jdk/nio/Channels.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 fd→channel→validOps→close→closer 回调。

## 5. 契约深化（JDK-27）

- 选择器真实接入：configureBlocking(false) 后 register(OP_READ) 成功，interestOps==OP_READ、readyOps==0、key 绑定同一通道；selectNow()==0（普通文件 fd 永不就绪）；随后可 configureBlocking(true) 往返。
- validOps 精确 == OP_READ|OP_WRITE(5)；provider 非空；新通道默认阻塞。
- 生命周期二段性：close() 恰好触发 implCloseChannel 一次，implReleaseChannel 不被触发（fd 仍被宿主持有）；二次 close 幂等、isOpen() 恒 false。
- null closer 直接 NPE（fd 所有权必须显式移交）。
