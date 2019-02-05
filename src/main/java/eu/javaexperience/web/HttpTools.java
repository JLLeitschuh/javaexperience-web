package eu.javaexperience.web;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.javaexperience.arrays.ArrayTools;
import eu.javaexperience.collection.map.SmallMap;
import eu.javaexperience.interfaces.simple.getBy.GetBy1;
import eu.javaexperience.interfaces.simple.publish.SimplePublish1;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.text.Format;
import eu.javaexperience.text.Format.DateFormatParseUnit;
import eu.javaexperience.time.TimeCalc;

public class HttpTools
{
	public static boolean isPost(HttpServletRequest req)
	{
		return "POST".equalsIgnoreCase(req.getMethod());
	}

	public static boolean isGet(HttpServletRequest req)
	{
		return "GET".equalsIgnoreCase(req.getMethod());
	}

	public static boolean isHead(HttpServletRequest req)
	{
		return "HEAD".equalsIgnoreCase(req.getMethod());
	}

	public static boolean isPut(HttpServletRequest req)
	{
		return "PUT".equalsIgnoreCase(req.getMethod());
	}

	public static boolean isDelete(HttpServletRequest req)
	{
		return "DELETE".equalsIgnoreCase(req.getMethod());
	}

	public static boolean isTrace(HttpServletRequest req)
	{
		return "TRACE".equalsIgnoreCase(req.getMethod());
	}

	public static boolean isOptions(HttpServletRequest req)
	{
		return "OPTIONS".equalsIgnoreCase(req.getMethod());
	}

	public static boolean isConnect(HttpServletRequest req)
	{
		return "CONNECT".equalsIgnoreCase(req.getMethod());
	}

	public static boolean isPath(HttpServletRequest req)
	{
		return "PATH".equalsIgnoreCase(req.getMethod());
	}

	public static boolean isPost(Context ctx)
	{
		return "POST".equalsIgnoreCase(ctx.getRequest().getMethod());
	}

	public static boolean isGet(Context ctx)
	{
		return "GET".equalsIgnoreCase(ctx.getRequest().getMethod());
	}

	public static boolean isHead(Context ctx)
	{
		return "HEAD".equalsIgnoreCase(ctx.getRequest().getMethod());
	}

	public static boolean isPut(Context ctx)
	{
		return "PUT".equalsIgnoreCase(ctx.getRequest().getMethod());
	}

	public static boolean isDelete(Context ctx)
	{
		return "DELETE".equalsIgnoreCase(ctx.getRequest().getMethod());
	}

	public static boolean isTrace(Context ctx)
	{
		return "TRACE".equalsIgnoreCase(ctx.getRequest().getMethod());
	}

	public static boolean isOptions(Context ctx)
	{
		return "OPTIONS".equalsIgnoreCase(ctx.getRequest().getMethod());
	}

	public static boolean isConnect(Context ctx)
	{
		return "CONNECT".equalsIgnoreCase(ctx.getRequest().getMethod());
	}

	public static boolean isPath(Context ctx)
	{
		return "PATH".equalsIgnoreCase(ctx.getRequest().getMethod());
	}
	
	/**
	 * Sends the given data with the given mime.
	 * sets the Content-Type, and Content-Length
	 * but at the end doesn't close the stream and doesn't call {@link #finishOperation()} 
	 * @throws IOException 
	 * */
	public static void sendMime(HttpServletResponse resp, MIME mime, byte[] data) throws IOException
	{
		resp.setContentType(mime.mime);
		resp.setContentLength(data.length);
		OutputStream os = resp.getOutputStream();
		os.write(data);
		os.flush();
	}
	
	public static Map<String,String[]> resolvMap(String params)
	{
		String[] pairs = params.split("&");
		Map<String,String[]> ret = new SmallMap<>();
		for (String pair : pairs)
		{
			int idx = pair.indexOf("=");
			String key = idx > 0 ? pair.substring(0, idx): pair;
			String value = idx > 0 && pair.length() > idx + 1 ?pair.substring(idx + 1): "";
			if(null != value)
			{
				value = URLDecoder.decode(value);
			}
			String[] add = ret.get(key);
			if(null == add)
			{
				add = new String[]{value};
			}
			else
			{
				add = Arrays.copyOf(add, add.length+1);
				add[add.length-1] = value;
			}
			ret.put(key, add);
		}
		return ret;
	}
	
	public static Map<String,String> convMapMulti(Map<String,String[]> map)
	{
		Map<String,String> ret = new HashMap<>();
		for(Entry<String, String[]> kv:map.entrySet())
			if(kv.getValue() != null)
				if(kv.getValue().length > 0)
					ret.put(kv.getKey(), kv.getValue()[0]);
		
		return ret;
	}
	
	public static void fillMultiMap(Map<String, String> dst, Map<String,String[]> map)
	{
		for(Entry<String, String[]> kv:map.entrySet())
			if(kv.getValue() != null)
				if(kv.getValue().length > 0)
					dst.put(kv.getKey(), kv.getValue()[0]);
	}
	
	public static Map<String,String> convMap(Map<String,String> map)
	{
		Map<String,String> ret = new HashMap<>();
		for(Entry<String, String> kv:map.entrySet())
			if(kv.getValue() != null)
				ret.put(kv.getKey(), kv.getValue());
		
		return ret;
	}
	
	/**
	 * ami a ? után van: asd=bsd&fgt=jhf
	 * @throws UnsupportedEncodingException 
	 * */
	public static void processArgsRequest(String line, Map<String,String[]> target) throws UnsupportedEncodingException
	{
		if("".equals(line))
		{
			return;
		}
		String[] pairs = line.split("&"); //ez megint gyorsabb
		for (String pair : pairs)
		{
			int idx = pair.indexOf("=");
			String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
			String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : "";
			putParam(target, key, value);
		}	
	}
	
	public static void putParam(Map<String,String[]> map, String key, String value)
	{
		String[] in = map.get(key);
		if(in == null)
			map.put(key, new String[]{value});
		else
			map.put(key, ArrayTools.arrayAppend(in, value));
	}
	
	public static String getParam(Map<String,String[]> map, String key)
	{
		return optParam(map, key, null);
	}
	
	public static String optParam(Map<String,String[]> map, String key, String def)
	{
		String[] ks = map.get(key);
		if(ks == null)
			return def;
		
		if(ks.length > 0)
			return ks[0];
		
		return def;
	}
	
	public static String renderRequestParams(Map<String, Object> params)
	{
		StringBuilder sb = new StringBuilder();
		renderRequestParams(params, sb);
		return sb.toString();
	}
	
	public static void renderRequestParams(Map<String, ? extends Object> params, Appendable app)
	{
		if(null == params || params.size() == 0)
		{
			return;
		}
		try
		{
			boolean nfirst = false;
			app.append("?");
			for(Entry<String, ?> kv:params.entrySet())
			{
				String k = kv.getKey();
				Object val = kv.getValue();
				if(null != k)
				{
					if(nfirst)
					{
						app.append("&");
					}
					nfirst = true;
					
					if(val instanceof String[])
					{
						for(String s:(String[])val)
						{
							appendParam(app, k, s);
						}
					}
					else if(val instanceof String)
					{
						appendParam(app, k, (String) val);
					}
					else if(null == val)
					{
						appendParam(app, k, null);
					}
					else
					{
						appendParam(app, k, val.toString());
					}
				}
			}
		}
		catch(Exception e)
		{
			Mirror.throwSoftOrHardButAnyway(e);
		}
	}
	
	public static void appendParam(Appendable app, String key, String val) throws IOException
	{
		app.append(key);
		
		if(null != val && val.length() > 0)
		{
			app.append("=");
			app.append(URLEncoder.encode(val));
		}
	}
	
	public static String getCookieValue(HttpServletRequest req, String cookieName)
	{
		return getCookieValue(req.getCookies(), cookieName);
	}
	
	public static String getCookieValue(Cookie[] cookies, String cookieName)
	{
		if(null == cookies || null == cookieName)
		{
			return null;
		}
		
		for(Cookie c: cookies)
		{
			if(cookieName.equals(c.getName()))
			{
				return c.getValue();
			}
		}
		
		return null;
	}
	
	
	public static String[] getCookieValues(Context ctx, String cookieName)
	{
		return getCookieValues(ctx.getRequest().getCookies(), cookieName);
	}
	
	public static String[] getCookieValues(HttpServletRequest req, String cookieName)
	{
		return getCookieValues(req.getCookies(), cookieName);
	}
	
	public static String[] getCookieValues(Cookie[] cookies, String cookieName)
	{
		if(null == cookies || null == cookieName)
		{
			return null;
		}
		
		ArrayList<String> ret = new ArrayList<>();
		
		for(Cookie c: cookies)
		{
			if(cookieName.equals(c.getName()))
			{
				ret.add(c.getValue());
			}
		}
		
		return ret.toArray(Mirror.emptyStringArray);
	}
	
	public static void httpRedirect(HttpServletResponse resp, String path,boolean permanently)
	{
		resp.addHeader("Location", path);
		if(permanently)
		{
			resp.setStatus(HttpResponseStatusCode._301_moved_permanently.getStatus());
		}
		else
		{
			resp.setStatus(HttpResponseStatusCode._307_temporary_redirect.getStatus());
		}
	}
	
	/**
	 * 
	 	try(FileInputStream fis = new FileInputStream("my file");)
		{
			ZipEntry zipEntry = new ZipEntry("zip filename");
			zos.putNextEntry(zipEntry);
			IOTools.copyStream(fis, zos);
			zos.closeEntry();
		}
	 * 
	 * */
	public static ZipOutputStream createZipOutputToClient(String filename, HttpServletResponse response) throws IOException
	{
		response.setContentType("application/x-zip");
		response.addHeader("Content-Transfer-Encoding", "binary");
		response.addHeader("Content-Disposition", "attachment; filename="+filename);
		
		response.flushBuffer();
		ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
		
		return zos;
	}
	
	public static void tryRecogniseCookieHeader(Collection<Cookie> dst, String header, String value)
	{
		if("cookie".equalsIgnoreCase(header))
		{
			String[] cs = value.split(";");
			
			for(String c:cs)
			{
				String kv[] = c.split("=",2);
				if(kv.length != 2)
					continue;
		
				dst.add(new Cookie(kv[0].trim(), kv[1].trim()));
				continue;
			}
		}
	}

	public static String headerFriendly(String k)
	{
		StringBuilder sb = new StringBuilder();
		k = k.toLowerCase();
		int l = k.length();
		boolean nextUpper = true;
		for(int i=0;i<l;++i)
		{
			char c = k.charAt(i);
			if(nextUpper)
			{
				sb.append(Character.toUpperCase(c));
				nextUpper = false;
			}
			else if(c == ' ' || c == '_')
			{
				nextUpper = true;
				sb.append("-");
			}
			else
			{
				sb.append(c);
			}
		}
		
		return sb.toString();
	}

	public static Map<String,String[]> convMapToMulti(Map<String, String> params)
	{
		SmallMap<String, String[]> ret = new SmallMap<>();
		for(Entry<String, String> p:params.entrySet())
		{
			String v = p.getValue();
			ret.put(p.getKey(), null == v?Mirror.emptyStringArray:new String[]{v});
		}
		
		return ret;
	}
	
	/**
	 * A végén \r\n-el zárva
	 * */
	public static String cookieToHeaderLine(Cookie c,boolean lineEndRN)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Set-Cookie: ");
		sb.append(c.getName());
		sb.append("=");
		sb.append(c.getValue());
		if(c.getDomain() != null)
		{
			sb.append("; Domain=");
			sb.append(c.getDomain());
		}
		
		if(c.getPath() != null)
		{
			sb.append("; Path=");
			sb.append(c.getPath());
		}
		
		if(c.getMaxAge()>0)
		{
			sb.append("; Expires=");
			sb.append(toHeaderDate(System.currentTimeMillis()+(c.getMaxAge()*1000)));
		}
		
		if(lineEndRN)
			sb.append("\r\n");
		return sb.toString();
	}
	
	public static String renderCookie(Cookie c,boolean lineEndRN)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(c.getName());
		sb.append("=");
		sb.append(c.getValue());
		if(c.getDomain() != null)
		{
			sb.append("; Domain=");
			sb.append(c.getDomain());
		}
		
		if(c.getPath() != null)
		{
			sb.append("; Path=");
			sb.append(c.getPath());
		}
		
		if(c.getMaxAge()>0)
		{
			sb.append("; Expires=");
			sb.append(toHeaderDate(System.currentTimeMillis()+(c.getMaxAge()*1000)));
		}
		
		if(lineEndRN)
			sb.append("\r\n");
		return sb.toString();
	}
	
	protected static class CookieDateFormatParseUnit extends DateFormatParseUnit
	{
		public CookieDateFormatParseUnit(String format)
		{
			super(format);
		}

		@Override
		protected SimpleDateFormat create(String format)
		{
			SimpleDateFormat ret = new SimpleDateFormat(format, Locale.US);
			ret.setTimeZone(TimeZone.getTimeZone("GMT"));
			return ret;
		}
	}
	
	public static final DateFormatParseUnit cookieFormat1 = new CookieDateFormatParseUnit("EEE, dd-MMM-yyyy HH:mm:ss z");
	public static final DateFormatParseUnit cookieFormat2 = new CookieDateFormatParseUnit("EEE, dd MMM yyyy HH:mm:ss zzz");
	
	
	public static Date fromHeaderDate(String head) throws ParseException
	{
		try
		{
			return cookieFormat1.parse(head);
		}
		catch(Exception e)
		{}
		
		try
		{
			return cookieFormat2.parse(head);
		}
		catch(ParseException e)
		{
			throw e;
		}
	}
	
	public static String toHeaderDate(long time)
	{
		return cookieFormat2.format(new Date(time));
	}
	
	public static String toHeaderDate(Date time)
	{
		return cookieFormat2.format(time);
	}
	
	public static final Cookie[] emptyCookieArray = new Cookie[0];
	
	public static final String commentCookieSet = "putCookieForBrowser";
	
	public static HttpResponseStatusCode httpResponseByNumber(int num)
	{
		for(HttpResponseStatusCode r:HttpResponseStatusCode.values())
			if(r.getStatus() == num)
				return r;
		
		throw new IllegalArgumentException(num+" status code does not exist. See "+HttpResponseStatusCode.class.getName());
	}
	
	public static long getWebDate(Date date)
	{
		return (date.getTime()/1000)*1000;
	}
	
	public static long getWebDate(long date)
	{
		return (date/1000)*1000;
	}
	
	public static long getWebDateNow()
	{
		return (System.currentTimeMillis()/1000)*1000;
	}
	
	
	public static String toCacheSaltHexa(long d)
	{
		d /= 1000;
		return Long.toHexString(d);
	}
	
	public static String toSeo(String string)
	{
		return Normalizer.normalize(string.toLowerCase(), Form.NFD)
			.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
			.replaceAll("[^\\p{Alnum}]+", "-");
	}

}
