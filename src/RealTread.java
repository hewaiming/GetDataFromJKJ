import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RealTread {
	Socket socket;
	InetAddress addr;
	BufferedInputStream bis = null;
	BufferedOutputStream bos = null;
	private DataInputStream in;
	private DataOutputStream out;
	
	private void GetData() {

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress("172.16.0.5", 9099), 2000);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			// bis=new BufferedInputStream(in);
			// bos=new BufferedOutputStream(os);
			byte[] cmdBuf = new byte[30];
			// 52 65 61 64 52 65 61 6C 54 72 65 6E 64 44 61 74
			cmdBuf[0] = (byte) 0x52;
			cmdBuf[1] = (byte) 0x65;
			cmdBuf[2] = (byte) 0x61;
			cmdBuf[3] = (byte) 0x64;
			cmdBuf[4] = (byte) 0x52;
			cmdBuf[5] = (byte) 0x65;
			cmdBuf[6] = (byte) 0x61;
			cmdBuf[7] = (byte) 0x6c;
			cmdBuf[8] = (byte) 0x54;
			cmdBuf[9] = (byte) 0x72;
			cmdBuf[10] = (byte) 0x65;
			cmdBuf[11] = (byte) 0x6e;
			cmdBuf[12] = (byte) 0x64;
			cmdBuf[13] = (byte) 0x44;
			cmdBuf[14] = (byte) 0x61;
			cmdBuf[15] = (byte) 0x74;
			cmdBuf[16] = (byte) 'a';
			cmdBuf[17] = (byte) ' ';
			cmdBuf[18] = (byte) '1'; // 厂房号
			cmdBuf[19] = (byte) ' ';
			cmdBuf[20] = (byte) '1'; // 区号
			cmdBuf[21] = (byte) ' ';
			cmdBuf[22] = (byte) '3'; // 槽号
			cmdBuf[23] = (byte) '6'; // 槽号
			cmdBuf[24] = (byte) 0x0d;
			cmdBuf[25] = (byte) 0x0a;
			out.write(cmdBuf);
			out.flush();
			byte[] trendBuf = new byte[10];
			byte[] trendBuf1 = new byte[18];
			int len, len1;// 表示成功读取的字节数的个数
			// while ((len = in.read(trendBuf)) != -1) {
			// if(len>4){
			// System.out.println("recv len=" + len);
			// int SysI=trendBuf[5]*256+trendBuf[4];
			// System.out.println("系列电流:"+SysI);
			// int PotV=trendBuf[7]*256+trendBuf[6];
			// System.out.println("槽压:"+PotV);
			// }
			// }
			len = in.read(trendBuf);
			len1 = in.read(trendBuf1);
			System.out.println(trendBuf1.toString());
			System.out.println(" 槽压:" + (((trendBuf1[7] & 0x00ff) << 8) + (trendBuf1[6] & 0x00ff)));
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
