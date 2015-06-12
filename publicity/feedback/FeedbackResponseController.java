package com.dsep.controller.publicity.feedback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;









import com.dsep.domain.AttachmentHelper;
import com.dsep.domain.dsepmeta.feedback.FeedbackMessage;
import com.dsep.entity.User;
import com.dsep.entity.dsepmeta.FeedbackManagement;
import com.dsep.entity.dsepmeta.FeedbackResponse;
import com.dsep.entity.enumeration.AttachmentType;
import com.dsep.entity.enumeration.EnumModule;
import com.dsep.entity.enumeration.feedback.FeedbackResponseStatus;
import com.dsep.service.base.AttachmentService;
import com.dsep.service.base.DisciplineService;
import com.dsep.service.base.UnitService;
import com.dsep.service.dsepmeta.dsepmetas.DMBackupService;
import com.dsep.service.dsepmeta.dsepmetas.DMCheckLogicRuleService;
import com.dsep.service.dsepmeta.dsepmetas.DMCollectService;
import com.dsep.service.dsepmeta.dsepmetas.DMViewConfigService;
import com.dsep.service.publicity.feedback.FeedbackManagementService;
import com.dsep.service.publicity.feedback.FeedbackResponseService;
import com.dsep.service.publicity.process.production.feedback.FeedbackProcess;
import com.dsep.util.FileOperate;
import com.dsep.util.JsonConvertor;
import com.dsep.util.MySessionContext;
import com.dsep.util.UserSession;


@Controller
@RequestMapping("feedback")
public class FeedbackResponseController extends FeedbackController{

	@Resource(name="feedbackManagementService")
	private FeedbackManagementService feedbackManagementService;
	
	@Resource(name="feedbackResponseService")
	private FeedbackResponseService feedbackResponseService;
	
	@Resource(name="backupService")
	private DMBackupService backupService;
	
	@Resource(name="collectService")
	private DMCollectService collectService;

	@Resource(name="dmViewConfigService")
	private DMViewConfigService dmViewConfigService;
	
	@Resource(name="disciplineService")
	private DisciplineService disciplineService;
	
	@Resource(name="unitService")
	private UnitService unitService;
	
	@Resource(name = "attachmentService")
	private AttachmentService attachmentService;
	
	@Resource(name = "checkLogicRule")
	private DMCheckLogicRuleService checkLogicRule;
	
	/**
	 * 进入反馈答复页面
	 * @param model
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("feedResponse")
	public String beginFeedback(Model model,HttpSession session) throws IllegalArgumentException, IllegalAccessException{	
		UserSession userSession = new UserSession(session);
		User user = userSession.getCurrentUser();
		//获取异议处理类
		FeedbackProcess process = userSession.getDsepProcessFactory().getFeedbackProcess();
		
		String result = JsonConvertor.obj2JSON(feedbackManagementService.getFeedbackTypeTree());
		Map<String,String> feedbackRoundMap = feedbackManagementService.getAllFeedbackRound();
		Map<String,String> joinDiscMap = process.getDiscMap(disciplineService);
		Map<String,String> joinUnitMap = process.getUnitMap(unitService);
		Map<String,String> statusMap = process.getResponseStatusMap();
		EnumModule module = new FeedbackResponseStatus();
		
		model.addAttribute("responseStatusMap",JsonConvertor.mapJSON(module.getEnumMap()));
		model.addAttribute("joinDisciplineMap", joinDiscMap);
		model.addAttribute("joinUnitMap", joinUnitMap);
		model.addAttribute("treeNodes", result);
		model.addAttribute("feedbackRoundMap", feedbackRoundMap);
		model.addAttribute("statusMap", statusMap);
		model.addAttribute("user",user);
		return "PublicAndFeedback/FeedBack/unit_feedback_gather";
	}
	
	@RequestMapping("feedResponse_getResponse")
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
	@RequestMapping("feedResponse_getSameResponse")
	@ResponseBody
	public String getSameResponse(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) throws IllegalArgumentException, IllegalAccessException{
		return super.getSameResponse(request, response, session, feedbackResponseService);
	}
	
	/**
	 * 获取本地数据配置信息
	 */
	@RequestMapping("feedResponse_getViewConfig")
	@ResponseBody
	public String getCollectDataConfig(@RequestParam(value = "entityId") String entityId) {
		return super.getCollectDataConfig(dmViewConfigService, entityId);
	}
	
	/**
	 * 获取反馈项对应的采集项数据
	 * @return
	 */
	@RequestMapping("feedResponse_getCollectData")
	@ResponseBody
	public String pubDataDetail(HttpServletRequest request,
			HttpServletResponse response,String entityId,String backupVersionId){
		/*List<String> itemIds = new ArrayList<String>();
		itemIds.addAll(Arrays.asList(request.getParameter("itemIds")));*/
		List<String> itemArray = Arrays.asList(request.getParameter("itemIds"));
		List<String> itemIds = Arrays.asList(itemArray.get(0).split(","));
		if( itemIds.size() == 0 || itemIds.get(0) == null){
			String itemId = request.getParameter("itemId");
			itemIds.add(itemId);
		}
		return super.pubDataDetail(request, response, entityId, itemIds, backupVersionId,backupService, collectService);
	}
	
	/**
	 * 获取反馈批次信息
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("feedResponse_getFeedbackRoundMessage")
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
	@RequestMapping("feedResponse_getCertainResponse")
	@ResponseBody
	public String getFeedbackResponse(HttpServletRequest request,
			HttpServletResponse response){
		String responseId = request.getParameter("responseItemId");
		FeedbackResponse feedbackResponse = feedbackResponseService.getResponseById(responseId);
		return JsonConvertor.obj2JSON(feedbackResponse);
	}
	
	/**
	 * 单位填写完反馈答复的修改意见后进行逻辑检查
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("feedResponse_logicCheck")
	@ResponseBody
	public String responseLogicCheck(HttpServletRequest request,
			HttpServletResponse response){
		String entityId = request.getParameter("entityId");
		String attrName = request.getParameter("attrId");
		String attrValue = request.getParameter("attrValue");
		return checkLogicRule.checkSingleField(entityId, attrName, attrValue);
	}
	
	/**
	 * 对于同一条数据项如果有多条反馈
	 * @param request
	 * @param response
	 * @return
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@RequestMapping("feedResponse_saveDeleteResponse")
	@ResponseBody
	public boolean saveDeleteResponse(HttpServletRequest request,
			HttpServletResponse response) throws IllegalArgumentException, IllegalAccessException{
		String feedbackRoundId = request.getParameter("feedbackRoundId");
		String problemEntityItemId = request.getParameter("problemEntityItemId");
		String responseItemId = request.getParameter("responseItemId");
		String responseType = request.getParameter("responseType");
		String adviceValue = request.getParameter("adviceValue");
		String responseAdvice = request.getParameter("responseAdvice");
		return feedbackResponseService.saveDeleteResponse(problemEntityItemId,responseItemId,responseType,adviceValue,responseAdvice,feedbackRoundId);
	}
	
	/**
	 * 保存反馈答复
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("feedResponse_saveResponse")
	@ResponseBody
	public boolean saveResponse(HttpServletRequest request,
			HttpServletResponse response){
		String responseItemId = request.getParameter("responseItemId");
		String responseType = request.getParameter("responseType");
		String adviceValue = request.getParameter("adviceValue");
		String responseAdvice = request.getParameter("responseAdvice");
		return feedbackResponseService.saveResponse(responseItemId,responseType,adviceValue,responseAdvice);	
	}
	
	/**
	 * 提交所有的反馈答复
	 * @param request
	 * @return
	 */
	@RequestMapping("feedResponse_submitResponse")
	@ResponseBody
	public boolean submitFeedbackResponse(HttpServletRequest request,
			HttpServletResponse response){
		String unitId = request.getParameter("unitId");
		String feedbackType = request.getParameter("feedbackType");
		String feedbackRoundId = request.getParameter("feedbackRoundId");
		return feedbackResponseService.submitResponse(unitId,feedbackType,feedbackRoundId);
	}
	
	/**
	 * 是否所有的反馈项均给出了答复意见
	 * @param request
	 * @return
	 */
	@RequestMapping("feedResponse_isAllAdvice")
	@ResponseBody
	public boolean isAllAdvice(HttpServletRequest request,
			HttpServletResponse response){
		String unitId = request.getParameter("unitId");
		String feedbackRoundId = request.getParameter("feedbackRoundId");
		String feedbackType = request.getParameter("feedbackType");
		return feedbackResponseService.isAllAdvice(unitId, feedbackType, feedbackRoundId);
	}
	
	@RequestMapping("feedResponse_isAllProfMaterial")
	@ResponseBody
	public boolean isAllProfMaterial(HttpServletRequest request,
			HttpServletResponse response){
		String unitId = request.getParameter("unitId");
		String feedbackRoundId = request.getParameter("feedbackRoundId");
		String feedbackType = request.getParameter("feedbackType");
		return feedbackResponseService.isAllProfMaterial(unitId,feedbackType,feedbackRoundId);
	}
	
	/**
	 * 证明材料上传成功后将证明材料的ID保存到相应的反馈项中
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("feedResponse_saveProveMaterial")
	@ResponseBody
	public boolean saveProveMaterial(HttpServletRequest request,
			HttpServletResponse response){
		String responseItemId = request.getParameter("responseItemId");
		String proveMaterialId = request.getParameter("proveMaterialId");
		return feedbackResponseService.uploadFile(responseItemId, proveMaterialId);
	}
	
	/**
	 * 学校提交反馈后查看具体的反馈意见
	 * @param request
	 * @param reponse
	 * @return
	 */
	@RequestMapping("feedResponse_queryAdviceDetail")
	@ResponseBody
	public String queryAdviceDetail(HttpServletRequest request,HttpServletResponse reponse){
		String responseItemId = request.getParameter("responseItemId");
		return feedbackResponseService.getFeedbackAdvice(responseItemId);
	}
	
	/**
	 * 上传证明材料
	 * @param file
	 * @return
	 */
	@RequestMapping("feedResponse_uploadFile")
	@ResponseBody
	public String uploadAttachment(HttpServletRequest request, String jsessionid){
		MySessionContext myc= MySessionContext.getInstance();
		HttpSession session = myc.getSession(jsessionid);
		
		UserSession userSession = new UserSession(session);
		User user = userSession.getCurrentUser();
		MultipartFile file = FileOperate.getFile(request);
		AttachmentHelper ah = attachmentService.getAttachmentHelper(file, user.getId(), AttachmentType.FEEDBACK, user.getUnitId());
		if(FileOperate.upload(file, ah.getPath(), ah.getStorageName())){
			return attachmentService.addAttachment(ah.getAttachment());
		}	
		else{
			return null;
		}
	}
	
	/**
	 * 获取证明材料的下载路径
	 * @param id
	 * @return
	 */
	@RequestMapping("feedResponse_getDownloadPath")
	@ResponseBody
	public String downloadAttachment(String proveMaterialId){
		String json = JsonConvertor.obj2JSON(attachmentService.getAttachmentPath(proveMaterialId));
		return json;
	}
	
	/**
	 * 删除附件
	 * @param id
	 * @return
	 */
	@RequestMapping("feedResponse_fileDelete")
	@ResponseBody
	public boolean deleteAttachment(String attachmentId, String responseItemId){
		boolean result = false;
		try{
			String path =  attachmentService.getAttachmentPath(attachmentId);
			result = feedbackResponseService.deleteProveMaterial(responseItemId,attachmentId);
			FileOperate.delete(path);
		}catch(Exception e){
			return false;
		}
		return result;
	}
	
	/**
	 * 下载修改的数据
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 */
	@RequestMapping("feedResponse_downloadFeedbackResponse")
	@ResponseBody
	public String unitdespResponse_downloadFeedbackResponse(
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
		return feedbackResponseService.unitdespResponse_downloadFeedbackResponse(feedbackResponse,realPath);
		
	}
	
	
}
