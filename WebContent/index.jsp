<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>可视化任务调配</title>
<link rel="stylesheet"
	href="https://cdn.staticfile.org/twitter-bootstrap/3.3.7/css/bootstrap.min.css">
<script src="https://cdn.staticfile.org/jquery/2.1.1/jquery.min.js"></script>
<script
	src="https://cdn.staticfile.org/twitter-bootstrap/3.3.7/js/bootstrap.min.js"></script>
</head>
<body width="100%" style="padding:0 10px;">
	<br />
	<form action="${pageContext.request.contextPath}/QuartzController"
					method="get">
	<table style="width:50%;" class="table" border="1">
		
		<tbody>
			
			<tr>
				<th>任务名称</th>
				<td>
					<input type="text" name="action" value="add" hidden/>
					<input type="text" name="jobName"/>
					<i>不能和之前的重复</i>
				</td>
			</tr>
			<tr>
				<th>触发器名称</th>
				<td>
					<input type="text" name="triggerName"/>
					<i>不能和之前的重复</i>
				</td>
			</tr>
			<tr>
				<th>任务类</th>
				<td>
					<input type="text" placeholder="weather.job.xxx" name="jobClass"/>
					<i>"包名.类名"</i>
				</td>
				
			</tr>
			<tr>
				<th>Cron时间表达式</th>
				<td><input type="text" name="cron"/></td>
			</tr>
			<tr>
				<td><input class="btn btn-info" type="reset" value="重置"></td>
				<td><input class="btn btn-success"type="submit" value="添加"/></td>
			</tr>
		</tbody>
		</table>
		</form>
	<hr />
	<br />
	
	<a id="start" href="${pageContext.request.contextPath }/QuartzController?action=startAll" class="btn btn-success">开始调度器</a>
	<a id="stop" href="${pageContext.request.contextPath }/QuartzController?action=stopAll" class="btn btn-danger">停止调度器</a>
	<script>
		function test(){
			var i = "${requestScope.isStarted}";
			//console.log(i);
			if(i == 'false'){
				$("#stop").hide();
				$("#start").show();
			} else {
				$("#stop").show();
				$("#start").hide();
			} 
		}
		test();
	</script>
	<br />
	<br />

	<table class="table">
		<thead>
			<tr>
				<th><label> <input type="checkbox" />
				</label></th>
				<th>作业名称</th>
				<th>触发器名称</th>
				<th>时间表达式</th>
				<th>上一次执行时间</th>
				<th>下一次执行时间</th>
				<th>工作开始时间</th>
				<th>工作结束时间</th>
				<th>触发器状态</th>
				<th>作业类</th>
				<th>misFire策略</th>
				<th>优先级</th>
				<th class="hidden-480"><i>操作</i></th>
			</tr>
		</thead>
		<tbody>
			<c:choose>
				<c:when test="${not empty requestScope.pageBean}">
					<c:forEach var="job" items="${requestScope.pageBean}"
						varStatus="vs">
						<tr>
							<td class="center"><label class="position-relative">
									<input type="checkbox" class="ace" /> <span class="lbl"></span>
							</label></td>
							<td class="jobName">${job.jobName }</td>
							<td class="triggerName">${job.triggerName }</td>
							<td class="cronExpression">${job.cronExpression }</td>
							<td class="previousExecuteTime"><fmt:formatDate
									value="${job.previousExecuteTime }" pattern="yyyy-MM-dd HH:mm" /></td>
							<td class="nextExecuteTime"><fmt:formatDate
									value="${job.nextExecuteTime }" pattern="yyyy-MM-dd HH:mm" /></td>
							<td class="startTime"><fmt:formatDate value="${job.startTime }"
									pattern="yyyy-MM-dd HH:mm" /></td>
							<td class="endTime"><fmt:formatDate value="${job.endTime }"
									pattern="yyyy-MM-dd HH:mm" /></td>
							<td class="trigggerState">${job.trigggerState }</td>
							<td class="jobClass">${job.jobClass }</td>
							<td class="misFireType">${job.misFireType }</td>
							<td class="priority">${job.priority }</td>
							<td>
								<div class="hidden-sm hidden-xs btn-group">
									<a index = '1'
										href="${pageContext.request.contextPath }/QuartzController?action=pause&jobName=${job.jobName}"
										id="pause"
										>
										<button class="btn btn-danger">
											<i>暂停</i>
										</button>
									</a>
									<a index = '2'
										href="${pageContext.request.contextPath }/QuartzController?action=delete&jobName=${job.jobName}&triggerName=${job.triggerName}">
										<button class="btn btn-success">
											<i>删除</i>
										</button>
									</a> 
									<a index="3" id="flag" onclick="click1(this)"
										data-toggle="modal" data-target="#myModal">
										<button class="btn btn-info">
											<i>修改</i>
										</button>
									</a>
									
								</div>
							</td>
						</tr>
					</c:forEach>
				</c:when>
			</c:choose>

		</tbody>
	</table>
	<h3>&nbsp;&nbsp;Tips:</h3>
	<ul>
		<li>Test:主页面入口<a href="${pageContext.request.contextPath}/QuartzController?action=get">${pageContext.request.contextPath}/QuartzController?action=get</a></li>
		<li>服务器启动之后，应该先<b>开启调度器</b>(即调度器开启之后，Job任务才能执行)</li>
		<li>添加时 作业名称(jobName)和触发器名称(triggerName)不能和之前的重复.</li>
		<li>时间表达式Cron可参考 <a target="_blank" href="http://cron.qqe2.com/">在线Cron时间表达生成</a></li>
	</ul>
	<!-- 模态框（Modal） -->
	<div class="modal fade" id="myModal" tabindex="-1" role="dialog"
		aria-labelledby="myModalLabel" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-hidden="true">&times;</button>
					<h4 class="modal-title" id="myModalLabel">修改任务时间</h4>
				</div>
				<form action="${pageContext.request.contextPath}/QuartzController"
					method="get">
					<div class="modal-body">
						<input type="text" name="action" value="modifyJobTime"  hidden/>
						<input id="jobName" type="text" name="jobName"  hidden/> <input
							id="triggerName" type="text" name="triggerName"  hidden/> <input
							type="text" name="cron" placeholder="请输入cron表达式" value="" />
						<h5>Tips:</h5>
						<p>"0 0 * * * ?" 表示为每天整点执行</p>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">关闭
						</button>
						<button type="submit" class="btn btn-primary">提交更改</button>
					</div>
				</form>
			</div>
			<!-- /.modal-content -->
		</div>
		<!-- /.modal -->
	</div>

	<script>
	
	function click1(e){
		console.log(e);
		var tr = $(e).parent().parent().parent();
		var td1 = tr.find("td[class='jobName']");
		var td2 = tr.find("td[class='triggerName']");
		//console.log(td1[0].innerText);
		//console.log(td2[0].innerText);
		$("#jobName[type=text]").attr("value", td1[0].innerText);
		$("#triggerName[type=text]").attr("value", td2[0].innerText);
		//$('#myModal').modal('show');
	}
	$(document).ready(function(){
		$(".trigggerState").each(function(){
 			if($(this).text() == "PAUSED"){
				$(this).parent().find("td a[index=1] button i").text("启动");
				$(this).parent().find("td a[index=1] button").removeClass("btn-danger");
				$(this).parent().find("td a[index=1] button").addClass("btn-info");
				var t = $(this).parent().find("td[class=jobName]")[0].innerText;
				var i = "${pageContext.request.contextPath }/QuartzController?action=resume&jobName=" + t;
				$(this).parent().find("td div a[index=1]").attr("href", i);
			} else {
				$(this).parent().find("td a[index=1] button i").text("停止");
				$(this).parent().find("td a[index=1] button").removeClass("btn-info");
				$(this).parent().find("td a[index=1] button").addClass("btn-danger");
				var t = $(this).parent().find("td[class=jobName]")[0].innerText;
				var i = "${pageContext.request.contextPath }/QuartzController?action=pause&jobName=" + t;
				$(this).parent().find("td div a[index=1]").attr("href", i);
			}
		});
	});
	</script>
</body>
</html>