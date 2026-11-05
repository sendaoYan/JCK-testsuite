# org.omg.IOP.TransactionService 契约设计

## API 功能
ServiceId 常量接口：`int value = 0`（事务服务上下文 id）。

## 参考来源
- OMG IOP ServiceId 表；jdk8u372-b07 实测。

## 测试用例设计
- 值精确 ==0；单字段；final；int；ServiceIdHelper.id() 精确串。

## 易错规范细节
- ServiceId 家族本 JDK 物化：0,1,14,17。
