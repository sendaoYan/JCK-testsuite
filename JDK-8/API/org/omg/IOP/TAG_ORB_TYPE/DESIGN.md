# org.omg.IOP.TAG_ORB_TYPE 契约设计

## API 功能
ComponentId 常量接口：`int value = 0`（组件 tag：ORB 类型）。

## 使用方式
`new TaggedComponent(TAG_ORB_TYPE.value, orbTypeBytes)`。

## 参考来源
- OMG IOP 规范 ComponentId 表；jdk8u372-b07 实测。

## 测试用例设计
- 值精确 ==0；单字段；final；int。
- ComponentIdHelper.id() 精确串。
- 数值碰撞文档化：与 ProfileId TAG_INTERNET_IOP 同为 0（两个命名空间独立）。

## 易错规范细节
- 断言“TAG_ORB_TYPE != TAG_INTERNET_IOP”是错误契约——数值确实相同。
