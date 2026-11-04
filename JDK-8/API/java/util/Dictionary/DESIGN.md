# java.util.Dictionary 契约测试设计（JDK-8 侧）

## 1. API 功能

Dictionary<K,V> 是 pre-Collections Framework 的抽象键值映射基类（JDK 1.0），Hashtable 是
平台内唯一的具体实现。公开 API 恰好 7 个方法：size/isEmpty/keys/elements/get/put/remove，
其中 keys/elements 返回旧式 Enumeration。不是 Map，也不是 Collection 框架成员。

## 2. 使用方式

- `Dictionary<String,Integer> d = new Hashtable<>()` 以 Hashtable 为载体；
- `MapDictionary extends Dictionary`（HashMap 支撑的最小实现）作契约探针；
- 反射 `Class.forName("java.util.Dictionary")` 做白盒结构断言。

## 3. 参考来源

- Oracle javadoc java.util.Dictionary（抽象方法集、无 null 规则说明）；
- OpenJDK8u `java/util/Dictionary.java`（仅接口定义，无逻辑）。

## 4. 测试用例设计

| 场景 | 断言要点 |
|---|---|
| 白盒 | abstract、恰 7 个公开方法、keys/elements 返回 Enumeration、非 Map、Hashtable 同时是 Map 与 Dictionary、Serializable |
| 探针契约 | put 新键 null/旧键前值、get 命中/缺失 null、remove 返回值/缺失 null、size/isEmpty、keys/elements 枚举内容 |
| null 语义 | 自定义实现可收 null key（Dictionary 层无规则）；Hashtable 经 Dictionary 引用 put(null,·)/put(·,null)/get(null)/remove(null) 全 NPE |
| 空枚举 | keys()/elements() 耗尽后 nextElement → NoSuchElementException（探针与 Hashtable 双轨） |
| 双引用 | 同一 Hashtable 经 Map/Dictionary 引用共享单表（put/remove 互通、size 联动） |
| 行为回归 | 原有 Hashtable 基本路径（put/get/size/keys/elements/remove）保留 |

## 5. 易错规范细节

- Dictionary 枚举顺序不承诺：断言用排序或成员判定，不锁顺序。
- 空枚举先断 hasMoreElements()==false 再断 nextElement NSEE。
- Hashtable 的 null 拒绝是 Hashtable 的（ synchronized 遗留）语义，不是 Dictionary 契约；
  用自定义探针证明「接口不要求拒绝 null」。

## 负例表

| 操作 | 异常 |
|---|---|
| Hashtable 路径 put(null,·)/put(·,null)/get(null)/remove(null) | NullPointerException |
| 空枚举 nextElement（两种载体） | NoSuchElementException |
