# AdapterStateHelper 契约设计

## API 功能
别名 Helper（unsigned short；HOLDING/ACTIVE/...）。

## 参考来源
- jdk8u372-b07 实测。

## 测试用例设计
- id 精确/tk_alias/wire 往返 HOLDING(0)/Any 往返 NON_EXISTENT(4)——与状态常量族交叉锁定。
