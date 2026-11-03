# java.util.Dictionary — 测试设计案

## 1. API 功能
遗留的键值映射抽象类（Hashtable 的父类，现已基本被 Map 取代）。

- 抽象：size/isEmpty/keys()/elements()/get/put/remove。
- 语义：keys 返回键枚举、elements 返回值枚举、put 覆盖旧值、get/remove 缺失返回 null。

## 2. Java 用户如何使用
几乎不用直接引用；历史上 `Dictionary d = new Hashtable()`。枚举遍历用
`for (Enumeration e = d.keys(); e.hasMoreElements();) ...`。
链路：new Hashtable -> put -> size/get -> keys/elements 枚举 -> remove -> isEmpty。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/Dictionary.java`（本地 `~/git/jdk25u-dev`）：
  全部 7 个方法 abstract；javadoc 注明 Hashtable 是其实现。
- OpenJDK `src/java.base/share/classes/java/util/Hashtable.java` 实现 Dictionary 且 put/get
  不接受 null key/value——第 5 节验证。
- 现代代码（Spring/Guava）几乎不使用，主要存在于遗留兼容层。

## 4. 测试用例设计 + 自动化实现（DictionaryTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| put/get/size | 空判定、put 后 size/get、缺失 get null |
| 覆盖 | put 同键覆盖、get 反映 |
| keys/elements | 枚举覆盖全部键/值（排序后断言） |
| remove | 返回并删除、缺失 null |
| Hashtable 语义 | 拒绝 null key |

共 5 组场景、约 13 个 check（`testBlackBox()`）。
