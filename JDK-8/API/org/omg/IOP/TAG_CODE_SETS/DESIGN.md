# org.omg.IOP.TAG_CODE_SETS 契约设计

## API 功能
ComponentId 常量接口：`int value = 1`（组件 tag：字符集协商）。

## 使用方式
`new TaggedComponent(TAG_CODE_SETS.value, connIdBytes)`。

## 参考来源
- OMG IOP 规范；jdk8u372-b07 实测。

## 测试用例设计
- 值精确 ==1；单字段；final；int。
- 与 ServiceId 家族 CodeSets(1) 数值一致（同一 OMG 号在两个命名空间重复使用）。

## 易错规范细节
- TAG_CODE_SETS（组件）与 CodeSets（服务上下文）是不同接口、同为 1。
