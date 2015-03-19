/*
 * 这个类用来显示曾经的聊天消息
 * 
 * */
package com.cjh.btc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.cjh.btc.dao.DB_Dao;
import com.cjh.btc.domain.MSG;
import com.cjh.btc.domain.MSGListEnity;
import com.cjh.btc.service.MSGListAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MSG_Fragment extends Fragment {

	private ListView MsgList;
	
	private MSGListAdapter mylistAdapter;
	private DB_Dao myDatabase;
	private List<MSGListEnity> msgList=new ArrayList<MSGListEnity>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v=inflater.inflate(R.layout.fragment_msg, container, false);
		MsgList=(ListView) v.findViewById(R.id.lV);
		return v;
	}

	public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setUserVisibleHint(true);
    }
	@Override
	public void onResume() {
		super.onResume();
		myDatabase=new DB_Dao(this.getActivity());
		msgList=myDatabase.getMsgList();
		
		mylistAdapter=new MSGListAdapter(this.getActivity(),msgList);
		MsgList.setAdapter(mylistAdapter);
		Log.e("消息列表", "个数为"+msgList.size());
		MsgList.setOnItemClickListener(myShortListener);
		MsgList.setOnItemLongClickListener(myLongListenner);

}
	
	private OnItemLongClickListener myLongListenner=new OnItemLongClickListener(){

		@Override
		public boolean onItemLongClick(AdapterView<?> x, View v,
				int P, long arg3) {
			TextView temp=(TextView)v.findViewById(R.id.tv_NA);
			 String info = temp.getText().toString();
	            String address = info.substring(info.length() - 17);
	            String name=info.substring(0,info.length() - 17);
	            DleteMsg(address,name,P);
			return true;
		}
		
	};
	private OnItemClickListener myShortListener=new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int arg2,
				long arg3) {
			TextView temp=(TextView)v.findViewById(R.id.tv_NA);
			    String info = temp.getText().toString();
	            String address = info.substring(info.length() - 17);            
	            String name=info.substring(0,info.length() - 17);
	            
	            Intent intent = new Intent();
	            intent.setClass(getActivity(), Talk_Activity.class);
	            intent.putExtra(MainActivity.DEVICE_ADDRESS, address);
	            intent.putExtra(MainActivity.DEVICE_NAME, name);
	            intent.putExtra(Contact_Fragment.isMyRequest, true);
	            startActivity(intent);
			
		}};
		
		public void DleteMsg(final String Address,String name,final int P){
			AlertDialog DeleteDialog=new AlertDialog.Builder(getActivity())
			.setTitle(name)
			.setIcon(R.drawable.edit_bt_active)
			.setPositiveButton("删除该聊天", new DialogInterface.OnClickListener() {//设置监听器
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(myDatabase.deleteRecords(Address)!=0){
						msgList.remove(P);
		             mylistAdapter.notifyDataSetChanged();
					}
				}
			})
					.setNegativeButton("取消", null).show();
			
		};
		
}


