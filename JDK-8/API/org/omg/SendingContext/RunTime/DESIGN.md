# org.omg.SendingContext.RunTime 契约设计

## API 功能
发送上下文运行时对象接口：本体无方法，继承 RunTimeOperations+CORBA::Object+IDLEntity。

## 参考来源
- OMG SendingContext 模块；jdk8u372-b07 实测。

## 测试用例设计
- 声明 0 方法；恰 3 父接口；RunTimeOperations 为空操作接口。
- org.omg.SendingContext.Entity 在 jdk8u372 未物化（规范里 RunTime extends Entity，本 JDK 无该类）。

## 易错规范细节
- 不要断言 "RunTime extends Entity"——Entity 类不存在于 rt.jar。
