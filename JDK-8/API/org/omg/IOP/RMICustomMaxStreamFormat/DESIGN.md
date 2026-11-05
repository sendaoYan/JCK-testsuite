# org.omg.IOP.RMICustomMaxStreamFormat 契约设计

## API 功能
ServiceId 常量接口：`int value = 17`（RMI 最大流格式服务上下文）。

## 参考来源
- OMG IOP ServiceId 表；jdk8u372-b07 实测。

## 测试用例设计
- 值精确 ==17；单字段；final；int。

## 易错规范细节
- 与组件族 TAG_RMI_CUSTOM_MAX_STREAM_FORMAT(38) 区分。
