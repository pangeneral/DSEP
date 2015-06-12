package com.dsep.controller.publicity.feedback;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.web.multipart.MultipartFile;

import com.dsep.domain.AttachmentHelper;
import com.dsep.domain.dsepmeta.feedback.FeedbackMessage;
import com.dsep.entity.User;
import com.dsep.entity.dsepmeta.FeedbackResponse;
import com.dsep.entity.enumeration.AttachmentType;
import com.dsep.entity.enumeration.EnumModule;
import com.dsep.entity.enumeration.feedback.ResponseType;
import com.dsep.service.base.AttachmentService;
import com.dsep.service.base.DisciplineService;
import com.dsep.service.base.UnitService;
import com.dsep.service.dsepmeta.dsepmetas.DMBackupService;
import com.dsep.service.dsepmeta.dsepmetas.DMCollectService;
import com.dsep.service.dsepmeta.dsepmetas.DMViewConfigService;
import com.dsep.service.publicity.feedback.FeedbackManagementService;
import com.dsep.service.publicity.feedback.FeedbackResponseService;
import com.dsep.service.publicity.process.production.feedback.FeedbackProcess;
import com.dsep.util.FileOperate;
import com.dsep.util.JsonConvertor;
import com.dsep.util.UserSession;


@Controller
@RequestMapping("feedback")
public class CenterResponseProcessController extends FeedbackController{
	@Resource(name="feedbackManagementService")
	private FeedbackManagementService feedbackManagementService;
	
	@Resource(name="feedbackResponseService")
	private FeedbackResponseService feedbackResponseService;
	
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
	
	@Resource(name = "attachmentService")
	private AttachmentService attachmentService;
	
	/**
	 * 进入中心反馈答复页面
	 * @param model
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("centerResponse")
	public String beginFeedback(Model model,HttpSession session) throws IllegalArgumentException, IllegalAccessException{	
		UserSession userSession = new UserSession(session);
		User user = userSession.getCurrentUser();
		//获取异议处理类
		FeedbackProcess process = userSession.getDsepProcessFactory().getFeedbackProcess();
		EnumModule module = new ResponseType();
		
		String result = JsonConvertor.obj2JSON(feedbackManagementService.getFeedbackTypeTree());
		Map<String,String> feedbackRoundMap = feedbackManagementService.getAllFeedbackRound();
		Map<String,String> joinDiscMap = process.getDiscMap(disciplineService);
		Map<String,String> joinUnitMap = process.getUnitMap(unitService);
		Map<String,String> statusMap = process.getResponseStatusMap();
		Map<String,String> responseTypeMap = module.getEnumMap();
		model.addAttribute("joinDisciplineMap", joinDiscMap);
		model.addAttribute("joinUnitMap", joinUnitMap);
		model.addAttribute("responseTypeMap",responseTypeMap);
		model.addAttribute("treeNodes", result);
		model.addAttribute("feedbackRoundMap", feedbackRoundMap);
		model.addAttribute("statusMap", statusMap);
		model.addAttribute("user",user);
		return "PublicAndFeedback/FeedBack/center_feedback_response";
	}
	
	/**
	 * 获取反馈数据的集合
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("centerResponse_getResponse")
	@ResponseBody
	public String getFeedbackResponse(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) throws IllegalArgumentException, IllegalAccessException{
		return super.getFeedbackVM(request, response, session, feedbackResponseService);
	}
	
	/**
	 * 对某条采集项可能会有多条反馈数据，此函数返回所有的反馈数据集合
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("centerResponse_getSameResponse")
	@ResponseBody
	public String getSameResponse(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) throws IllegalArgumentException, IllegalAccessException{
		return super.getSameResponse(request, response, session, feedbackResponseService);
	}
	
	/**
	 * 获取本地数据配置信息
	 */
	@RequestMapping("centerResponse_getViewConfig")
	@ResponseBody
	public String getCollectDataConfig(@RequestParam(value = "entityId") String entityId) {
		return super.getCollectDataConfig(dmViewConfigService, entityId);
	}
	
	/**
	 * 获取反馈项对应的采集项数据
	 * @return
	 */
	@RequestMapping("centerResponse_getCollectData")
	@ResponseBody
	public String pubDataDetail(HttpServletRequest request,
			HttpServletResponse response,String entityId,String backupVersionId){
		List<String> itemIds = new ArrayList<String>();
		itemIds.addAll(Arrays.asList(request.getParameter("itemIds")));
		if( itemIds.size() == 0 || itemIds.get(0) == null){
			String itemId = request.getParameter("itemId");
			itemIds.add(itemId);
		}
		return super.pubDataDetail(request, response, entityId, itemIds,backupVersionId, backupService, collectService);
	}
	
	/**
	 * 获取反馈批次的信息
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("centerResponse_getFeedbackRoundMessage")
	@ResponseBody
	public String getFeedbackRoundMessage(HttpServletRequest request,
			HttpServletResponse response){
		String feedbackRoundId = request.getParameter("feedback_round_id");
		FeedbackMessage theMessage = feedbackManagementService.getFeedbackRoundMessage(feedbackRoundId);
		return JsonConvertor.obj2JSON(theMessage);
	}
	
	/**
	 * 获取某条反馈答复
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("centerResponse_getCertainResponse")
	@ResponseBody
	public String getFeedbackResponse(HttpServletRequest request,
			HttpServletResponse response){
		String responseId = request.getParameter("responseItemId");
		FeedbackResponse feedbackResponse = feedbackResponseService.getResponseById(responseId);
		return JsonConvertor.obj2JSON(feedbackResponse);
	}
	
	/**
	 * 获取证明材料的下载路径
	 * @param id
	 * @return
	 */
	@RequestMapping("centerResponse_getDownloadPath")
	@ResponseBody
	public String downloadAttachment(String proveMaterialId){
		String json = JsonConvertor.obj2JSON(attachmentService.getAttachmentPath(proveMaterialId));
		return json;
	}
	
	/**
	 * 同意学校的处理意见
	 * @param id
	 * @return
	 */
	@RequestMapping("centerResponse_agree")
	@ResponseBody
	public boolean agreeUnitAdvice(HttpServletRequest request,HttpSession session){
		List<String> idList = Arrays.asList(request.getParameter("responseItemIdList").split(","));
		UserSession userSession = new UserSession(session);
		User user = userSession.getCurrentUser();
		return feedbackResponseService.agreeUnitAdvice(idList,user.getId());
	}
	
	/**
	 * 不同意学校的处理意见
	 * @param id
	 * @return
	 */
	@RequestMapping("centerResponse_disagree")
	@ResponseBody
	public boolean disagreeUnitAdvice(HttpServletRequest request,HttpSession session){
		List<String> idList = Arrays.asList(request.getParameter("responseItemIdList").split(","));
		UserSession userSession = new UserSession(session);
		User user = userSession.getCurrentUser();
		return feedbackResponseService.disagreeUnitAdvice(idList,user.getId());
	}
	
	@RequestMapping("centerResponse_downloadFeedbackResponse")
	@ResponseBody
	public String center_downloadFeedbackResponse(
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session){
		String realPath = request.getSession().getServletContext().getRealPath("/");
		String currentRoundId = request.getParameter("currenFeedbackRoundId");
		String feedbackType = request.getParameter("feedbackType");
		String feedbackStatus = request.getParameter("feedbackStatus");
		FeedbackResponse feedbackResponse = new FeedbackResponse();
		feedbackResponse.setFeedbackRoundId(currentRoundId);
		feedbackResponse.setFeedbackType(feedbackType);
		feedbackResponse.setFeedbackStatus(feedbackStatus);
		return feedbackResponseService.center_downloadFeedbackResponse(feedbackResponse,realPath);
		
	}

}
