# org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID 契约覆盖设计（仅 JDK-8）

## API 功能
常量接口：`public static final int value = 22`——RequestProcessingPolicy 的 policy_type 注册号（PI create_policy 的 type）。

## 使用方式
`REQUEST_PROCESSING_POLICY_ID.value` 直接引用；`orb.create_policy(type, any)` 的 type 实参即取此值。

## 参考来源
- OMG PI/POA POLICY_TYPE_ID 分配表（16..22 归 PortableServer）
- jdk8u372-b07 实测（/tmp/omg-proto-e）

## 测试用例设计 + 易错细节
1. 数值精确 == 22；反射读回一致；字段 public static final int；接口无方法、仅此一个字段。
2. 七个 POLICY_TYPE_ID 连续表 16..22 互不相同（一次锁全表）。
3. ORB 负例见 THREAD_POLICY_ID 用例（PolicyError 精确消息）。