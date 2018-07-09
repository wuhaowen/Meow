package cn.mealkey.meow

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.provider.Settings





class MainActivity : AppCompatActivity() {

    lateinit var txtInfo:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       // startService(Intent(this,NSDService::class.java))
        txtInfo = findViewById(R.id.txt_info)
        findViewById<Button>(R.id.btn_setting).setOnClickListener { startActivity(Intent(this,SettingActivity::class.java)) }
        ScreenShot().startObserver(this)

    }


    override fun onResume() {
        super.onResume()
        txtInfo.text = ""
        var sp = this.getSharedPreferences("meow", Context.MODE_PRIVATE)
        var deviceName = sp.getString("deviceName","")
        var host = sp.getString("host","")
        var f = true
        if (TextUtils.isEmpty(deviceName)){
            txtInfo.append("设备名称为空，请设置\n")
            f = false
        }else{
            txtInfo.append("设备名称："+deviceName+"\n")
        }
        if (TextUtils.isEmpty(host)){
            txtInfo.append("服务器地址为空，请设置\n")
            f = false
        }else{
            txtInfo.append("服务器地址："+host+"\n")
        }
        if (f){
            txtInfo.append("尝试连接")

            MeowApplication.instance?.createClient(host)
        }
    }
}
