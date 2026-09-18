# Add project specific ProGuard rules here.
# Xem: https://developer.android.com/studio/build/shrink-code

# Room: giữ lại các Entity/DAO để tránh lỗi khi bật minify
-keep class com.vocatclone.app.data.local.entity.** { *; }
