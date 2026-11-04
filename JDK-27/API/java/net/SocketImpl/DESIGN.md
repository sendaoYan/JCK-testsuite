# SocketImpl 设计文档

## 一、API 功能

套接字实现的抽象基类，封装原生 socket 操作(create/connect/bind/listen/accept/输入输出)。

## 二、Java 用户如何使用

自定义 Socket 实现时继承 SocketImpl 并实现各方法，再通过 setSocketImplFactory 注入。

## 三、开源代码实际用法调研

JDK 内建 PlainSocketImpl 派生自此；自定义协议栈/代理实现亦继承。

## 四、测试用例设计

验证 abstract 且实现 SocketOptions；protected 字段 fd/address/port/localport；匿名/具名子类暴露受保护访问器并断言默认值。

## 批次 F 深化补充（79 断言）

- 白盒：抽象方法清单及修饰符（public abstract ×5：create/connect/bind/listen/accept 等）；protected 字段类型（address:InetAddress、port:int、localport:int、fd:FileDescriptor）。
- 子类默认行为：MiniImpl 只覆写必要方法；基类 `shutdownInput/shutdownOutput` 抛 IOException "Method not implemented!"。
- `toString` 默认形式 "Socket[addr=null,port=0,localport=0]"。
- JDK-27：protected `setOption/getOption(SocketOption)`（JDK 13+）存在性（MiniImpl 不覆写即继承默认抛错路径不触发）。
