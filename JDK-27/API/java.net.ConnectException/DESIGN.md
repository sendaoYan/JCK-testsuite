# ConnectException 设计文档

## 一、API 功能

连接被拒绝(目标不可达/端口未监听)时抛出的 SocketException 子类。

## 二、Java 用户如何使用

用户在 Socket.connect 失败时捕获。

## 三、开源代码实际用法调研

客户端重试与熔断逻辑依赖此异常。

## 四、测试用例设计

继承 SocketException；构造器；连接关闭端口触发 ConnectException。
