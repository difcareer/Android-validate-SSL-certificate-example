package org.ssl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.saps.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SSLSelfAssignActivity extends Activity {
	/** Called when the activity is first created. */

	private EditText textContainer;

	private ClientConnectionManager clientConnectionManager;
	private HttpContext context;
	private HttpParams params;

	Handler handler = new MyHandler();
	private static final int MSG_SHOW = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		 textContainer = (EditText) findViewById(R.id.editText1);

		// prepare for the https connection call this in the constructor of the
		// class that does the connection if it's used multiple times
		SchemeRegistry schemeRegistry = new SchemeRegistry();

//		// http scheme
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		// https scheme
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(),
				443));

		params = new BasicHttpParams();
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 1);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
				new ConnPerRouteBean(1));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf8");

//		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//		credentialsProvider.setCredentials(new AuthScope("yourServerHere.com",
//				AuthScope.ANY_PORT), new UsernamePasswordCredentials(
//				"YourUserNameHere", "UserPasswordHere"));
		clientConnectionManager = new ThreadSafeClientConnManager(params,
				schemeRegistry);

		context = new BasicHttpContext();
//		context.setAttribute("http.auth.credentials-provider",
//				credentialsProvider);
	}

	public void clean(View v) {
		textContainer.setText("");
	}

	public void callWebService(View v) {

		textContainer.setText("");

		String pathToWebService = "https://taobao.com/";  //ÄãµÄhttpsµØÖ·

		// connection (client has to be created for every new connection)
		final HttpClient client = new DefaultHttpClient(clientConnectionManager,
				params);

		final HttpGet get = new HttpGet(pathToWebService);

		new Thread(new Runnable() {
			@Override public void run() {
				try {
					HttpResponse response = client.execute(get, context);

					InputStream instream = response.getEntity().getContent();

					BufferedReader b = new BufferedReader(new InputStreamReader(
							instream));

					String txt = b.readLine();
					b.close();
					Message msg = handler.obtainMessage(MSG_SHOW);
					msg.obj = txt;
					handler.sendMessage(msg);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();

		new AsyncTask<Void,Void,String>(){

			@Override
			protected String doInBackground(Void... voids) {
				try {
					HttpResponse response = client.execute(get, context);

					InputStream instream = response.getEntity().getContent();

					BufferedReader b = new BufferedReader(new InputStreamReader(
							instream));

					return b.readLine();



				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}

			protected void onPostExecute(String result){
				textContainer.setText(result);
			}
		}.execute();
	}

	class MyHandler extends Handler {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SHOW:
				String txt = (String) msg.obj;
				textContainer.setText(txt);
				break;
			}
		}
	}
}