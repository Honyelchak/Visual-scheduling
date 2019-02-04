package weather.job;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class Test implements Job {

	@Override
	public void execute(JobExecutionContext jobContext) {
		Date d = new Date();
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		System.out.println("你好" + s.format(d));
	}

}
