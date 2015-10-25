package caiqiqi.sdnserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;


public class ServerThread implements Runnable {

	public static String TAG = "ServerThread";
	// 当前线程处理的Socket
	Socket s;
	// 当前线程所处理的Socket所对应的输入流
	BufferedReader br;

	public ServerThread(Socket s) throws IOException {
		this.s = s;
		// 初始化该Socket对应的输入流
		br = new BufferedReader(new InputStreamReader(s.getInputStream(), "utf-8"));
	}

	public void run() {
		
		System.out.println("ServerThread started...");
		try {
			String content;
			// 采用循环不断从Socket中读取客户端发送过来的数据
			while ((content = readFromClient()) != null) {

				System.out.println("ServerThread reading from stream...");
				// TODO 服务器端处理一些逻辑，查询当前每个热点的连接数，然后再将结果返回客户端
				// 遍历socketList中的每个Socket
				// 将读到的内容向每个Socket发送一次
				for (Socket s : MyServer.socketList) {
					OutputStream os = s.getOutputStream();
					os.write((content + "\n").getBytes("utf-8"));
					System.out.println("ServerThread writing..." + content);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 上述的os.write()代码将网络的字节输入流转换为字符输入流时，指定了转换所用的字符串：UTF-8.
	 * 当需要编写跨平台的网络通信程序时，使用UTF-8字符集进行编码、解码是一种较好的解决方案。
	 */

	// 定义读取客户端数据的方法
	// 这里将几句代码抽象出来形成一个方法，有一个好处就是，避免了过多的使用try...catch导致代码不好看
	private String readFromClient() {
		try {
			return br.readLine();
		} catch (IOException e) {
			// 若捕获到异常，则说明该Socket对应的客户端已关闭，于是需从socketList中移除对应的客户端Socket
			MyServer.socketList.remove(s);
		}

		return null;
	}
}
