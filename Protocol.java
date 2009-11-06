import java.io.*;
import java.util.*;

class OfferHelpResponse{
	int tlx;
	int tly;
	int width;
	int height;
	int globalWidth;
	int globalHeight;
}

public class Protocol{

	private static byte OFFERHELP = 0x1;

	public static void offerHelpReq(OutputStream out){
		try{
			out.write(OFFERHELP);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void offerHelpResp(OutputStream out, int tlx, int tly, int width,
					int height, int globalWidth, int globalHeight){
		try{
			DataOutputStream data = new DataOutputStream(out);
			data.write(OFFERHELP);
			data.writeInt(tlx);
			data.writeInt(tly);
			data.writeInt(width);
			data.writeInt(height);
			data.writeInt(globalWidth);
			data.writeInt(globalHeight);
			data.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static OfferHelpResponse offerHelpResp(InputStream in){
		OfferHelpResponse r = new OfferHelpResponse();
		try{
			in.read();
			DataInputStream data = new DataInputStream(in);
			r.tlx = data.readInt();
			r.tly = data.readInt();
			r.width = data.readInt();
			r.height = data.readInt();
			r.globalWidth = data.readInt();
			r.globalHeight = data.readInt();
		}catch(Exception e){
			e.printStackTrace();
		}
		return r;
	}
}

