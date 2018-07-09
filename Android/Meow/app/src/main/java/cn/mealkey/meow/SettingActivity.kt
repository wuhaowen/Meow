package cn.mealkey.meow

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.*
import de.mannodermaus.rxbonjour.BonjourEvent
import de.mannodermaus.rxbonjour.RxBonjour
import de.mannodermaus.rxbonjour.drivers.jmdns.JmDNSDriver
import de.mannodermaus.rxbonjour.platforms.android.AndroidPlatform
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class SettingActivity : AppCompatActivity() {
    lateinit var rxBonjour:RxBonjour
    var disposable:Disposable? = null
    lateinit var rg:RadioGroup
    lateinit var txtIP:EditText
    lateinit var txtPort:EditText
    lateinit var txtSelect:TextView
    lateinit var rcy:RecyclerView
    lateinit var device:TextView
    var adapter:ServerAdapter? = null
    var servers:MutableList<Server> = mutableListOf()
    var host = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        rg = findViewById(R.id.rg_mode)
        device = findViewById(R.id.txt_name)
        txtIP = findViewById(R.id.txt_ip)
        txtPort = findViewById(R.id.txt_port)
        txtSelect = findViewById(R.id.txt_select)
        rcy = findViewById(R.id.rcy_server)
        findViewById<Button>(R.id.btn_save).setOnClickListener({
            var deviceName = device.text.toString()
            if (TextUtils.isEmpty(deviceName)){
                Toast.makeText(this,"设备名称不能为空",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (rg.checkedRadioButtonId == R.id.rb_ip){
                host = txtIP.text.toString() + ":"+txtPort.text.toString()
            }
            if (TextUtils.isEmpty(host)){
                Toast.makeText(this,"服务器地址不能为空",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var sp = this.getSharedPreferences("meow", Context.MODE_PRIVATE)
            sp.edit().putString("deviceName",deviceName).putString("host",host).apply()
            finish()
        })

        rg.setOnCheckedChangeListener { group, i ->
            run {
                when (i) {
                    R.id.rb_ip -> ipMode()
                    else -> discover()
                }
            }
        }
        rxBonjour = RxBonjour.Builder()
                .platform(AndroidPlatform.create(this))
                .driver(JmDNSDriver.create())
                .create()

    }

    fun ipMode(){
        txtSelect.visibility = View.GONE
        rcy.visibility = View.GONE
        txtIP.visibility = View.VISIBLE
        txtPort.visibility = View.VISIBLE
        servers.clear()
        txtSelect.text = ""
    }

    fun discover(){
        txtSelect.visibility = View.VISIBLE
        rcy.visibility = View.VISIBLE
        txtIP.visibility = View.GONE
        txtPort.visibility = View.GONE
        if (adapter == null){
            rcy.layoutManager = LinearLayoutManager(this)
            adapter = ServerAdapter(servers,{
                host = "${it.ip}:${it.port}"
                txtSelect.text = host
            })
            rcy.adapter = adapter
        }

        disposable?.dispose()
        disposable = null
        disposable = rxBonjour.newDiscovery("_http._tcp")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { event ->
                            when(event) {
                                is BonjourEvent.Added -> {
                                    with(event.service){
                                        val server = Server(name,host.hostAddress,port.toString())
                                        servers.add(server)
                                        adapter?.notifyDataSetChanged()
                                    }
                                }
                                is BonjourEvent.Removed -> println("Lost Service: ${event.service}")
                            }
                        },
                        { error -> println("Error during Discovery: ${error.message}") }
                )
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }
}
