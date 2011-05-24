package com.example.intent03;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class HttpUtil {
	
	
	public static String getHtml(String url){
		String result = null;
		
		HttpGet httpGet = new HttpGet(url);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse = null;
		try {
			// レスポンスを取得
			httpResponse = client.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();
			String response = EntityUtils.toString(entity);
			entity.consumeContent();			// entityのリソースを解放
			result = response;

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// クライアントを終了させる
		client.getConnectionManager().shutdown();
		
		return result;
	}
	
	public static String getTitle(String url) {
		String html = getHtml(url);
		String title = null;

		String regexp1 = "<title>(.*)</title>";
		Pattern pattern = Pattern.compile(regexp1);
		Matcher matcher = pattern.matcher(html);
		while (matcher.find()) {
			title = matcher.group(1);
		}
		title =  NCR2String(title);
		return title;
	}
	

	//タイトルに含まれる数値文字参照を変換する。
	// 例：　「&#65374;」→ 「〜」
	static String NCR2String(String str) { 
		String ostr = new String(); 
		int i1 = 0; 
		int i2 = 0; 

		while (i2 < str.length()) { 
			i1 = str.indexOf("&#", i2); 
			if (i1 == -1) { 
				ostr += str.substring(i2, str.length()); 
				break; 
			} 
			ostr += str.substring(i2, i1); 
			i2 = str.indexOf(";", i1); 
			if (i2 == -1) { 
				ostr += str.substring(i1, str.length()); 
				break; 
			} 

			String tok = str.substring(i1 + 2, i2); 
			try { 
				int radix = 10; 
				if (tok.trim().charAt(0) == 'x') { 
					radix = 16; 
					tok = tok.substring(1, tok.length()); 
				} 
				ostr += (char) Integer.parseInt(tok, radix); 
			} 
			catch (NumberFormatException exp) { 
				ostr += '?'; 
			} 
			i2++; 
		} 
		return ostr; 
	}
	
}
