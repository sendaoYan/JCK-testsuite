# org.omg.PortableServer.ServantManagerOperations 契约覆盖设计（仅 JDK-8）

## API 功能
空标记接口：0 声明方法、0 父接口、总面 0。

## 使用方式
仅作为 ServantActivatorOperations/ServantLocatorOperations 的共同父接口存在。

## 参考来源
- OMG POA；jdk8u372-b07 javap

## 测试用例设计 + 易错细节
1. 三个 0（declared/parents/getMethods）锁定纯标记形态。
