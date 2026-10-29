module m {
    exports api;
    exports p;
    uses api.Service;
    provides api.Service with impl.ServiceImpl;
}
