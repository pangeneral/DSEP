package com.dsep.controller.publicity.feedback;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dsep.domain.dsepmeta.viewconfig.ViewConfig;
import com.dsep.entity.dsepmeta.FeedbackResponse;
import com.dsep.service.dsepmeta.dsepmetas.DMBackupService;
import com.dsep.service.dsepmeta.dsepmetas.DMCollectService;
import com.dsep.service.dsepmeta.dsepmetas.DMViewConfigService;
import com.dsep.service.publicity.feedback.FeedbackResponseService;
import com.dsep.service.publicity.process.production.feedback.FeedbackProcess;
import com.dsep.service.publicity.process.production.objection.ObjectionProcess;
import com.dsep.util.JsonConvertor;
import com.dsep.util.StringProcess;
import com.dsep.util.UserSession;
import com.dsep.vm.JqgridVM;
import com.dsep.vm.PageVM;
import com.dsep.vm.feedback.FeedbackResponseVM;
import com.dsep.vm.publicity.OriginalObjectionVM;

public abstract class FeedbackController {
	
	/**
	 * 获取反馈答复数据的列表
	 * @param request
	 * @param response
	 * @param session
	 * @param feedbackResponseService
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public String getFeedbackVM(HttpServletRequest request,
			HttpServletResponse response, HttpSession session
			,FeedbackResponseService feedbackResponseService) throws IllegalArgumentException, IllegalAccessException{
		UserSession us = new UserSession(session);
		FeedbackProcess feedbackProcess = us.getDsepProcessFactory().getFeedbackProcess();

		String sord = request.getParameter("sord");
		String orderName = request.getParameter("sidx");
		int pageIndex = Integer.parseInt(request.getParameter("page"));
		int pageSize = Integer.parseInt(request.getParameter("rows"));
		boolean order_flag = false;
		if ("desc".equals(sord)) {
			order_flag = true;
		}
		
		String problemDiscId = request.getParameter("problemDiscId");
		String problemUnitId = request.getParameter("problemUnitId");
		String currentFeedbackRoundId = request.getParameter("currentRoundId");
		String feedbackStatus = request.getParameter("feedbackStatus");
		String feedbackType = request.getParameter("feedbackType");
		String responseType = request.getParameter("responseType");
		
		FeedbackResponse queryResponse = new FeedbackResponse();
		queryResponse.setFeedbackRoundId(currentFeedbackRoundId);
		queryResponse.setFeedbackType(feedbackType);
		
		if( !StringProcess.isNull(feedbackStatus)){
			queryResponse.setFeedbackStatus(feedbackStatus);
		}
		if( !StringProcess.isNull(problemDiscId) ){
			queryResponse.setProblemDiscId(problemDiscId);
		}
		if( !StringProcess.isNull(problemUnitId) ){
			queryResponse.setProblemUnitId(problemUnitId);
		}
		if( !StringProcess.isNull(responseType)){
			queryResponse.setResponseType(responseType);
		}
		
		PageVM<FeedbackResponseVM> pageVM = feedbackProcess.getFeedbackResponse(queryResponse,order_flag, orderName, pageIndex, pageSize, feedbackResponseService);
		return JsonConvertor.obj2JSON(pageVM.getGridData());
	}
	
	/**
	 * 获取对某条采集项提出的所有反馈问题
	 * @param request
	 * @param response
	 * @param session
	 * @param feedbackResponseService
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public String getSameResponse(HttpServletRequest request,
			HttpServletResponse response, HttpSession session
			,FeedbackResponseService feedbackResponseService) throws IllegalArgumentException, IllegalAccessException{
		String sord = request.getParameter("sord");
		String orderName = request.getParameter("sidx");
		int pageIndex = Integer.parseInt(request.getParameter("page"));
		int pageSize = Integer.parseInt(request.getParameter("rows"));
		boolean order_flag = false;
		if ("desc".equals(sord)) {
			order_flag = true;
		}
		
		String currentFeedbackRoundId = request.getParameter("currentRoundId");
		String feedbackStatus = request.getParameter("feedbackStatus");
		String problemCollectItemId = request.getParameter("problemCollectItemId");
		
		FeedbackResponse queryResponse = new FeedbackResponse();
		queryResponse.setFeedbackRoundId(currentFeedbackRoundId);
		queryResponse.setProblemCollectItemId(problemCollectItemId);
		
		if( !StringProcess.isNull(feedbackStatus)){
			queryResponse.setFeedbackStatus(feedbackStatus);
		}
		
		PageVM<FeedbackResponseVM> pageVM = feedbackResponseService.getSameResponseVM(pageIndex, pageSize, order_flag, orderName, queryResponse);
		return JsonConvertor.obj2JSON(pageVM.getGridData());
	}
	
	
	public String getCollectDataConfig(DMViewConfigService dmViewConfigService,String entityId) {
		ViewConfig viewConfig = dmViewConfigService.getViewConfig(entityId);
		String configData = JsonConvertor.obj2JSON(viewConfig);
		return configData;
	}
	

	public String pubDataDetail(HttpServletRequest request,
			HttpServletResponse response,String entityId, List<String> itemIds,
			String backupVersionId,
			DMBackupService backupService, DMCollectService collectService){
		String sord = request.getParameter("sord");
		String sidx = request.getParameter("sidx");
		int page = Integer.parseInt(request.getParameter("page"));
		int pageSize = Integer.parseInt(request.getParameter("rows"));
		
		boolean order_flag = false;
		if ("desc".equals(sord)) {
			order_flag = true;
		}
		
		JqgridVM jqgridVM;
		if( StringProcess.isNull(backupVersionId)){
			jqgridVM = collectService.getCollectDataDetail(
				entityId, itemIds ,sidx, order_flag, page, pageSize);
		}
		else{
			jqgridVM = backupService.getCollectDataDetail(entityId, itemIds, backupVersionId, sidx, order_flag, page, pageSize);
		}
		String result = JsonConvertor.obj2JSON(jqgridVM);
		return result;
	}
}
