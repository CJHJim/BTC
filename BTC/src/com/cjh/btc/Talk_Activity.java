package com.cjh.btc;


import java.util.ArrayList;
import java.util.List;

import com.cjh.btc.dao.DB_Dao;
import com.cjh.btc.domain.MSG;
import com.cjh.btc.service.MsgAdapter;
import com.cjh.btc.service.talkService;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;

public class Talk_Activity extends Activity {

	public final static int MSGACTION_READ=6;
	public final static int MSGACTION_WRITE=7;

	public static final int MESSAGE_DEVICE_NAME = 3;
	public final static int CONNECTING=8;//提示正在连接
	public final static int MSG_TOAST=9;//弹出土司提示
	
	public final static int CONNECT_STATE=10;
	
	public final static String NAME="MyDevice";
	public final static String TOAST="TOAST";
	public final static String ISMY_MSG="SEND_OR_READ_MSG";
	public final static String MSG_CONTENT="MSG_CONTENT";
	
	private String ur_Address;
	private String ur_Name;
	private boolean isMyRequest;
	
	private final BluetoothAdapter myAdapter=BluetoothAdapter.getDefaultAdapter();//用户自己的蓝牙设备
	private BluetoothDevice urDevice=null;//连接对象的蓝牙设备
	
	private talkService talkservice=null;//
	
	private TextView talkTitle;
	private TextView talkState;
	private Button sendMSG;
	private EditText et;
	private ListView msg_lv;
	
	private MsgAdapter myMsgAdapter;
	private List<MSG> msgList=new ArrayList<MSG>();
	private DB_Dao myDatabase;
	private long lastMsgId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_talk);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.talk_title);

		//初始化界面
		talkTitle=(TextView) findViewById(R.id.ur_name);
		talkState=(TextView) findViewById(R.id.state);
		sendMSG=(Button) findViewById(R.id.ib_send);
		et= (EditText) findViewById(R.id.et_MSG);
		msg_lv=(ListView) findViewById(R.id.lV);
		
		Intent intent= getIntent();
		ur_Address=new String();
		ur_Name=new String();
		ur_Address=intent.getStringExtra(MainActivity.DEVICE_ADDRESS);//获取设备地址
		ur_Name=intent.getStringExtra(MainActivity.DEVICE_NAME);//获取设备名字
		isMyRequest=intent.getBooleanExtra(Contact_Fragment.isMyRequest,true);//获取是否是自己发送的对话请求
		talkTitle.setText(ur_Name);
		initData();
		
	}

	private void initData(){
		urDevice = myAdapter.getRemoteDevice(ur_Address);
		myDatabase=new DB_Dao(this);

		//如果设备不存在数据库中，加入将该设备数据库中
		if(!myDatabase.QueryContact(ur_Address))
		{
			Log.e("添加设备到数据库", "tianjia");
			long x=myDatabase.addContact(ur_Name, ur_Address);
			Log.e("添加设备到数据库", Long.toString(x));
		}
		else{
			Log.e("添加设备到数据库", "已经存在了？");
		}
        //如果数据库中有该设备的消息记录，则导出显示在界面上
		if(myDatabase.isRecordExist(ur_Address)){
			Log.e("消息记录", "有消息记录？");
		    msgList=myDatabase.getMsgRecord(ur_Address);
		}
		else{
			Log.e("消息记录", "没有消息记录？");
		}
		talkservice=talkService.getInstance(myHandler);
		if(isMyRequest){
			System.out.println("我找你的呀！");
			talkservice.connect(urDevice);
		}
		else
		{
			System.out.println("你找我的呀！");
			talkservice.setMyState(talkService.STATE_CONNECTED);
			myHandler.obtainMessage(CONNECT_STATE,talkservice.getMyState(),-1).sendToTarget();
		}
		//获取以前的聊天记录
		
		myMsgAdapter=new MsgAdapter(this,msgList);
		msg_lv.setAdapter(myMsgAdapter);
	}
	
	public void Send_Msg(View view){
		String content=et.getText().toString();
		System.out.print(content.toString());
		if(talkservice.getMyState()!=talkService.STATE_CONNECTED)
		{
			Toast.makeText(this, "您的设备未连接", Toast.LENGTH_SHORT).show();
			return;
		}
		if(content.isEmpty()){
			Toast.makeText(this, "不能发送空消息", Toast.LENGTH_SHORT).show();
		}
		else{
			byte[] msg = content.getBytes();
			talkservice.write(msg);
			content="";
			et.setText(content);
		}
	}
	
	private Handler myHandler=new Handler(){
		
		 public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case CONNECT_STATE:
	                switch (msg.arg1) {
	                case talkService.STATE_CONNECTED:
	                    talkState.setText("已连接");
	                    et.setHint("请输入消息^_^");
	                    break;
	                case talkService.STATE_CONNECTING:
	                    talkState.setText("正在连接……");
	                    et.setHint("正在连接……");
	                    break;
	                case talkService.STATE_LISTEN:
	                	talkState.setText("未连接");
	                	et.setHint("未连接");
	                	break;
	                case talkService.STATE_LOST:
	                	talkState.setText("未连接");
	                	et.setHint("未连接");
	                	if(isMyRequest)
	                	{
	                	   talkservice.connect(urDevice);
	                	}
	                	else
	                		talkservice.start();
	                    break;
	                case talkService.STATE_STOP:
		            	finish();
		            	break;
	                }
	            
	                break;
	            case MSGACTION_WRITE:
	            case MSGACTION_READ:
	            	
	            	Bundle bundle=msg.getData();
	            	int is_mine=bundle.getInt(ISMY_MSG);
	            	String content=bundle.getString(MSG_CONTENT);
	            	MSG msg_enity=new MSG(content,is_mine);
	            	lastMsgId=myDatabase.addRecord(ur_Address, msg_enity);
	            	msgList.add(msg_enity);
	            	myMsgAdapter.notifyDataSetChanged();
	            	Log.e("保存信息", content+"\n真实信息内容：\n"+msg_enity.getContent()+"##"+msg_enity.getTime());
	                break;
	            case MSG_TOAST:
	                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
	                               Toast.LENGTH_SHORT).show();
	                break;
	            }
	        }
	};
	
	
	@Override
	protected void onPause() {
		Log.e("PAUSE", "PAUSE");
		
		super.onPause();
	}

	
	@Override
	protected void onStop() {
		if (talkservice != null) 
        	talkservice.stop();
		if(lastMsgId!=0){
           long x=myDatabase.addLastMsgId(lastMsgId, ur_Address);
           if(x==0)
       	      Log.i("PAUSE!", "添加记录失败");
       
           else
       	      Log.i("PAUSE!", "添加记录成功");
		}
		super.onStop();
	}

	 public boolean onKeyDown(int keyCode, KeyEvent event)  
	    {  
	        if (keyCode == KeyEvent.KEYCODE_BACK )  
	        {  
	           onStop();
	           finish();
	        }  
	          
	        return false;  
	          
	    }  
}
