import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetUML {

	private URL url;
	private String fileName;
	
	public GetUML(URL url, String fileName) {
		this.url = url;
		this.fileName = fileName;
	}
	// 由于传输编码格式为UTF-8，所以要进行转换
	public boolean saveImage() throws Exception {
		HttpURLConnection conn;
		
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5 * 1000);
			//InputStream inStream = conn.getInputStream();
			InputStream inStream = url.openStream();
			byte[] data = readInputStream(inStream);
			File imageFile = new File(fileName);
			FileOutputStream outStream = new FileOutputStream(imageFile);
			outStream.write(data);
			outStream.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public byte[] readInputStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		inStream.close();
		return outStream.toByteArray();
	}


}
