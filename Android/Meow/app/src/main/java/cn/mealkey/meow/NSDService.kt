package cn.mealkey.meow

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.net.nsd.NsdManager
import android.widget.Toast
import android.net.nsd.NsdServiceInfo
import android.util.Log


class NSDService :Service() {

    override fun onCreate() {
        super.onCreate()
        Thread(Runnable { discoverService() }).start();
    }

    var nsdManager :NsdManager? = null
    lateinit var discoveryListener : NsdManager.DiscoveryListener

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    fun discoverService() {

        val mResolverListener = object : NsdManager.ResolveListener{
            override fun onResolveFailed(p0: NsdServiceInfo, p1: Int) {
                Log.d("mdns",p0.toString()+"onResolveFailed")

            }

            override fun onServiceResolved(p0: NsdServiceInfo) {
                Log.d("mdns","onServiceResolved")
                Log.d("mdns",p0.toString())
                Log.d("mdns",p0.serviceName)
                Log.d("mdns",p0.port.toString())
                Log.d("mdns",p0.serviceType)
                var url = "http://"+p0.host.hostAddress+":"+p0.port
                MeowApplication.instance?.createClient(baseUrl = url)
                nsdManager?.stopServiceDiscovery(discoveryListener)

            }
        }


        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Toast.makeText(applicationContext, "Stop Discovery Failed", Toast.LENGTH_SHORT).show()

            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Toast.makeText(applicationContext,
                        "Start Discovery Failed", Toast.LENGTH_SHORT).show()
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Toast.makeText(applicationContext, "Service Lost", Toast.LENGTH_SHORT).show()
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d("mdns",serviceInfo.toString())
                Log.d("mdns",serviceInfo.serviceName)
                Log.d("mdns",serviceInfo.port.toString())
                Log.d("mdns",serviceInfo.serviceType)

                Toast.makeText(applicationContext, "onServiceFound", Toast.LENGTH_SHORT).show()

                nsdManager?.resolveService(serviceInfo, mResolverListener)


            }

            override fun onDiscoveryStopped(serviceType: String) {
                Toast.makeText(applicationContext, "Discovery Stopped", Toast.LENGTH_SHORT).show()
            }

            override fun onDiscoveryStarted(serviceType: String) {
                Toast.makeText(applicationContext, "Discovery Started", Toast.LENGTH_SHORT).show()
            }
        }
        nsdManager = applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager
        nsdManager?.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}