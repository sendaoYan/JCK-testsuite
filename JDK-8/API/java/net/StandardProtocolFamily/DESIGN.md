# java.net.StandardProtocolFamily 测试设计案（JDK-8）

> 批次 F 深化：枚举结构、常量集 {INET, INET6}、name/valueOf/ordinal/compareTo/序列化恒等、values 防御拷贝。

## 1. API 功能

- 枚举 implements ProtocolFamily；常量 INET(0)、INET6(1)。
- 继承 Enum 全套：name/toString/ordinal/compareTo/valueOf/values/getDeclaringClass。

## 2. 测试用例设计（StandardProtocolFamilyTest.java，50 断言）

- 结构：final enum、直接接口恰好 {java.net.ProtocolFamily}、values()/valueOf(String)/name() 可反射取得。
- 常量：values().length==2、name()/toString() 精确串（枚举 toString==name）。
- valueOf 边界：INET/INET6 往返恒等；BOGUS/空串/小写→IllegalArgumentException（消息含常量名）；null→NullPointerException；**UNIX 在 JDK-8 不存在**（IAE，消息含 "UNIX"）。
- Enum 契约：ordinal 0/1、compareTo 全序 INET<INET6、equals 身份、hashCode 稳定、getDeclaringClass。
- values() 防御拷贝：两次调用返回不同数组、内容相等。
- 序列化恒等：round-trip 返回同一单例（enum 反序列化不走构造器）。

## 易错规范细节（已锁入断言）

- `valueOf` 区分大小写；`valueOf(null)`→NPE 而非 IAE。
- 枚举序列化恒等：`back == INET6` 为 true。

## JDK-8 vs JDK-27 差异

- JDK-27（65 断言）：三常量含 UNIX(2)（JDK 16+），另有 `SocketChannel.open(INET)`/`ServerSocketChannel.open(UNIX)` 家族驱动用法与逐常量序列化恒等。
