package com.example.intent03;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class HttpUtil {
	
	
	public static String GetHtml(String url){
		String result = null;
		
		HttpGet httpGet = new HttpGet(url);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse = null;
		try {
			httpResponse = client.execute(httpGet);

			// ステータスコードを取得
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			
			// レスポンスを取得
			HttpEntity entity = httpResponse.getEntity();
			String response = EntityUtils.toString(entity);
			// リソースを解放
			entity.consumeContent();
			
			result = statusCode + ": " + response;

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
}
