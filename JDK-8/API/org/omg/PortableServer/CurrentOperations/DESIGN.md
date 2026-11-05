# org.omg.PortableServer.CurrentOperations 契约覆盖设计（仅 JDK-8）

## API 功能
恰 2 自有方法：`get_POA() -> POA`、`get_object_id() -> byte[]`，均恰 throws NoContext。

## 使用方式
上下文查询通道；父接口 CORBA.CurrentOperations 为空标记接口（总面仍为 2）。

## 参考来源
- OMG POA；jdk8u372-b07 javap + 实测

## 测试用例设计 + 易错细节
1. 自有==2、总面==2（父接口空）；返回类型与异常表逐个精确。
