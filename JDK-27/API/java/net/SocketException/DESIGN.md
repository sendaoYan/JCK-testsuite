# SocketException 设计文档

## 一、API 功能

套接字操作失败的通用异常。

## 二、Java 用户如何使用

用户在所有 socket 操作处捕获 IOException/SocketException。

## 三、开源代码实际用法调研

网络编程最常用的异常之一。

## 四、测试用例设计

继承 IOException；构造器；closed socket 操作触发。

## 批次 F 深化补充（63 断言）

- 构造器矩阵（JDK 20+ 4 个）：()、(String)、(String,Throwable)、(Throwable)；(Throwable) 消息=cause.toString()；(String,null) 合法。
- toString 精确形式："java.net.SocketException: msg" / 无消息时裸类名；空消息保留 "…: "；非 ASCII 保留。
- serialVersionUID=-5935874303556886934L（与 JDK-8 相同，反射断言）。
- cause 契约：initCause 一次性（第二次→ISE）、自因→IAE "Self-causation not permitted"、fresh+initCause(null) 合法。
- suppressed：初始 0、顺序保持、防御拷贝、自抑制→IAE。
- 序列化：message/cause/suppressed 全存活、同类。
- 真实触发（回环）：closed socket getInputStream/getSoTimeout→"Socket is closed"。
- JDK-8 侧（65 断言）：2 构造器、ctor(Throwable) 不存在。
