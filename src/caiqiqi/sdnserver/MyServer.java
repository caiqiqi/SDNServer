package caiqiqi.sdnserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MyServer {
	/**
	 * 负责接收客户端Socket的连接请求。每当客户端Socket连接到该ServerSocket之后，
	 * 程序将对应的Socket加入到socketList集合中保存，并为该Socket启动一条线程，该线程负责处理该Socket所有的通信任务。
	 **/
	
	public static ArrayList<Socket> socketList = new ArrayList<Socket>();

	public static void main(String[] args) {

		ServerSocket ss = null;
		Socket s =null;
		
			try {
				// 默认端口30000
				ss = new ServerSocket(Constants.DEFAULT_BIND_PORT);
				System.out.println("Server started,listening at port ..." + Constants.DEFAULT_BIND_PORT);
				
				while (true) {
					
					s = ss.accept();
					socketList.add(s);
					
					new Thread(new ServerThread1(s)).start();
				}
			} catch (IOException e) {
				
				//若捕获到异常则移除该Socket
				if(s!= null){
					
					socketList.remove(s);
				}
				e.printStackTrace();
			}
	}
}
