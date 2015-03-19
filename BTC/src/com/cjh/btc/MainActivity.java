package com.cjh.btc;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.cjh.btc.service.talkService;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("ResourceAsColor")
public class MainActivity extends FragmentActivity {

	private RelativeLayout RelativeLayout1, RelativeLayout2;
	private TextView textView1, textView2;
	private ViewPager viewPager;
	private int[] selectList;
	private TextView[] textViewList;
	private Button Enable_BT;

	private Button Edit_name;
	
	private TextView msg_tv;
	private TextView contact_tv;
	private TextView[] titleList;
	
	private int selectID = 0;
	public final static int REQUEST_OPEN_BT=1;
	public final static int REQUEST_DISCOVERABLE=2;
	public final static int REQUEST_TALK=5;//其他设备请求连接
	
	private Notification myNotification;
	
	
	public static boolean DEVLIST_ISCHANGE=false;//设备列表是否改变

	
	
	public final static String DEVICE_ADDRESS="device_address";
	public final static String DEVICE_NAME="device_name";
	public final static String TOAST="TOAST";
	public static String isMyRequest;
	public final static String DEVICE_ARR="DEVICE_ARR";
	
	
	public static ArrayAdapter<String> myDevice;
	public static ArrayAdapter<String> AroundDev;
	
	Set<BluetoothDevice> myBoundedDevice;
	List<String> newDevice;
	private FragmentManager fm; 
	
	public static talkService myService;
	public NotificationManager myNotificationManager ;
	public BluetoothAdapter myAdapter;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		initLayout();
		initData();
	}
	


	private void initLayout() {
		RelativeLayout1 = (RelativeLayout) findViewById(R.id.RelativeLayout1);
		RelativeLayout2 = (RelativeLayout) findViewById(R.id.RelativeLayout2);
		//RelativeLayout3 = (RelativeLayout) findViewById(R.id.RelativeLayout3);

		textView1 = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		//textView3 = (TextView) findViewById(R.id.textView3);
		
	    msg_tv= (TextView) findViewById(R.id.msg_tv);
	    contact_tv=(TextView) findViewById(R.id.contact_tv);
		
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		
		Enable_BT=(Button)findViewById(R.id.enable_bt);

		Edit_name=(Button)findViewById(R.id.edit_name);
	}

	private void initData() {
		selectList = new int[] { 0, 1};// 1 };// 0表示选中，1表示未选中(默认第一个选中)
		textViewList = new TextView[] { textView1, textView2};//, textView3 };
		titleList=new TextView[]{msg_tv,contact_tv};
		
		RelativeLayout1.setOnClickListener(listener);
		RelativeLayout2.setOnClickListener(listener);
		//RelativeLayout3.setOnClickListener(listener);
		
		newDevice=new ArrayList<String>();
		
		Enable_BT.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
			if(myAdapter.getScanMode() !=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
			{
			Intent OpenIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	        startActivityForResult(OpenIntent,MainActivity.REQUEST_DISCOVERABLE);
			}
			else
			{
				Toast.makeText(getApplication(), "您的蓝牙已可被搜索", Toast.LENGTH_SHORT).show();
			}
			
		}
		});
		
		Edit_name.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				EditName();
			}
			
		});
		

		fm=getSupportFragmentManager();
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(changeListener);
		myNotificationManager=(NotificationManager) this.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		
	}

	
	public void EditName(){
		final EditText et=new EditText(this);
		final String MyName=myAdapter.getName();
		AlertDialog editNameDialog=new AlertDialog.Builder(this)
		.setTitle("请输入您要修改的名称:")
		.setIcon(R.drawable.edit_bt_active)
		.setView(et)
		.setPositiveButton("确定修改", new DialogInterface.OnClickListener() {//设置监听器
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newName=et.getText().toString();
				if(newName.isEmpty()){
					Toast.makeText(MainActivity.this, "输入为空……", Toast.LENGTH_SHORT).show();
				}
				else
				{
					if(newName.equals(MyName))
						Toast.makeText(MainActivity.this, "您输入的名称与当前的名称相同-_-", Toast.LENGTH_SHORT).show();
					else
					if(myAdapter.setName(newName))
						Toast.makeText(MainActivity.this, "修改名称成功！", Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(MainActivity.this, "修改名称失败-_-", Toast.LENGTH_SHORT).show();
				}
				
			}
		})
				.setNegativeButton("取消", null).show();
		
		
	}
	public void onStart() {
        super.onStart();
       Log.e("Contact_Acitivity", "++ ON START ++");
       myAdapter=BluetoothAdapter.getDefaultAdapter();
		if(myAdapter==null){
			Toast.makeText(this,"本机没有蓝牙设备",Toast.LENGTH_SHORT).show();
			finish();
		}
		else
		if (myAdapter.getScanMode() !=BluetoothAdapter.STATE_ON) {
			if(!myAdapter.enable()){
				Toast.makeText(this, "蓝牙没打开", Toast.LENGTH_SHORT).show();
				finish();
			myService=talkService.getInstance(myHandler);
			}
				
			
			//Intent OpenIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	        //startActivityForResult(OpenIntent,REQUEST_OPEN_BT);
	        }
		if (myAdapter.getScanMode() ==BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
		                myService.start();
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_DISCOVERABLE){
        	 System.out.print(resultCode);
        	if (myAdapter.getScanMode() ==BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
        		Toast.makeText(this, "您的蓝牙已可被搜索", Toast.LENGTH_SHORT).show();
        		myService=talkService.getInstance(myHandler);
                myService.start();
        	}
        	else{
        		Log.d("onActivityResult()", "不可被搜索");
                Toast.makeText(this, "要想周围的朋友发现你，请先设置蓝牙可见", Toast.LENGTH_SHORT).show();
        	};
        }
		
	}

	@SuppressLint("HandlerLeak")
	private Handler myHandler = new Handler(){
		
		 public void handleMessage(Message msg) {
	            if (msg.what==REQUEST_TALK) {
	                String DEV_Address=msg.getData().getString(DEVICE_ADDRESS);
	                String DEV_Name=msg.getData().getString(DEVICE_NAME);
	                showNotification(DEV_Address,DEV_Name);
	                Log.i("REQUEST_TALK", DEV_Address+"##"+DEV_Name);
	            }
	        }
	};
	private OnClickListener listener = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.RelativeLayout1:
				if (selectID == 0) {
					return;
				} else {
					setSelectedTitle(0);
					viewPager.setCurrentItem(0);
				}
				break;
			case R.id.RelativeLayout2:
				if (selectID == 1) {
					return;
				} else {
					setSelectedTitle(1);
					viewPager.setCurrentItem(1);
				}
				break;
			}
		}
	};
	
	
    
	private FragmentPagerAdapter adapter = new FragmentPagerAdapter(
			getSupportFragmentManager()) {
		public int getCount() {
			return selectList.length;
		}

		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch (position) {
			case 0:
				fragment=new MSG_Fragment();
				break;
			case 1:
				fragment=new Contact_Fragment();
				break;
			}
			return fragment;
		}
		
	};
	private SimpleOnPageChangeListener changeListener=new SimpleOnPageChangeListener(){
		public void onPageSelected(int position) {
			setSelectedTitle(position);
		}
	};

	private void setSelectedTitle(int position) {
		for (int i = 0; i < selectList.length; i++) {
			if (selectList[i] == 0) {
				selectList[i] = 1;
				textViewList[i].setVisibility(View.INVISIBLE);
				titleList[i].setTextColor(this.getResources().getColor(R.color.black));
			}
		}
		selectList[position] = 0;
		textViewList[position].setVisibility(View.VISIBLE);
		titleList[position].setTextColor(this.getResources().getColor(R.color.selectcolor));
		selectID = position;
	}
	
	@SuppressWarnings("deprecation")
	public void showNotification(CharSequence address,CharSequence name){
		myNotification=new Notification(R.drawable.ic_launcher,"BTC",System.currentTimeMillis());
		
		myNotification.defaults=Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE;
		myNotification.flags=Notification.FLAG_AUTO_CANCEL;
		
		Intent intent=new Intent();
		intent.setClass(MainActivity.this, Talk_Activity.class);
        intent.putExtra(DEVICE_ADDRESS, address);
        intent.putExtra(DEVICE_NAME, name);
        intent.putExtra(isMyRequest, false);
		
        PendingIntent pendingintent=PendingIntent.getActivity(this, 0, intent, 0);
        myNotification.setLatestEventInfo(this, "BTC", name+"想与您对话", pendingintent);
        
        myNotificationManager.notify(0, myNotification);
	}
	
	 public boolean onCreateOptionsMenu(Menu menu) {
	      
	        getMenuInflater().inflate(R.menu.main, menu);
	        
	        return true;
	    }
	   @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	        if(item.getItemId()==R.id.action_exit) 
	        {
	        	if(myAdapter.isEnabled()){
	        	    if(myAdapter.isDiscovering())
	        		    myAdapter.cancelDiscovery();
	        	    myAdapter.disable();
	            }
	        	finish();
	        }
	        return false;
	    }

	
}


