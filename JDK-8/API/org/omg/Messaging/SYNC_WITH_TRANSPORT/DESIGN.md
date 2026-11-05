# org.omg.Messaging.SYNC_WITH_TRANSPORT 契约设计

## API 功能
同步范围常量接口：short value = 1（响应到达传输层即认为完成）。

## 参考来源
- OMG Messaging（SYNC_NONE=0/SYNC_WITH_TRANSPORT=1/SYNC_WITH_SERVER=2/SYNC_WITH_TARGET=3）；jdk8u372-b07 实测。

## 测试用例设计
- 值精确 ==1（short）；单字段；final。
- 家族覆盖度锁定：SYNC_NONE/SYNC_WITH_SERVER/SYNC_WITH_TARGET 在 jdk8u372 rt.jar **未物化**（Class.forName 抛 CNFE）——本 JDK 只带 SYNC_WITH_TRANSPORT 一个常量类 + SyncScopeHelper。
- SyncScopeHelper 仓库 id 精确。

## 易错规范细节
- 勿断言 4 常量全存在——JDK-8 只物化 1 个；SyncScopeHelper 的 Any 通道完全可用。
