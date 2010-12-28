package errorapp_notifier;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotifierFilter implements Filter {
	
	private String endpointUrl;
	private HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		try {
			chain.doFilter(req, res);
		} catch (IOException e) {
			System.out.println("caught");
			errorNotifier(e, req);
			throw e;
		} catch (ServletException e) {
			System.out.println("caught");
			errorNotifier(e, req);
			throw e;			
		} catch (RuntimeException e) {
			System.out.println("caught");
			errorNotifier(e, req);
			throw e;
		}
	}
	
	private void errorNotifier(Throwable t, ServletRequest request) {
		OutputStream out = null;
		try {
			PostMethod post = new PostMethod(endpointUrl);
			NameValuePair pair = new NameValuePair("data", exceptionData(t, (HttpServletRequest)request));
			post.setRequestBody(new NameValuePair[] { pair });
			try {
				httpClient.executeMethod(post);
				System.out.println("Error Notified");
			} finally {
				post.releaseConnection();
			}
		} catch (MalformedURLException e) {
			throw new ErrorappException(e);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ioe) {
				}
			}
		}
	}

	private String exceptionData(Throwable t, HttpServletRequest request) {
		try {
			JSONObject report = new JSONObject();
			JSONArray data = new JSONArray();
			data.put(getSummary(t, request));
			report.put(data);
			
			return report.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private JSONArray getSummary(Throwable t, HttpServletRequest request) throws JSONException {
		JSONArray summary = new JSONArray();
		summary.put("summary");
		JSONArray summaryContent = new JSONArray();
		summaryContent.put("type", "foo type");
		return summary;
	}

	public void init(FilterConfig cfg) throws ServletException {
	    String defaultServerAddress = "http://errorapp.com"
		String serverAddress = cfg.getInitParameter("serverAddress");
		String apiKey = cfg.getInitParameter("apiKey");

		if(serverAddress == null) {
		  serverAddress = defaultServerAddress     
        }
		

		if (!serverAddress.startsWith("http")) {
			serverAddress = "http://" + serverAddress;
		}
		if (!serverAddress.endsWith("/")) {
			serverAddress += "/";
		}
		
		this.endpointUrl = serverAddress + "api/projects/" + apiKey + "/fails";
	}
	
	public void destroy() {		
	}
	
}
