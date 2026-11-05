# RequestProcessingPolicyValue 契约覆盖设计（仅 JDK-8）

## API 功能
PortableServer IDL 枚举映射类：每常量一对 `_NAME` int 常量 + `NAME` 单例对象；
`int value()`、`static T from_int(int)` 归一。

## 使用方式
纯值对象：直接引用常量或 from_int；无 ORB 依赖；无 Helper/Holder（本 JDK 映射未生成）。

## 参考来源
- OMG PortableServer/CORBA 3.0.3 POA 策略与 POAManager 状态定义
- jdk8u372-b07 实测（探测脚本 /tmp/omg-proto-e，2026-11-05）+ javap -public 方法面

## 测试用例设计 + 易错细节
1. 数值精确表（本族）：
- `USE_ACTIVE_OBJECT_MAP_ONLY` = 0，`_USE_ACTIVE_OBJECT_MAP_ONLY` = 0
- `USE_DEFAULT_SERVANT` = 1，`_USE_DEFAULT_SERVANT` = 1
- `USE_SERVANT_MANAGER` = 2，`_USE_SERVANT_MANAGER` = 2
2. from_int 全值循环归一：`from_int(v.value()) == v` 逐常量 == 断言（单例身份、自反 ==）。
3. from_int 越界（首越界值/中间值/负数/MIN/MAX）→ `org.omg.CORBA.BAD_PARAM`，
   minor==0、completed==COMPLETED_NO、消息为空串。
4. 常量两两单例身份矩阵；每个常量 implements `org.omg.CORBA.portable.IDLEntity`。
5. **易错（本 JDK 差异）**：旧映射**不覆写 toString**——值为默认 `FQCN@identityHashCode十六进制`，
   不含常量名；也**没有 `values()` 方法**（非 java enum）。断言按实测锁定。
6. 形态：类非 final；唯一 protected(int) 构造器（外部不可实例化）。
7. **映射缺口**：无 `RequestProcessingPolicyValueHelper`/`RequestProcessingPolicyValueHolder` 类（Class.forName 断言不存在）。
