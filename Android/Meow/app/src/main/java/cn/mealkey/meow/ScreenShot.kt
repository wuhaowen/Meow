package cn.mealkey.meow

import android.app.Activity
import android.util.Log
import com.github.piasy.rxscreenshotdetector.RxScreenshotDetector



class ScreenShot {
    fun startObserver(context: Activity){
        RxScreenshotDetector.start(context)
                .subscribe({ path ->
                    run {
                        MeowApplication.instance?.upload(path)
                    }
                })
    }

}