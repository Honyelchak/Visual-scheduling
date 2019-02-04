package weather.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class TDB {
	private static String driver;
	private static String url;
	private static String user;
	private static String pwd;
	private static String host;
	private static Integer port;
	private static Statement statement;
	private static Connection conn;
	private static ResultSet rs;

	static {
		try {
			// 拿数据库配置文件所在目录
			URL fileurl = TDB.class.getResource("/application-dev.properties");
			String file = "";
			if (fileurl != null) {

				file = fileurl.getFile();// 拿到路径字符串表示形式
				Properties properties = new Properties();
				InputStream inputStream = new FileInputStream(file);
				properties.load(inputStream);// 通过流对象，让properties把数据库配置信息拿过来

				driver = properties.getProperty("driver-class-name");
				url = properties.getProperty("url");
				user = properties.getProperty("username");
				pwd = properties.getProperty("password");

				if (!(url == null || url == "")) {
					String ipwithport = "";
					if (url.contains("///")) {
						host = "127.0.0.1";
						port = 3306;
					}
					if (url.contains("//")) {
						ipwithport = url.substring(url.indexOf("//") + 2,
								url.lastIndexOf("/"));
						host = ipwithport.substring(0, ipwithport.indexOf(":"));
						port = Integer.parseInt(ipwithport.substring(ipwithport
								.indexOf(":") + 1));
					}
				}
				// System.out.println(driver+":"+url+":"+user+":"+pwd);
				Class.forName(driver);
			} else {
				throw new RuntimeException("找不到数据库配置文件");
			}

		} catch (FileNotFoundException e) {
			System.out.println("找不到数据库配置文件");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("读取数据库配置文件错误");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("未找到驱动程序");
			e.printStackTrace();
		}
	}

	public static Connection getConnection() {
		try {
			conn = DriverManager.getConnection(url, user, pwd);
			return conn;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ResultSet query(String sql) {

		try {
			statement = getConnection().createStatement();
			rs = statement.executeQuery(sql);
			return rs;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int execUpdate(String sql) {
		try {
			Statement statement = getConnection().createStatement();
			int count = statement.executeUpdate(sql);
			return count;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void close() {
		if (rs != null)
			try {
				rs.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			statement = null;
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			conn = null;
		}
	}

	public static PreparedStatement getPreparedStatement(String sql) {
		try {
			return getConnection().prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getDriver() {
		return driver;
	}

	public static String getUrl() {
		return url;
	}

	public static String getUser() {
		return user;
	}

	public static String getPwd() {
		return pwd;
	}

	public static String getHost() {
		return host;
	}

	public static Integer getPort() {
		return port;
	}
}
