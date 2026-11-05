# UserDataHandler — 设计案

## 1. API 功能
（见 org.w3c.dom.UserDataHandler javadoc / DOM Level 3 Core）

## 2. 使用方式
经 Document/DOMConfiguration/LSParser 组合取得实例或回调。

## 3. 参考来源
W3C DOM Level 3 Core/LS 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
五常量（CLONED=1/IMPORTED=2/DELETED=3/RENAMED=4/ADOPTED=5）+ 真实路由：cloneNode→1（src=原 dst=克隆）、importNode→2（dst=导入副本）、renameNode→4（src=改名后节点、**dst=null**）、adoptNode→5（**dst=null**，Xerces）；NODE_DELETED 在活树上不可观测（勿测）。userData **不会**自动拷贝到克隆（handler 是应用唯一的挂钩）；adoption 后原节点 userData 仍可读；多 key 各自独立回调；null handler 数据保留但无回调。
