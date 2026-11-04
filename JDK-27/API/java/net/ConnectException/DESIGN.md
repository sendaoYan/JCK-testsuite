# ConnectException 设计文档

## 一、API 功能

连接被拒绝(目标不可达/端口未监听)时抛出的 SocketException 子类。

## 二、Java 用户如何使用

用户在 Socket.connect 失败时捕获。

## 三、开源代码实际用法调研

客户端重试与熔断逻辑依赖此异常。

## 四、测试用例设计

继承 SocketException；构造器；连接关闭端口触发 ConnectException。

## 批次 F 深化补充（56 断言）

- serialVersionUID=3831404271622369215L（双 JDK 一致）。
- 真实触发（回环）：ServerSocket(0) 取端口后 close，connect 该端口→ConnectException；JDK-27 消息 "Connection refused (connect failed)"（JDK-8 "…(Connection refused)"）；refused 后 JDK-27 isClosed=true（JDK-8 false）；第二次拒绝同样 CE。
- CE 可作为 CE 的 cause（重试级联）；cause/suppressed/序列化/跨线程全套。
