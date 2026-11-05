# Service.Mode 设计案（JDK-8 契约测试）

## 1. API 功能
Dispatch 消息粒度枚举：PAYLOAD（仅消息体）/ MESSAGE（整个协议消息）。

## 2. 使用方式
Service.createDispatch(port, type, Service.Mode.PAYLOAD|MESSAGE) 第三参。

## 3. 参考来源
Oracle javadoc javax.xml.ws.Service.Mode、JSR 224、本仓库 ModeTest/ServiceTest/DispatchTest。

## 4. 测试用例设计 + 易错规范细节
- 枚举矩阵：恰 2 常量；**声明序 MESSAGE(0) 在前、PAYLOAD(1) 在后**（实测，与直觉相反）；name/toString/ordinal/vaofOf 全等；valueOf 大小写敏感（"payload" → IAE，消息含常量名）；compareTo 方向与 EnumSet 互操作。
- 单例性：values() 返回同一批常量实例；final 类、无 public 构造。
- 易错点：声明顺序勿想当然（PAYLOAD 更常用但排在第二）。
