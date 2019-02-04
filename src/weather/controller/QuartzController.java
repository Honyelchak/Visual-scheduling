package weather.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import weather.entity.JobInfo;
import weather.quartz.QuartzService;
import weather.quartz.QuartzServiceImpl;

@WebServlet("/QuartzController")
public class QuartzController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private QuartzService q;

	public QuartzController() {
		super();
		q = new QuartzServiceImpl();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		String action = request.getParameter("action");
		if (action.equals("get")) {
			getAll(request, response);
			return;
		} else if (action.equals("delete")) {
			delete(request, response);
		} else if (action.equals("add")) {
			add(request, response);
		} else if (action.equals("startAll")) {
			startAll(request, response);
		} else if (action.equals("modifyJobTime")) {
			modifyJobTime(request, response);
		} else if (action.equals("stopAll")) {
			stopAll(request, response);
		} else if (action.equals("resume")) {
			resume(request, response);
		} else if (action.equals("pause")) {
			pause(request, response);
		}
		getAll(request, response);
	}

	private void resume(HttpServletRequest request, HttpServletResponse response) {
		String jobName = request.getParameter("jobName");
		q.resumeJob(jobName);
	}

	private void getAll(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<JobInfo> allInfo = q.getAllInfo();
		boolean isStarted = q.isStarted();
		request.setAttribute("isStarted", isStarted);
		request.setAttribute("pageBean", allInfo);
		request.getRequestDispatcher("/index.jsp").forward(request, response);
	}

	private void add(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Class<?> cla;
		try {
			String jobName = request.getParameter("jobName");
			String triggerName = request.getParameter("triggerName");
			String jobClass = request.getParameter("jobClass");
			cla = Class.forName(jobClass);
			String cron = request.getParameter("cron");
			q.addJob(jobName, triggerName, cla, cron);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void delete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String jobName = request.getParameter("jobName");
		String triggerName = request.getParameter("triggerName");

		q.removeJob(jobName, triggerName);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

	private void startAll(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		q.startJobs();
	}

	private void stopAll(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		q.shutdownJobs();
	}

	private void modifyJobTime(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String jobName = request.getParameter("jobName");
		String triggerName = request.getParameter("triggerName");
		String cron = request.getParameter("cron");
		cron = cron.replace("？", "?");
		boolean f = q.modifyJobTime(jobName, triggerName, cron);
		if (f) {
			System.out.println(jobName + "任务时间修改完成！");
		} else {
			System.out.println(jobName + "任务时间修改失败！");
		}
	}

	private void pause(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String jobName = request.getParameter("jobName");
		q.pauseJob(jobName);
		System.out.println(jobName + "任务暂停成功！");
	}

}
