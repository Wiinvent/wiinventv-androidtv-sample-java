
### Version 1.1.10 
Change log: hỗ trợ thêm run midroll & 2 quảng cáo liên tiếp

#### 1. Nâng version
```gralde
implementation 'tv.wiinvent:wiinvent-sdk-android:1.1.10'
```

#### 2. Cập nhật config
    Chỉnh sửa init bỏ biến alwaysCustomSkip

    ==> InStreamManager.Companion.getInstance().init(requireContext(), "14", DeviceType.TV, Environment.SANDBOX, 5, 10, 5, 2500, LevelLog.BODY, 8);
