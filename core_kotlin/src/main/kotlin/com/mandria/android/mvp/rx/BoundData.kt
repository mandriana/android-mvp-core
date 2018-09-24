package com.mandria.android.mvp.rx

import io.reactivex.Notification

/** This class binds a typed <View> [view] to some typed <Result> [data]. **/
data class BoundData<View, Result>(val view: View?, val data: Notification<Result>)