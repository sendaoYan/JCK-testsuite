# javax.print.event 包级 黑盒行为测试设计

## 1. API 功能
PrintEvent 基类 + PrintJobEvent(6 类型常量) + 属性事件 2 + 监听器 3 + PrintJobAdapter。

## 2. 测试设计
每个类型一个子目录（均有 DESIGN.md）：常量全等、防御拷贝、null 矩阵（NPE/IAE "null source"）、适配器六空方法、监听器记录型回调闭环。

## 3. 运行纪律
纯 headless；jtreg 双侧全绿。
