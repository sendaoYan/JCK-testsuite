# org.omg.CosNaming._NamingContextImplBase

## API 功能
服务端骨架基类（旧式 DynamicImplementation 路线，deprecated 自 POA）。
## 使用方式
继承并实现 10 个命名操作；`_ids()` 声明类型。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- abstract + extends org.omg.CORBA.DynamicImplementation；匿名子类离线可构造（10 个方法全实现）。
- `_ids()` 单元素精确 `"IDL:omg.org/CosNaming/NamingContext:1.0"`（与 stub 一致）。
