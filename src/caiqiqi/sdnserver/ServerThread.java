package caiqiqi.sdnserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
//import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVWriter;

public class ServerThread implements Runnable{

	public static String TAG ="ServerThread";
	
	/** 当前线程处理的Socket */
	private Socket s;

	private InputStream is;
	private ObjectInputStream ois;

	private PrintWriter os;
	
	/** 服务器端接收到的List<ScanResult>
	 * 这里不能引入ScanResult这个类，于是只能用Object代替
	 *  */
	private Object objScanResult;
	
	/*while循环是否运行*/
	private boolean isRunning = true;
	
	/*返回给客户端的标志*/
	boolean flag = false;
	
	/**
	 * 用来放扫描结果的，哦，错了，这里不应该为static，因为每个Socket对应一个ServerThread
	 * 以BSSID为键，String[]为值
	 */
	private Map<String,String[]> mHashMap = new HashMap<String,String[]>();
	
	private File mCSVFile;
	private CSVWriter mWriter;
	
	public ServerThread(Socket s ) throws IOException {
		
		this.s = s;
		this.mHashMap = new HashMap<String,String[]>();
	}
	
	
	
	@Override
	public void run() {

		System.out.println("Socket " + this.s.getRemoteSocketAddress() + " accepted");
		
		while (isRunning) {

			try {

				doThings();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
	}



	private void doThings() throws IOException, ClassNotFoundException, InterruptedException {
		
		is = this.s.getInputStream();
		
		if (is.available() > 0){
			
			//注意这里不能重复创建ois，只在第一次创建。
			//由于会发几次热点信息过来，所以第二次发的时候就不必重复创建了，否则会出错
			if (ois == null) {
				ois = new ObjectInputStream(is);
			}
			objScanResult = ois.readObject();
			
			//如果接收到的对象是一个List<String>的话
			if( objScanResult instanceof List<?>){
				
				//先将Object强制转换成List<String>
				List<String> listScanResult = (List<String>)objScanResult;
				putIntoHashMap( listScanResult );
				//若成功写入服务器端的文件中，则flag置为true
				flag = writeToCSV();
				
				//将服务器端执行结果返回给客户端
				sendToClient(flag);
				System.out.println(TAG + ": " + "已向客户端发送flag");
			}
			
		} 
		
		
	}


/**
 * 将接收到的List写入到HashMap
 */
	private void putIntoHashMap(List<String> list) {
		
		String line;
		String[] strsline;
		String strBSSID;
		
		for (int i=0; i< list.size(); i++){
			line =list .get(i).toString();
			strsline = line.split(",");
			strBSSID = strsline[1];
			mHashMap.put( strBSSID, strsline);				
		}
	}


/**
 * 
 * @param flag 服务器端是否已成功写入文件
 */
	private void sendToClient(boolean flag) {
		
		try {
			
			if (os == null) {
				os = new PrintWriter(this.s.getOutputStream());
			}
			
			os.println(flag);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


/**
 * 写入到CSV文件
 * @return 是否成功写入到CSV文件中
 */
	private boolean writeToCSV() {

		try {

			initFileWriter();

			if (mHashMap != null) {
				// 遍历HashMap的Value
				for (String[] value : mHashMap.values()) {
					mWriter.writeNext(value);
				}
				mWriter.close();
				System.out.println(TAG + ": " + "Writing to CSV successfully...");
				return true;

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}

/**
 * 初始化文件写入
 * @throws IOException
 */
	private void initFileWriter() {

		try {

			mCSVFile = new File(Constants.FILE_NAME);

			if (!mCSVFile.exists()) {
				mCSVFile.createNewFile();
			}
			mWriter = new CSVWriter(new FileWriter(mCSVFile));
		} catch (IOException e) {
			System.out.println(TAG + "写入文件失败");
			e.printStackTrace();
		}

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
