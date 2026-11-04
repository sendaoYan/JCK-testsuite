# java.net.NoRouteToHostException 测试设计案（JDK-8）

> 批次 F 深化：层级/catch 矩阵、构造器矩阵、重试链模拟。真实触发需要离开回环沙箱（外部不可达网络），按网络纪律不做，以合成深化补足。

## 1. API 功能

- extends SocketException（→IOException）；构造器 `()`/`(String)`。
- 语义：到主机的网络不可达（EHOSTUNREACH/ENETUNREACH）。

## 2. 测试用例设计（NoRouteToHostExceptionTest.java，62 断言）

- 构造器矩阵 + toString 精确形式 + serialVersionUID==-1897550894873493790L（双 JDK 一致）+ 恰好 2 构造器。
- 层级：NRTH<SE<IOException 三级 catch；与 BE/CE/STE 互不为子类。
- cause：CE 可作为 NRTH 的 cause（路由失败级联）；全套 cause/suppressed/序列化/跨线程。
- 重试链模拟：try-with-resources 形态嵌套 suppressed 链（attempt2⊃attempt1⊃attempt0），顶层按 IOException 捕获且消息正确。

## 易错规范细节（已锁入断言）

- 无回环触发手段：ICMP 不可达依赖真实路由，测试沙箱内不可构造——真实行为留给集成环境。

## JDK-8 vs JDK-27 差异

- 无差异（JDK-27 59 断言，契约一致）。
