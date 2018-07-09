package cn.mealkey.meow

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ServerAdapter (private var servers: List<Server>, private val itemClick:(Server)->Unit) : RecyclerView.Adapter<Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_server,parent,false))
    }

    override fun getItemCount(): Int  = servers.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val s = servers[position]
        holder.txtName.text = s.name
        holder.txtIp.text = s.ip
        holder.itemView.setOnClickListener({
            itemClick.invoke(s)
        })
    }
}

class Holder :RecyclerView.ViewHolder{
    lateinit var txtName:TextView
    lateinit var txtIp:TextView

    constructor(itemView: View) : super(itemView){
        txtName = itemView.findViewById(R.id.txt_name)
        txtIp = itemView.findViewById(R.id.txt_ip)
    }
}