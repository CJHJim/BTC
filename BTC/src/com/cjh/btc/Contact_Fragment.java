package com.cjh.btc;



import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cjh.btc.domain.MSG;
import com.cjh.btc.service.talkService;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

/**
 * @author CJH
 *
 */
public class Contact_Fragment extends Fragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		super.onActivityCreated(savedInstanceState);
	}

	public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setUserVisibleHint(true);
    }
	
	public final static int CONNECT_STATE=1;
	public final static int MSG_TOAST=2;//弹出土司提示

	public final static String TOAST="TOAST";
	public final static String TIP="要想查看到周围的设备，请点击搜索键";
	
	private  BluetoothAdapter myAdapter;
	
	ListView BoundDev;
	ListView aroundDev;
	ProgressDialog mypDialog;
	private Button discovery;
	
	public static Activity myActivity;
	
	private ArrayAdapter<String> myDevice;
	private ArrayAdapter<String> AroundDev;
	
	Set<BluetoothDevice> myBoundedDevice;
	
	public static String isMyRequest;
	public final static String NO_DEVICE="附近没有设备";
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		myActivity=activity;
		super.onAttach(activity);
	}
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_contact, container, false);
		BoundDev=(ListView) v.findViewById(R.id.lV_BoundDevice);
		aroundDev=(ListView) v.findViewById(R.id.lV_around_device);
		return v;
		
	}

    @SuppressWarnings("unchecked")
	public void onResume() {
		super.onResume();
		
		discovery=(Button)myActivity.findViewById(R.id.discovery);
		myAdapter=BluetoothAdapter.getDefaultAdapter();
		myDevice=new ArrayAdapter<String>(myActivity,R.layout.device_name);
	    AroundDev=new ArrayAdapter<String>(myActivity,R.layout.device_name);
        BoundDev.setAdapter(myDevice);
        BoundDev.setOnItemClickListener(myListener);
        aroundDev.setAdapter(AroundDev);
        aroundDev.setOnItemClickListener(myListener);
        
        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_FOUND); 
		myActivity.registerReceiver(myReceiver, filter);
        filter=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        myActivity.registerReceiver(myReceiver,filter);
        
        AroundDev.add(TIP);
        aroundDev.setEnabled(false);
        
        
        myBoundedDevice=myAdapter.getBondedDevices();
        if (myBoundedDevice.size() > 0) {
        	for(BluetoothDevice tem:myBoundedDevice)
            	myDevice.add(tem.getName().toString()+"\n"+tem.getAddress().toString());
        	BoundDev.setEnabled(true);
        } else {
        	myDevice.add("没有已配对蓝牙");
        	BoundDev.setEnabled(false);
        }
        
		mypDialog = new ProgressDialog(myActivity);
		
		mypDialog.setOnCancelListener(new OnCancelListener(){

			@Override
			public void onCancel(DialogInterface dialog) {
				if(myAdapter.isDiscovering())
					myAdapter.cancelDiscovery();
				
			}
			
		});
		discovery.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Discovery();
			}
        	
        });
		
	}
    public void Discovery(){
    	AroundDev.clear();
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setMessage("正在搜索…………");
    	mypDialog.setIndeterminate(false);
    	myActivity.setProgressBarIndeterminateVisibility(true);
    	mypDialog.show();
    	if(myAdapter.isDiscovering())
    	{
    	    myAdapter.cancelDiscovery();
    	}
    	myAdapter.startDiscovery();
    	System.out.println("在搜索");
    	
		
	}
    
    private OnItemClickListener myListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // 取消蓝牙搜索行为
        	if(myAdapter.isDiscovering())
        	{
            	System.out.println("还在搜索，停止搜索");
        	    myAdapter.cancelDiscovery();
        	}

            // 
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            System.out.println(address);
            
            
            String name=info.substring(0,info.length() - 17);
            System.out.println(name);
            
            Intent intent = new Intent();
            intent.setClass(myActivity, Talk_Activity.class);
            intent.putExtra(MainActivity.DEVICE_ADDRESS, address);
            intent.putExtra(MainActivity.DEVICE_NAME, name);
            intent.putExtra(isMyRequest, true);
            startActivity(intent);
        }
    };
    
   
	private final BroadcastReceiver myReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //当扫描到周围的设备时
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            	
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                AroundDev.add(device.getName() + "\n" + device.getAddress());
                System.out.print(device.getName() + "\n" + device.getAddress());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	mypDialog.cancel();
            	int num= AroundDev.getCount();
            	if(num==0)
            	{
            		Toast.makeText(myActivity, "搜索结束,附近没有设备", Toast.LENGTH_SHORT).show();
            		AroundDev.add(NO_DEVICE);
            		aroundDev.setEnabled(false);
            	}
            	else
            	{
            	    Toast.makeText(myActivity, "搜索结束", Toast.LENGTH_SHORT).show();
            	    aroundDev.setEnabled(true);
            	}
            	
            }
        }

    };

	@Override
	public void onDestroy() {
		  myActivity.unregisterReceiver(myReceiver);
		super.onDestroy();
	}
    
}
