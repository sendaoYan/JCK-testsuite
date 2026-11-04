# java.net.SocketOption 测试设计案（JDK-8）

> 批次 F 深化：泛型接口结构、自定义实现矩阵、标准选项引用、NetworkChannel 消费面。

## 1. API 功能

- 泛型接口 `SocketOption<T>`：`name()`（String）、`type()`（Class<T>）。
- 被 `java.nio.channels.NetworkChannel`（setOption/getOption/supportedOptions）与 JDK 9+ 的 Socket/ServerSocket/DatagramSocket 消费。

## 2. 测试用例设计（SocketOptionTest.java，85 断言）

- 白盒：单类型参数 T、恰好 2 个声明方法且均 abstract。
- 自定义实现：任意 name/type 组合、数组类型参数、null 返回值透传（接口不校验）。
- 标准矩阵：11 个 StandardSocketOptions 字段逐一 name/type 精确断言 + 名字唯一。
- NetworkChannel 表面：三方法存在性（反射）。

## 易错规范细节（已锁入断言）

- name()/type() 无默认实现；type() 返回的是擦除后的 Class 对象（如 Boolean.class 而非 boolean.class）。

## JDK-8 vs JDK-27 差异

- JDK-27（92 断言）：12 标准选项；另有 `Socket.supportedOptions()` 实测消费（含 SO_KEEPALIVE set/get 往返）。
