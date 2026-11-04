# java.util.Observer 测试设计案（JDK-8）

> 覆盖目标：完整契约：update(Observable,Object) 回调参数与多观察者通知顺序（内部注册序）。

## 1. API 功能

- 接口方法：update(Observable o, Object arg)

## 2. 使用方式

多 Observer 依次注册 → notifyObservers 按注册序回调；update 收到的 Observable 引用与 arg 精确。

## 3. 参考来源

- Oracle javadoc `java/util/Observer`（deprecated 9+）

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 回调参数（observable 引用、arg）精确 | testObserver |
| P0 | 多观察者注册序通知 | testObserverContracts |
| P0 | 契约深化：update 回调参数矩阵（直接调用契约） | testObserverContracts |
