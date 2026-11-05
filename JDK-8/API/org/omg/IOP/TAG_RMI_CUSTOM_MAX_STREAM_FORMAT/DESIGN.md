# org.omg.IOP.TAG_RMI_CUSTOM_MAX_STREAM_FORMAT 契约设计

## API 功能
ComponentId 常量接口：`int value = 38`（RMI 自定义最大流格式组件）。

## 参考来源
- OMG IOP/RMI-over-IIOP 规范；jdk8u372-b07 实测。

## 测试用例设计
- 值精确 ==38；单字段；final；int。
- 与同名 ServiceId 家族 RMICustomMaxStreamFormat(17) 互异（38 vs 17）。

## 易错规范细节
- 名字相近但分属 ComponentId/ServiceId 两个家族，数值不同。
