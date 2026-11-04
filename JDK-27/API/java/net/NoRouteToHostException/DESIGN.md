# NoRouteToHostException 设计文档

## 一、API 功能

无法路由到目标主机(ICMP 不可达)时抛出的 SocketException 子类。

## 二、Java 用户如何使用

用户在 Socket.connect 时捕获(通常表现为 ConnectException 的替代)。

## 三、开源代码实际用法调研

网络诊断工具使用。

## 四、测试用例设计

继承 SocketException；直接构造与带 message 构造。

## 批次 F 深化补充（59 断言）

- serialVersionUID=-1897550894873493790L（双 JDK 一致）。
- 无真实触发（到达不可达主机需离开回环沙箱，违反网络纪律）→ 合成深化：构造器矩阵、toString、catch 矩阵、与 BE/CE/STE/PUE 互斥、CE 可作 NRTH 的 cause。
- 重试链模拟：try-with-resources 形态的嵌套 suppressed 链（attempt2 ⊃ attempt1 ⊃ attempt0），顶层按 IOException 捕获。
