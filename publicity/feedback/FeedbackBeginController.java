package com.dsep.controller.publicity.feedback;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dsep.common.exception.BusinessException;
import com.dsep.domain.dsepmeta.viewconfig.ViewConfig;
import com.dsep.entity.User;
import com.dsep.entity.dsepmeta.FeedbackManagement;
import com.dsep.entity.dsepmeta.FeedbackResponse;
import com.dsep.service.base.DisciplineService;
import com.dsep.service.base.UnitService;
import com.dsep.service.dsepmeta.dsepmetas.DMBackupService;
import com.dsep.service.dsepmeta.dsepmetas.DMCollectService;
import com.dsep.service.dsepmeta.dsepmetas.DMViewConfigService;
import com.dsep.service.publicity.feedback.FeedbackImportService;
import com.dsep.service.publicity.feedback.FeedbackManagementService;
import com.dsep.service.publicity.feedback.FeedbackResponseService;
import com.dsep.service.publicity.process.production.feedback.FeedbackProcess;
import com.dsep.service.publicity.process.production.objection.ObjectionProcess;
import com.dsep.util.JsonConvertor;
import com.dsep.util.StringProcess;
import com.dsep.util.UserSession;
import com.dsep.vm.JqgridVM;
import com.dsep.vm.feedback.PreFeedBackManagementVM;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

@Controller
@RequestMapping("feedback")
public class FeedbackBeginController extends FeedbackController{
	
	@Resource(name="feedbackManagementService")
	private FeedbackManagementService feedbackManagementService;
	
	@Resource(name="feedbackResponseService")
	private FeedbackResponseService feedbackResponseService;
	
	@Resource(name="feedbackImportService")
	private FeedbackImportService feedbackImportService;
	
	@Resource(name="collectService")
	private DMCollectService collectService;

	@Resource(name="backupService")
	private DMBackupService backupService;
	
	@Resource(name="dmViewConfigService")
	private DMViewConfigService dmViewConfigService;
	
	@Resource(name="disciplineService")
	private DisciplineService disciplineService;
	
	@Resource(name="unitService")
	private UnitService unitService;
	
	/**
	 * 进入中心反馈配置页面
	 * @param model
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("beginRound")
	public String beginFeedback(Model model) throws IllegalArgumentException, IllegalAccessException{	
		String result = JsonConvertor.obj2JSON(feedbackManagementService.getFeedbackTypeTree());
		FeedbackManagement currentRound = feedbackManagementService.getCurrentFeedbackRound();
		Map<String,String> joinDiscMap = disciplineService.getAllEvalDiscMap();
		Map<String,String> joinUnitMap = unitService.getAllEvalUnitMap();
		model.addAttribute("joinDisciplineMap", joinDiscMap);
		model.addAttribute("joinUnitMap", joinUnitMap);
		model.addAttribute("treeNodes", result);
		model.addAttribute("currentRound", currentRound);
		return "PublicAndFeedback/FeedBack/center_begin_feedback";
	}
	
	/**
	 * 打开进行反馈的对话框
	 * @return
	 */
	@RequestMapping("beginRound_beginFeedbackDialog")
	public String beginFeedbackDialog(Model model) throws IllegalArgumentException, IllegalAccessException {
		PreFeedBackManagementVM currentRound = new PreFeedBackManagementVM(feedbackManagementService.getCurrentFeedbackRound());
		model.addAttribute("currentRound", currentRound);
		return "PublicAndFeedback/FeedBack/center_begin_feedback_dialog";
	}
	
	/**
	 * 对于某条采集项数据可能会有多条反馈项，此页面处理那些重复的反馈项
	 * @param model
	 * @return
	 */
	@RequestMapping("beginRound_processSameProblem")
	public String processSameProblem(Model model) throws IllegalArgumentException, IllegalAccessException{
		FeedbackManagement currentRound = feedbackManagementService.getCurrentFeedbackRound();
		Map<String,String> joinDiscMap = disciplineService.getAllEvalDiscMap();
		Map<String,String> joinUnitMap = unitService.getAllEvalUnitMap();
		model.addAttribute("joinDisciplineMap", joinDiscMap);
		model.addAttribute("currentRound", currentRound);
		model.addAttribute("joinUnitMap", joinUnitMap);
		return "PublicAndFeedback/FeedBack/center_process_same_problem";
	}
	
	@RequestMapping("beginRound_getProcessEntity")
	@ResponseBody
	public String getSameItemProblem(HttpServletRequest request,String currentRoundId){
		String sord = request.getParameter("sord");
		String orderName = request.getParameter("sidx");
		int pageIndex = Integer.parseInt(request.getParameter("page"));
		int pageSize = Integer.parseInt(request.getParameter("rows"));
		boolean order_flag = false;
		if ("desc".equals(sord)) {
			order_flag = true;
		}
		
		String unitId = request.getParameter("problemUnitId");
		String discId = request.getParameter("problemDiscId");
		
		FeedbackResponse conditionalResponse = new FeedbackResponse();
		conditionalResponse.setFeedbackRoundId(currentRoundId);
		if( unitId != null && !unitId.equals("")){
			conditionalResponse.setProblemUnitId(unitId);
		}
		if( discId != null && !discId.equals("")){
			conditionalResponse.setProblemDiscId(discId);
		}
		
		JqgridVM theVm = feedbackResponseService.getSameItemJqgridVM(pageIndex, 
			pageSize, order_flag, orderName, conditionalResponse);
		return JsonConvertor.obj2JSON(theVm);
	}
	
	/**
	 * 获取反馈数据
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("beginRound_getResponse")
	@ResponseBody
	public String getFeedbackResponsePage(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) throws IllegalArgumentException, IllegalAccessException{
		return super.getFeedbackVM(request, response, session, feedbackResponseService);
	}
	
	/**
	 * 获取对于某采集数据项提出的所有问题
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@RequestMapping("beginRound_getSameResponse")
	@ResponseBody
	public String getSameResponse(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) throws IllegalArgumentException, IllegalAccessException{
		return super.getSameResponse(request, response, session, feedbackResponseService);
	}
	
	/**
	 * 开启新的反馈批次
	 * @return
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@RequestMapping("beginRound_openNewRound")
	@ResponseBody
	public boolean openNewFeedbackRound(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) throws IllegalArgumentException, IllegalAccessException{
		return feedbackManagementService.openNewFeedbackRound();
	}
	
	@RequestMapping("beginRound_beginFeedback")
	@ResponseBody
	public boolean beginFeedback(HttpServletRequest request) throws IllegalArgumentException, IllegalAccessException, ParseException{
		String beginTime = request.getParameter("beginTime");
		String endTime = request.getParameter("endTime");
		String feedbackName = request.getParameter("feedbackName");
		String remark = request.getParameter("remark");
		boolean result = feedbackManagementService.beginFeedback(feedbackName,beginTime,endTime,remark);
		return result;
	}
	
	/**
	 * 立即反馈
	 * @return
	 */
	@RequestMapping("beginRound_immediateFeedback")
	@ResponseBody
	public String immediateFeedback(HttpServletRequest request) throws IllegalArgumentException, IllegalAccessException, ParseException{
		return feedbackManagementService.immediateFeedback();
	}
	
	@RequestMapping("beginRound_isAllSimilarItemProcess")
	@ResponseBody
	public String isAllSimilarItemProcess(HttpServletRequest request){
		String currentFeedbackRoundId = request.getParameter("feedbackRoundId");
		if( feedbackResponseService.getAllSameItemNumber(currentFeedbackRoundId) > 0){
			return "false";
		}
		else
			return "true";
	}
	
	/**
	 * 删除某一条反馈项
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 */
	@RequestMapping("beginRound_deleteRow")
	@ResponseBody
	public boolean deleteFeedbackRow(HttpServletRequest request,
			HttpServletResponse response, HttpSession session){
		String feedbackId = request.getParameter("feedbackId");
		return feedbackResponseService.deleteFeedbackResponse(feedbackId);
	}
	
	/**
	 * 导入反馈数据源
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("beginRound_importObjection")
	@ResponseBody
	public int importOriginalObjection(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) throws IllegalArgumentException, IllegalAccessException{
		String feedbackRoundId = request.getParameter("feedbackRoundId");
		String feedbackType = request.getParameter("feedbackType");
		return feedbackImportService.importFeedbackDataSource(feedbackRoundId, feedbackType);
	}
	
	/**
	 * 获取采集数据配置信息
	 */
	@RequestMapping("beginRound_getViewConfig")
	@ResponseBody
	public String getCollectDataConfig(@RequestParam(value = "entityId") String entityId) {
		return super.getCollectDataConfig(dmViewConfigService, entityId);
	}
	
	/**
	 * 获取反馈项对应的采集项数据
	 * @return
	 */
	@RequestMapping("beginRound_getCollectData")
	@ResponseBody
	public String pubDataDetail(HttpServletRequest request,
			HttpServletResponse response,String entityId,String backupVersionId){
		/*List<String> itemIds = new ArrayList<String>();*/
		List<String> itemArray = Arrays.asList(request.getParameter("itemIds"));
		List<String> itemIds = Arrays.asList(itemArray.get(0).split(","));
		if( itemIds.size() == 0 || itemIds.get(0) == null){
			String itemId = request.getParameter("itemId");
			itemIds.add(itemId);
		}
		return super.pubDataDetail(request, response, entityId,itemIds,backupVersionId,backupService, collectService);
	}
	
	/**
	 * 导出所有已提交的异议到excel中
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 */
	@RequestMapping("beginRound_downloadFeedbackResponse")
	@ResponseBody
	public String viewObjection_downloadSubmitObjection(
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session) {
//		UserSession us = new UserSession(session);
//		User user = us.getCurrentUser();
//		FeedbackProcess process = us.getDsepProcessFactory().getFeedbackProcess();
		String realPath = request.getSession().getServletContext().getRealPath("/");
		String currentRoundId = request.getParameter("currentFeedbackRoundId");
		String feedbackType = request.getParameter("feedbackType");
		FeedbackResponse feedbackResponse = new FeedbackResponse();
		feedbackResponse.setFeedbackRoundId(currentRoundId);
		feedbackResponse.setFeedbackType(feedbackType);
	//	return process.downloadFeedbackResponse(null, currentRoundId, feedbackType, realPath);
		return feedbackResponseService.downloadFeedbackResponse(feedbackResponse,realPath);
	}
	
}
