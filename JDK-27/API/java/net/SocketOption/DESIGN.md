# SocketOption 设计文档

## 一、API 功能

泛型接口，表示套接字配置选项(名称+值类型)。

## 二、Java 用户如何使用

用户以 SocketOption<T> 作为 Socket.setOption/getOption 的键。

## 三、开源代码实际用法调研

JDK 的 StandardSocketOptions 中所有选项均实现此接口；NIO 通道亦使用。

## 四、测试用例设计

验证为接口；name() 返回字符串、type() 返回值类型；子类/内建选项 instanceof SocketOption。

## 批次 F 深化补充（92 断言）

- 泛型接口白盒：单类型参数 T、name()/type() 均 abstract、恰好 2 个声明方法。
- 自定义实现矩阵（含数组类型参数、null 返回值透传）。
- 12 个标准选项消费矩阵 + `Socket.supportedOptions()` 非空、含 SO_KEEPALIVE/TCP_NODELAY、自定义选项不在集合中。
- NetworkChannel 表面：setOption(SocketOption,T)/getOption/supportedOptions。
