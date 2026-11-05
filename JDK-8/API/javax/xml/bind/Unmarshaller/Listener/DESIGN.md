# Unmarshaller.Listener 设计案（JDK-8 契约测试）

## 1. API 功能
unmarshal 生命周期钩子抽象类：beforeUnmarshal(target, parent)/afterUnmarshal(target, parent)，两参形态（parent 对根对象为 null）。

## 2. 使用方式
Unmarshaller.setListener(subclass) → unmarshal 时回调；afterUnmarshal 的字段修改进入返回对象。

## 3. 参考来源
Oracle javadoc javax.xml.bind.Unmarshaller.Listener、JSR 222、OpenJDK8 RI unmarshaller、本仓库 ListenerTest/UnmarshallerTest。

## 4. 测试用例设计 + 易错规范细节
- 结构：abstract、恰 2 个具体 no-op 方法、(Object,Object) 双参。
- 生命周期：before/after 各 >=1、首 before 末 after、target==Person、**根对象 parent==null**（实测）。
- 行为闭环：afterUnmarshal 改 name=fixed → 返回对象读到 fixed（钩子在填充后、返回前）；setListener(null) 停；空子类 no-op。
- 易错点：parent 仅对嵌套子对象非 null；RI 回调次数 >=1 不精确。
