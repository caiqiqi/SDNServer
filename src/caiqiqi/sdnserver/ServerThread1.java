package caiqiqi.sdnserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.HashMap;
//import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVWriter;

public class ServerThread1 implements Runnable{

	public static String TAG ="ServerThread1";
	
	/**最近一次接收到的时间 */
	private long lastReceiveTime = System.currentTimeMillis();
	/**两次接收的延迟时间 3s */
	private long receiveTimeDelay=3000;
	/** 当前线程处理的Socket */
	private Socket s;

	private InputStream is;
	private ObjectInputStream ois;

	/** 服务器端接收到的List<ScanResult> */
	private Object mList;
	
	private boolean isRunning = true;
	
	/**
	 * 以BSSID为键，String[]为值
	 */
	private static Map<String,String[]> mHashMap = new HashMap<String,String[]>();
	
	private File mCSVFile;
	private CSVWriter mWriter;
	
	public ServerThread1(Socket s ) throws IOException {
		
		this.s = s;
		mHashMap = new HashMap<String,String[]>();
	}
	
	
	
	@Override
	public void run() {

		System.out.println("Socket " + this.s.getRemoteSocketAddress() + " accepted");
		while (isRunning) {

			try {

				doThings();

			} catch (IOException e) {
				// 碰到异常则说明Socket已断开，则服务器这边也断开这个Socket
				e.printStackTrace();
				// try {
				//
				// this.is.close();
				// this.s.close();
				// this.is = null;
				// this.s = null;
				//
				// } catch (IOException e1) {
				// e1.printStackTrace();
				// }

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			// }
		}
		
	}



	private void doThings() throws IOException, ClassNotFoundException, InterruptedException {
		is = this.s.getInputStream();
		
		if (is.available() > 0){
			
			ois = new ObjectInputStream(is);
			mList = ois.readObject();
			
			//如果接收到的对象是一个List<String>的话
			if( mList instanceof List<?>){
				
				String line;
				String[] strsline;
				String strBSSID;
				
				for (int i=0; i< ((List<String>) mList).size(); i++){
					line = ((List<String>) mList).get(i).toString();
					strsline = line.split(",");
					strBSSID = strsline[1];
					mHashMap.put( strBSSID, strsline);
									
				}
				writeToCSV();
			}
			
		} else {
			Thread.sleep(10);
		}
	}



	private synchronized void writeToCSV() {

		try {

			initFileWriter();

			if (mHashMap != null) {
				// 遍历HashMap的Value
				for (String[] value : mHashMap.values()) {
					mWriter.writeNext(value);
				}
				mWriter.close();
				System.out.println(TAG + ": " + "Writing to CSV successfully...");

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void initFileWriter() throws IOException {

		mCSVFile = new File(Constants.FILE_NAME);
		if (!mCSVFile.exists()) {
			mCSVFile.createNewFile();
		}
		mWriter = new CSVWriter(new FileWriter(mCSVFile));
	}

	private void closeSocket() {

		if (isRunning)
			isRunning = false;

		if (s != null) {

			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("关闭：" + s.getRemoteSocketAddress());
	}

}
