# java.util.Observable 测试设计案（JDK-8）

> 覆盖目标：完整契约：addObserver/deleteObserver/countObservers、setChanged/clearChanged 状态门、notifyObservers 两形态、deleteObservers。

## 1. API 功能

- addObserver(Observer)/deleteObserver/deleteObservers/countObservers
- setChanged()/clearChanged()/hasChanged()；notifyObservers()/notifyObservers(Object)
- JDK-9+ 已废弃（27 侧仍测试行为一致性）

## 2. 使用方式

自定义 Observer 记录 update 调用 → 无 setChanged 时 notifyObservers 不投递；notifyObservers(null) 投递 null arg。

## 3. 参考来源

- Oracle javadoc `java/util/Observable`（deprecated 9+）
- JDK 源码 Observable 状态门实现

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | setChanged 门控（未置位不投递） | testObservableContracts |
| P0 | notifyObservers(null) 投递 null；addObserver(null) NPE | testObservableContracts |
| P0 | countObservers/deleteObservers 生命周期 | testObservable |
| P0 | 契约深化：未 setChanged 不通知/参数 null 语义/deleteObservers/NPE | testObservableContracts |
