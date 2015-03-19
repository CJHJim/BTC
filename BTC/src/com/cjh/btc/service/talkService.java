/*
 * 采用单例对象设计模式
 */

package com.cjh.btc.service;


import java.io.IOException;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.cjh.btc.MainActivity;
import com.cjh.btc.Talk_Activity;
import com.cjh.btc.domain.MSG;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class talkService {

	
	
	private static final UUID MY_UUID =UUID.fromString("cf620832-f9d5-42c5-8e5f-6d72d60aa1dc");
	
	
	public final static int STATE_LOST=1;     //没有进行任何操作
	public final static int STATE_LISTEN=2;   //作为服务端正在监听
	public final static int STATE_CONNECTED=3;//已经连接
	public final static int STATE_CONNECTING=4;//连接中
	public final static int STATE_STOP=5;
	private int myState;//当前状态
	
	
	private final BluetoothAdapter myAdapter;//用户自己的蓝牙设备
	private static Handler myHandler;        //
	private AcceptRequestThread myAcceptThread;    //处理作为服务端连接请求的线程
    private ConnectRequestThread myConnectThread;  //处理作为客户端连接请求的线程
    private talkingThread mytalkingThread;//处理已连接的信道的线程
	
	public talkService(Handler handler) {
		myAdapter=BluetoothAdapter.getDefaultAdapter();
		myState=STATE_LOST;
		myHandler=handler;
		
	}

	private static class talkServiceHolder{
		static final talkService INSTANCE=new talkService(myHandler);
	}
	
	public static talkService getInstance(Handler handler){
		talkServiceHolder.INSTANCE.setHandler(handler);
		return talkServiceHolder.INSTANCE;
		
	}
	public void setHandler(Handler handler){
		myHandler=handler;
	}
	
	public int getMyState() {
		return myState;
	}


	public synchronized void setMyState(int myState) {
		this.myState = myState;
	}


	public static UUID getMyUuid() {
		return MY_UUID;
	}

	
	
	 public synchronized void start() {
		 
	        // 取消现在的连接请求
	        if (myConnectThread != null) {
	        	myConnectThread.cancel();
	        	myConnectThread = null;
	        	}

	        // 断开现在已经连接的连接
	        if (mytalkingThread != null) {
	        	mytalkingThread.cancel(); 
	        	mytalkingThread = null;
	        	}

	        // 开始一个新的监听
	        if (myAcceptThread == null) {
	        	myAcceptThread = new AcceptRequestThread();
	        	myAcceptThread.start();
	        }
	        setMyState(STATE_LISTEN);

	        myHandler.obtainMessage(Talk_Activity.CONNECT_STATE, myState, -1).sendToTarget();
	    }
	 
	 
	    public synchronized void connect(BluetoothDevice device) {
	    	
	       
	        if (myState == STATE_CONNECTING) {
	            if (myConnectThread != null) {
	            	     myConnectThread.cancel();
	            	     myConnectThread = null;
	            	}
	        }

	        if(myAcceptThread!=null){
	        	myAcceptThread.cancel();
	        	myAcceptThread=null;
	        }
	        if (mytalkingThread != null) {
	        	mytalkingThread.cancel(); 
	        	mytalkingThread = null;
	        	}

	       
	        myConnectThread = new ConnectRequestThread(device);
	        Log.i("myConnectThread", "开始请求连接线程"+device.getName().toString());
	        myConnectThread.start();
	        
	        setMyState(STATE_CONNECTING);
	        myHandler.obtainMessage(Talk_Activity.CONNECT_STATE, myState, -1).sendToTarget();
	    }
	 
		public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

			// 取消现在的连接请求
	        if (myConnectThread != null) {
	        	myConnectThread.cancel(); 
	        	myConnectThread = null;
	        	}

	        // 断开现在已经连接的连接
	        if (mytalkingThread != null) {
	        	mytalkingThread.cancel();
	        	mytalkingThread = null;
	        	}

	        // 取消请求连接请求
	        if (myAcceptThread != null) {
	        	myAcceptThread.cancel(); 
	        	myAcceptThread = null;
	        	}

	        // 开始一个新的连接
	        mytalkingThread = new talkingThread(socket);
	        mytalkingThread.start();
	        setMyState(STATE_CONNECTED);
	        myHandler.obtainMessage(Talk_Activity.CONNECT_STATE, myState, -1).sendToTarget();
	    }
		
		 public synchronized void stop() {
		        if (myConnectThread != null) {myConnectThread.cancel(); myConnectThread = null;}
		        if (mytalkingThread != null) {mytalkingThread.cancel(); mytalkingThread = null;}
		        if (myAcceptThread != null) {myAcceptThread.cancel(); myAcceptThread = null;}
		        setMyState(STATE_STOP);
		        myHandler.obtainMessage(Talk_Activity.CONNECT_STATE, myState, -1).sendToTarget();
		    }
		 
		 
		 private void connectionFailed() {
		        setMyState(STATE_LISTEN);
		        Message msg = myHandler.obtainMessage(Talk_Activity.MSG_TOAST);
		        Bundle bundle = new Bundle();
		        bundle.putString(Talk_Activity.TOAST, "请求连接失败");
		        msg.setData(bundle);
		        myHandler.sendMessage(msg);
		        myHandler.obtainMessage(Talk_Activity.CONNECT_STATE, myState, -1).sendToTarget();
		    }
		 
		 
		 public void write(byte [] msg) {
			  
			    talkingThread temp;
			    
		        synchronized (this) {
		            if (myState != STATE_CONNECTED) 
		            	return;//如果当前状态不是已经连接就直接结束函数
		            temp = mytalkingThread;
		            
		        }
			    temp.write(msg);
		    }
		  
		  private void connectionLost() {
		        setMyState(STATE_LOST);

		        Message msg = myHandler.obtainMessage(Talk_Activity.MSG_TOAST);
		        Bundle bundle = new Bundle();
		        bundle.putString(Talk_Activity.TOAST, "断开连接");
		        msg.setData(bundle);
		        myHandler.sendMessage(msg);
		        myHandler.obtainMessage(Talk_Activity.CONNECT_STATE, myState, -1).sendToTarget();
		        
		    }
	//处理来自外界的请求
	private class AcceptRequestThread extends Thread {
        //作为服务器的socket
        private final BluetoothServerSocket myServerSocket;

		public AcceptRequestThread() {
			
			//临时socket对象
            BluetoothServerSocket temp = null;
            try {
                temp = myAdapter.listenUsingRfcommWithServiceRecord("LET'S TALK!", MY_UUID);
            } catch (IOException e) {
            	
            }
            myServerSocket = temp;
        }

 
		//实现线程的run函数
		public void run() {
			//给线程设置名称
            setName("TalkToMe");
            
            BluetoothSocket socket = null;

            System.out.println("UUID:"+MY_UUID);
            // 当没有连接上时，持续监听直到超时
            while (myState != STATE_CONNECTED) {
                try {
                    socket = myServerSocket.accept();
                    Log.i("myAcceptThread", "监听线程运行");
                } catch (IOException e) {
                    //
                    break;
                }

                // 监听到信号时
                if (socket != null) {
                	//由于蓝牙是一对一的通信，所以一个BTC程序中某个时间只能有一个talkService对象
                	
                    synchronized (talkService.this) {
                        switch (myState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                        	Message msg = myHandler.obtainMessage(MainActivity.REQUEST_TALK);
            		        Bundle bundle = new Bundle();
            		        bundle.putString(MainActivity.DEVICE_ADDRESS, socket.getRemoteDevice().getAddress());
            		        bundle.putString(MainActivity.DEVICE_NAME, socket.getRemoteDevice().getName());
            		        Log.i("myAcceptThread",socket.getRemoteDevice().getAddress().toString()+"##"+ socket.getRemoteDevice().getName().toString());
            		        msg.setData(bundle);
            		        myHandler.sendMessage(msg);
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_LOST:
                        case STATE_CONNECTED:
                            //发送消息后关闭信道
                            try {
                                socket.close();
                            } catch (IOException e) {
                                //
                            }
                            break;
                        }
                    }
                    //cancel();
                }
            }
        }

		public void cancel() {
            try {
                myServerSocket.close();//关闭通信
           
            } catch (IOException e) {
                
            }
        }
    }
	
	/*
	 * 用来处理向外界发起连接请求的线程
	 */
	private class ConnectRequestThread extends Thread{
		private final BluetoothSocket mySocket;
		private final BluetoothDevice myDevice;
		
		public ConnectRequestThread(BluetoothDevice device){
			myDevice=device;
			
			BluetoothSocket tempSocket=null;
			try{
				//创建一个RFCOMM连接
		         	tempSocket=device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
			}
             catch (IOException e) {
            	 Log.i("myConnectThread", "创建线程失败");
             }
			Log.i("myConnectThread", tempSocket.getRemoteDevice().getAddress().toString());
			mySocket=tempSocket;
		}
		public void run(){
			//设置线程名称
			setName("TalkToU");
			//如果设备还在搜索，就停止搜索，避免浪费系统资源
			if(myAdapter.isDiscovering())
        	{
            	System.out.println("还在搜索，停止搜索");
        	    myAdapter.cancelDiscovery();
        	}
			//if(myDevice.)
			Log.i("myConnectThread", "尝试请求连接线程");
			try{
				//尝试连接目的设备
				mySocket.connect();
			}
			catch(IOException e){
				connectionFailed();
				try {
					mySocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				talkService.this.start();
				return;
				
			}
			 synchronized (talkService.this) {
	                myConnectThread = null;
	            }
			 connected(mySocket, myDevice);

		}
		
		public void cancel(){
			
			try {
                mySocket.close();//关闭通信
           
            } catch (IOException e) {
                
            }
		}
		
	}
	
	private class talkingThread extends Thread{
		
		private final BluetoothSocket mySocket;
		private final InputStream inputInfo;
		private final OutputStream outputInfo;
		
		public talkingThread(BluetoothSocket socket){
			mySocket=socket;
			InputStream tempIn = null;
            OutputStream tempOut = null;

            // 获得信道的输入输出流
            try {
                tempIn = socket.getInputStream();
                tempOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            inputInfo = tempIn;
            outputInfo = tempOut;
		}
		
		public void run(){
			byte[] buffer = new byte[1024];
            int bytes;
            //持续监听信道动静
           while(true){
        	   try {
        		   myHandler.obtainMessage(talkService.STATE_CONNECTED).sendToTarget();//同时发送一个消息
                   bytes = inputInfo.read(buffer);
                   // 显示到对话界面上去
                   
                String content=new String(buffer,0,bytes);
                
                System.out.print(content);
               	Message msg = myHandler.obtainMessage(Talk_Activity.MSGACTION_READ);
               	Bundle bundle=new Bundle();
               	bundle.putInt(Talk_Activity.ISMY_MSG, 0);
               	bundle.putString(Talk_Activity.MSG_CONTENT, content);
               	msg.setData(bundle);
               	myHandler.sendMessage(msg);
               } catch (IOException e) {
                   connectionLost();
                   break;
               }
           }
		}
		public void write(byte[] buffer) {
            try {
            	
                
                
               
            	outputInfo.write(buffer);
            	outputInfo.flush();

                // 显示发送的信息到对话界面上
            	String content=new String(buffer,0,buffer.length);
            	System.out.print(content);
               	Message msg = myHandler.obtainMessage(Talk_Activity.MSGACTION_WRITE);
               	Bundle bundle=new Bundle();
               	bundle.putInt(Talk_Activity.ISMY_MSG, 1);
               	bundle.putString(Talk_Activity.MSG_CONTENT, content);
               	msg.setData(bundle);
               	myHandler.sendMessage(msg);
            } catch (IOException e) {
            	myHandler.obtainMessage(Talk_Activity.MSG_TOAST,"发送失败").sendToTarget();
            }
        }

		public void cancel() {
            try {
                mySocket.close();
            } catch (IOException e) {
            	myHandler.obtainMessage(Talk_Activity.MSG_TOAST,"发送失败").sendToTarget();
            }
        }
    }
		
}
	
