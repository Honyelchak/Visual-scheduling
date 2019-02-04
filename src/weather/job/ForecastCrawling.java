package weather.job;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import weather.util.TDB;

public class ForecastCrawling implements Job {

	/*
	 * 爬取每个地区的
	 */
	@Override
	public void execute(JobExecutionContext jobContext) {
		Calendar cal = Calendar.getInstance();
		Date time = cal.getTime();
		String[] city = new String[124];
		// 读取城市ID
		int i = 0;
		String str = "";
		try {
			URL resource = this.getClass().getResource("/cityId.txt");
			String path = resource.getPath();// 获取文件的绝对路径
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(path)));
			while ((str = br.readLine()) != null) {
				city[i] = str;
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setSocketTimeout(60000).setConnectTimeout(60000)
				.setConnectionRequestTimeout(60000)
				.setStaleConnectionCheckEnabled(true).build();
		// 创建httpClient对象
		CloseableHttpClient h = HttpClients.custom()
				.setDefaultRequestConfig(defaultRequestConfig).build();

		// 创建并设置URI
		URIBuilder uri = null;
		// 创建Get请求
		HttpGet hg = null;
		String url = "";
		// 创建响应对象
		CloseableHttpResponse response = null;
		InputStream inputstream = null;
		int j = 0;
		for (; j < city.length; j++) {

			try {
				url = "http://www.weather.com.cn/weather/" + city[j] + ".shtml";
				uri = new URIBuilder(url);
				hg = new HttpGet(uri.build());
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			// 设置请求头
			hg.setHeader(
					"Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
			hg.setHeader("Accept-Encoding", "gzip, deflate");
			hg.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
			hg.setHeader("Cache-Control", "no-cache");
			hg.setHeader("Connection", "keep-alive");
			hg.setHeader("Host", "www.weather.com.cn");
			hg.setHeader("Upgrade-Insecure-Requests", "1");
			hg.setHeader(
					"Cookie",
					"UM_distinctid=1677bacc36c318-06864df6ba12d8-50422618-13c680-1677bacc36e326;"
							+ " vjuids=9377f5f84.1677bacc68a.0.c5cf929c2799d; userNewsPort0=1; "
							+ "f_city=%E9%83%91%E5%B7%9E%7C101180101%7C; "
							+ "Hm_lvt_36dcc28125c1b7e65fa2190352951396=1546071468; Wa_lvt_13=1546071468; "
							+ "zs=101180101%7C%7C%7Cyd-uv; Wa_lvt_2=1546072400; CNZZDATA1257969847=1421622329-1543967425-https%253A%252F%252Fwww.baidu.com%252F%7C1546332806;"
							+ " CNZZDATA5652381=cnzz_eid%3D126216725-1543964774-https%253A%252F%252Fwww.baidu.com%252F%26ntime%3D1546328474; defaultCty=101180101; defaultCtyName=%u90D1%u5DDE; vjlast=1543968180.1547593222.11; Hm_lvt_080dabacb001ad3dc8b9b9049b36d43b=1547474626,1547559941,1547593221,1548134214; CNZZDATA1262608253=2132717208-1546067788-https%253A%252F%252Fwww.baidu.com%252F%7C1548129724; Wa_lvt_1=1547559941,1547593225,1547593221,1548134214; Hm_lpvt_080dabacb001ad3dc8b9b9049b36d43b=1548134497;"
							+ " Wa_lpvt_1=1548134497");
			hg.setHeader(
					"User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Safari/537.36");
			hg.setHeader("Referer", "http://www.weather.com.cn/weather1d/"
					+ city[j] + ".shtml");
			// 发送请求
			HttpEntity entity = null;
			String line, Sline = "";

			try {
				response = h.execute(hg);
				// 获取请求结果
				entity = response.getEntity();
				inputstream = entity.getContent();
				BufferedReader bufferedreader = new BufferedReader(
						new InputStreamReader(inputstream, "UTF-8"));
				while ((line = bufferedreader.readLine()) != null) {
					Sline += line + "\n";
				}

				Document doc = Jsoup.parse(Sline);
				Element times = doc.getElementById("update_time");

				Elements one = doc.getElementsByClass("sky");
				Document oneDoc = Jsoup.parse(one.toString());
				Elements ul = oneDoc.getElementsByClass("skyid");
				Timestamp current_time = null;
				Timestamp forecast_time = null;

				String s1 = times.attr("value");
				String[] update_time = s1.split(":");

				cal.clear();
				cal = Calendar.getInstance();
				time.setHours(Integer.valueOf(update_time[0]));
				time.setMinutes(Integer.valueOf(update_time[1]));
				time.setSeconds(0);
				current_time = new Timestamp(time.getTime());
				boolean flag = false;
				for (Element li : ul) {

					Element weatherEle = li.select("p.wea").get(0);
					Element minTemEle = null, maxTemEle = null;
					// 温度第一天没有最高和最低温度
					if (li.select("p.tem > span").isEmpty()) {
						maxTemEle = li.select("p.tem > i").get(0);
						minTemEle = maxTemEle;
					} else {
						maxTemEle = li.select("p.tem > span").get(0);
						minTemEle = li.select("p.tem > span").get(0);
					}
					// 风向有时只有一个
					Element wind1 = null, wind2 = null;
					if (li.select("p.win > em > span").size() == 2) {
						wind1 = li.select("p.win > em > span:nth-child(1)")
								.get(0);
						wind2 = li.select("p.win > em > span:nth-child(2)")
								.get(0);
					} else {
						wind1 = li.select("p.win > em > span").get(0);
						wind2 = wind1;
					}
					Element power = li.select("p.win > i").get(0);

					// String timeStr = timeEle.ownText().replace("日（今天）", "");
					// 描述天气状况的中文
					String weather = weatherEle.ownText();
					// 去除天气字段中的单位
					String maxTem = maxTemEle.ownText().replace("℃", "");
					String minTem = minTemEle.ownText().replace("℃", "");

					// 需要注意预测天气中有两个风向，
					String wind_direction = "";
					if (wind1.attr("title") == wind2.attr("title")) {
						wind_direction = wind1.attr("title");
					} else {
						wind_direction = wind1.attr("title") + "转"
								+ wind2.attr("title");
					}
					// 风力
					String wind_power = power.ownText();
					// 推算预测的天气
					if (!flag) {
						forecast_time = current_time;
					} else {
						cal.add(Calendar.DATE, 1);
						forecast_time = new Timestamp(cal.getTimeInMillis());
					}

					Date d = new Date(cal.get(Calendar.YEAR),
							cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
					String id = city[j] + d.getTime();
					flag = true;
					save(new Object[] { id, city[j], maxTem, minTem,
							wind_direction, wind_power, weather, current_time,
							forecast_time });
				}

			} catch (ClientProtocolException e1) {

				System.out.println("请求超时等问题");
				e1.printStackTrace();
			} catch (IOException e1) {

				System.out.println("I/O问题");
				e1.printStackTrace();
			} finally {
				try {
					inputstream.close();
					response.close();
				} catch (IOException e) {

					System.out.println("I/O、response流关闭");
					e.printStackTrace();
				}
			}
		}

		if (j == city.length) {
			System.out.println("天气信息爬取完成");
		}

	}

	/**
	 * 将爬取的预测数据存入数据库
	 * 
	 * @param args
	 */
	public void save(Object[] args) {
		Connection con = TDB.getConnection();
		String sql = "INSERT INTO weather_15d (id,city_id, maxTem, minTem, wind_direction, wind_power, weather, current1_time, forecast_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		QueryRunner qr = new QueryRunner();
		int m = 0;
		try {
			m = qr.update(con, sql, args);
			if (m <= 0) {
				System.out.println(args[0] + ": 该地区的天气数据爬取失败！");
			} else {
				System.out.println(args[0] + "地区" + args[8] + "储存成功");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtils.closeQuietly(con);
		}

	}

}
