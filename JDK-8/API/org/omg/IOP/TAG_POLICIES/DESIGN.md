# org.omg.IOP.TAG_POLICIES 契约设计

## API 功能
ComponentId 常量接口：`int value = 2`（组件 tag：策略组件）。

## 参考来源
- OMG IOP 规范；jdk8u372-b07 实测。

## 测试用例设计
- 值精确 ==2；单字段；final；int；紧邻 TAG_ALTERNATE_IIOP_ADDRESS(3)。

## 易错规范细节
- 组件 id 全表（本 JDK 物化）：0,1,2,3,25,38。
