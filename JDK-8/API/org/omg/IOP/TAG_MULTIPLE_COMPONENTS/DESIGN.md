# org.omg.IOP.TAG_MULTIPLE_COMPONENTS 契约设计

## API 功能
ProfileId 常量接口：`int value = 1`（多组件 profile 的 tag）。

## 使用方式
`new TaggedProfile(TAG_MULTIPLE_COMPONENTS.value, data)`。

## 参考来源
- OMG IOP 规范；jdk8u372-b07 javap/反射实测。

## 测试用例设计
- 值精确 ==1；单字段；final；int。
- 与 TAG_INTERNET_IOP 差值恰为 1（保留 profile id 连续区间 0..1）。

## 易错规范细节
- 勿把 TAG_MULTIPLE_COMPONENTS(1) 与 ComponentId 的 TAG_CODE_SETS(1) 混同。
