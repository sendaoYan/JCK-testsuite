# org.omg.SendingContext.RunTimeOperations 契约设计

## API 功能
空操作接口（规范 2.6 版 SendingContext::RunTime 无操作）。

## 参考来源
- OMG SendingContext；jdk8u372-b07 实测。

## 测试用例设计
- 0 方法/0 字段/0 父接口；RunTime（conformance 接口）仍带 CORBA::Object。

## 易错规范细节
- 旧版规范有 get_encoding/…操作，2.6 修订后无——JDK 映射为空接口。
