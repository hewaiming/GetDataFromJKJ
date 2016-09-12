import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import com.sun.org.apache.xpath.internal.operations.And;

public class PotStatusData {

	public static void main(String[] args) {
		Socket socket;
		InetAddress addr;
		InputStream is = null;
		OutputStream os = null;		
		DataInputStream in;
		DataOutputStream out;
		String[] OsStatus = { "NB", "AEB", "AU", "AD", "AC", "TAP", "IRF", "FNB", "APB", "TMT", "RRK" };
		String[] AeStatus = { "N1", "W1", "N2", "W2", "NX", "CO", " ", " " };
		String[] WorkStatus={"NORM","预热","启动","停槽","CErr"};
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress("172.16.0.6", 9099), 2000);
			is = socket.getInputStream();
			os = socket.getOutputStream();
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());		
			byte[] cmdBuf = new byte[30];
			// ReadPotStatus
			cmdBuf[0] = 'R';
			cmdBuf[1] = 'e';
			cmdBuf[2] = 'a';
			cmdBuf[3] = 'd';
			cmdBuf[4] = 'P';
			cmdBuf[5] = 'o';
			cmdBuf[6] = 't';
			cmdBuf[7] = 'S';
			cmdBuf[8] = 't';
			cmdBuf[9] = 'a';
			cmdBuf[10] = 't';
			cmdBuf[11] = 'u';
			cmdBuf[12] = 's';
			cmdBuf[13] = ' ';

			cmdBuf[14] = (byte) '2'; // 厂房号
			cmdBuf[15] = (byte) ' ';
			cmdBuf[16] = (byte) '1'; // 区号
			cmdBuf[17] = (byte) 0x0d;
			cmdBuf[18] = (byte) 0x0a;
			out.write(cmdBuf);
			out.flush();
			byte[] LenBuf = new byte[4];
			byte[] RecvBuf = new byte[2200];

			int Len1 = 0, len2 = 0;// 表示成功读取的字节数的个数
			Len1 = in.read(LenBuf);
			len2 = in.read(RecvBuf);
			int recvLenght = ((LenBuf[2] & 0x00ff) << 8) + (LenBuf[3] & 0x00ff);
			if ((Len1 == 4) && (len2 == recvLenght)) {
               // RecvBuf[0]==0x42  实时曲线
		      // RecvBuf[0]==0x00    槽状态表
				System.out.println("厂房：" + RecvBuf[1] + "  区号：" + RecvBuf[2]);
				int SysI = ((RecvBuf[5] & 0x00ff) << 8) + (RecvBuf[4] & 0x00ff);
				System.out.println("系列电流:" + SysI);
				int SysV = ((RecvBuf[7] & 0x00ff) << 8) + (RecvBuf[6] & 0x00ff);
				System.out.println("系列电压:" + SysV);
				int RoomV = ((RecvBuf[9] & 0x00ff) << 8) + (RecvBuf[8] & 0x00ff);
				System.out.println("厂房电压:" + RoomV);
				
				int S = 17;
				for (int i = 1; i <= 37; i++) {
					System.out.println("-----------------------");
					System.out.println("槽号：" + i);
					if ((RecvBuf[S] & 0x40) != 0) {
						System.out.println("自手动：MAN");
					} else {
						System.out.println("自手动:  ");
					}

					int action = RecvBuf[S] & 0x3f;
					if ((action >= 1) && (action <= 11)) {

						System.out.println("槽作业: " + OsStatus[action - 1]);
					} else {
						System.out.println("槽作业: ");
					}

					int worksta=RecvBuf[S + 1] & 0x03;
					if (worksta == 3) {
						System.out.println("槽状态：" + " 停槽 ");
					} else {
						System.out.println("槽状态：" +WorkStatus[worksta] );
					}
				
					int tmp = RecvBuf[S + 2] & 0x07;
					System.out.println("加料状态:" + AeStatus[tmp]);// RecvBuf[S+2]:=PCBST[RoomNo,A,i].SPAEST;//SPAEST

					System.out.println("故障:" + RecvBuf[S + 3]);

					int SetV = ((RecvBuf[S + 5] & 0x00ff) << 8) + (RecvBuf[S + 4] & 0x00ff); //
					System.out.println("设定电压:" + String.format("%.3f", SetV/1000.000)); // 设定电压

					
					int WorkV = ((RecvBuf[S + 7] & 0x00ff) << 8) + (RecvBuf[S + 6] & 0x00ff);
					if ((RecvBuf[S + 2] & 0x80)!=0){
						System.out.println("效应 工作电压: 红色 "+String.format("%.3f", WorkV/1000.000));
					}else if ((RecvBuf[S+42] & 0x40)!=0){
						System.out.println("异常 工作电压: 绿色 "+String.format("%.3f", WorkV/1000.000));
					}else if((RecvBuf[S+42] & 0x20)!=0){
						System.out.println("工作电压:绿黄色 " +  String.format("%.3f", WorkV/1000.000));// 工作电压
					}else{
						System.out.println("工作电压:" +  String.format("%.3f", WorkV/1000.000));// 工作电压
					}					

					int comerr = RecvBuf[S + 8] - 35;
					System.out.println("通讯故障:" + comerr); // RecvBuf[S+8]:=PCBST[RoomNo,A,i].ComError;//

					int SetNb = ((RecvBuf[S + 10] & 0x00ff) << 8) + (RecvBuf[S + 9] & 0x00ff);
					System.out.println("设定NB:" + SetNb);

					int RealNb = ((RecvBuf[S + 12] & 0x00ff) << 8) + (RecvBuf[S + 11] & 0x00ff);
					System.out.println("实际NB:" + RealNb);					
					
					int AeTimes = ((RecvBuf[S + 14] & 0x00ff) << 8) + (RecvBuf[S + 13] & 0x00ff);
					System.out.println("效应间隔:" + AeTimes/60);	
					
					int Aetmp = RecvBuf[S + 15] & 0x07;
					System.out.println("效应状态:" + AeStatus[Aetmp]);	
					
					System.out.println("效应时刻:" +Integer.toHexString(RecvBuf[S + 16])+"/"+Integer.toHexString(RecvBuf[S + 17])+":"+Integer.toHexString(RecvBuf[S + 18]));
					
					int AeContinus = ((RecvBuf[S + 20] & 0x00ff) << 8) + (RecvBuf[S + 19] & 0x00ff);
					System.out.println("AE时间:" + AeContinus);
					
					System.out.println("NB时刻:" +Integer.toHexString(RecvBuf[S + 24]) + ":" +Integer.toHexString(RecvBuf[S + 23])); // ok

					//判断效应和异常槽压标志 21 22  25 26 位
					
					/*if((RecvBuf[S+25] & 0x80)!=0){
						System.out.println("效应和异常槽压标志：红色");
					}else if ((RecvBuf[S+25] & 0x20)!=0) {
						System.out.println("效应和异常槽压标志：绿黄色");
					}else if ((RecvBuf[S+25] & 0x40)!=0) {
						System.out.println("效应和异常槽压标志：绿色");
					}else {
						System.out.println("效应和异常槽压标志：黑色");
					}*/
					
					
					int Nbplus =((RecvBuf[S + 44] & 0x00ff) << 8) + (RecvBuf[S + 43] & 0x00ff);
					System.out.println("过欠:" + Nbplus);					
					
					int AeCnt = ((RecvBuf[S + 38] & 0x00ff) << 8) + (RecvBuf[S + 37] & 0x00ff);
					System.out.println("效应次数:" + AeCnt);

					int AeV = ((RecvBuf[S + 46] & 0x00ff) << 8) + (RecvBuf[S + 45] & 0x00ff);					
					System.out.println("AE电压:" + Math.round(AeV/1000.0 * 100) * 0.01d);										

					int yjwz = ((RecvBuf[S + 48] & 0x00ff) << 8) + (RecvBuf[S + 47] & 0x00ff);
					System.out.println("阳极位置:" + yjwz);

					int noise = ((RecvBuf[S + 52] & 0x00ff) << 8) + (RecvBuf[S + 51] & 0x00ff);
					System.out.println("噪音:" + noise);
					S = S + 53;
				}
				
			}
			out.close();
			in.close();
			is.close();
			os.close();			
			socket.close();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

}
