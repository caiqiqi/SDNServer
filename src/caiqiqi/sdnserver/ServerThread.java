package caiqiqi.sdnserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;


public class ServerThread implements Runnable {

	public static String TAG = "ServerThread";
	// 当前线程处理的Socket
	private Socket s;
	// 当前线程所处理的Socket所对应的输入流
	private BufferedReader br;
	
	private ObjectInputStream ois;
	
	//服务器端接收到的List<ScanResult>
	private Object mList;
	
//	private PrintWriter  out;;
	
	/**
	 * 以BSSID为键，String[]为值
	 */
	private static Map<String,String[]> mHashMap = new HashMap<String,String[]>();
//	private List<String[] > mList_content;
	
	private File mCSVFile;
	private CSVWriter mWriter;
	
	private static int counter = 0;

	public ServerThread(Socket s) throws IOException {
		
		counter++;
		this.s = s;
		// 初始化该Socket对应的输入流
		br = new BufferedReader(new InputStreamReader(s.getInputStream(), "utf-8"));
		
		mHashMap = new HashMap<String,String[]>();
		
		
	}

	public void run() {
		
		System.out.println("ServerThread started...for: " + counter);
		
		try {
			
			this.ois = new ObjectInputStream(this.s.getInputStream());
			mList = ois.readObject();
			
			//如果接收到的对象是一个List的话
			if( mList instanceof List<?>){
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println( "终于跳出while循环了" );
		writeToCSV();
		
		
		/*
		 *  遍历socketList中的每个Socket
		 *  将读到的内容向每个Socket发送一次
		 */
//		for (Socket s : MyServer.socketList) {
//			//OutputStream os = s.getOutputStream();
//			//TODO 服务器操作之后返回给客户端
//			//os.write(...);
//		}
	}

	private synchronized void writeToCSV() {
		
		try {
			
			initFileWriter();
			
			if(mHashMap != null){
				//遍历HashMap的Value
				for (String[] value : mHashMap.values()){
					mWriter.writeNext(value);
				}
				mWriter.close();
				System.out.println("Writing to CSV successfully...");
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initFileWriter() throws IOException {
		
		mCSVFile = new File(Constants.FILE_NAME);
		if( !mCSVFile.exists()){
			mCSVFile.createNewFile();  
		}
		mWriter = new CSVWriter(new FileWriter(mCSVFile) );
	}
	
}
