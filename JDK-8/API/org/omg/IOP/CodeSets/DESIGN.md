# org.omg.IOP.CodeSets 契约设计

## API 功能
ServiceId 常量接口：`int value = 1`（CodeSets 服务上下文 id）。

## 参考来源
- OMG IOP ServiceId 表；jdk8u372-b07 实测。

## 测试用例设计
- 值精确 ==1；单字段；final；int；TransactionService(0) 与之连续。

## 易错规范细节
- 与 ComponentId TAG_CODE_SETS 同值不同族。
