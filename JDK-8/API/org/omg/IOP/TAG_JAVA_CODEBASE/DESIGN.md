# org.omg.IOP.TAG_JAVA_CODEBASE 契约设计

## API 功能
ComponentId 常量接口：`int value = 25`（Java 专用 codebase 组件）。

## 参考来源
- OMG IOP 规范（SUN 保留段 >=20）；jdk8u372-b07 实测。

## 测试用例设计
- 值精确 ==25；单字段；final；int；>=20（保留段）。

## 易错规范细节
- 20-23 为 SUN 早期私有保留，25 为正式分配的 TAG_JAVA_CODEBASE。
