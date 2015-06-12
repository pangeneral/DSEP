package com.dsep.controller.publicity.publicity;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.dsep.domain.dsepmeta.viewconfig.ViewConfig;
import com.dsep.entity.User;
import com.dsep.entity.dsepmeta.OriginalObjection;
import com.dsep.entity.enumeration.EnumModule;
import com.dsep.entity.enumeration.publicity.MaterialType;
import com.dsep.service.base.AttachmentService;
import com.dsep.service.base.DisciplineService;
import com.dsep.service.base.UnitService;
import com.dsep.service.dsepmeta.dsepmetas.DMBackupService;
import com.dsep.service.dsepmeta.dsepmetas.DMCollectService;
import com.dsep.service.dsepmeta.dsepmetas.DMViewConfigService;
import com.dsep.service.publicity.objection.OriginalObjectionService;
import com.dsep.service.publicity.objection.PublicityService;
import com.dsep.service.publicity.process.production.objection.ObjectionProcess;
import com.dsep.util.JsonConvertor;
import com.dsep.util.UserSession;
import com.dsep.vm.JqgridVM;
import com.dsep.vm.PageVM;
import com.dsep.vm.publicity.OriginalObjectionVM;

/**
 * 
 * @author jc 这个Controller里面用如下路由, 并且采用ConventionOverConfiguration的方式命名,
 *         方法名、路由名和对应PublicAndFeedback文件夹下同名的jsp名相同： 1.centerPreparePublicity
 *         ->作用是学位中心预公示 2.centerViewObjection ->作用是学位中心查看公示异议并据此编写反馈
 *         3.centerViewFeedback ->作用是学位中心查看反馈信息汇总 4.
 */
@Controller
@RequestMapping("publicity")
public class ObjectionProcessController {
	

	@Resource(name = "originalObjectionService")
	private OriginalObjectionService originalObjectionService;

	@Resource(name = "publicityService")
	private PublicityService publicityService;
	
	@Resource(name="disciplineService")
	private DisciplineService disciplineService;

	@Resource(name="collectService")
	private DMCollectService collectService;

	@Resource(name="dmViewConfigService")
	private DMViewConfigService dmViewConfigService;
	
	@Resource(name="unitService")
	private UnitService unitService;
	
	@Resource(name = "attachmentService")
	private AttachmentService attachmentService;
	
	@Resource(name="backupService")
	private DMBackupService backupService;
	
	/**
	 * 进入异议汇总页面
	 * @param model
	 * @param session
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("viewObjection")
	public String viewObjection(Model model, HttpSession session)
			throws IllegalArgumentException, IllegalAccessException {
		UserSession userSession = new UserSession(session);
		User user = userSession.getCurrentUser();
		//获取异议处理类
		ObjectionProcess process = userSession.getDsepProcessFactory().getObjectionProcess();
		Map<String,String> statusMap = process.getObjectStatus();
		model.addAttribute("statusMap",statusMap);//界面上异议的状态，如：已提交、未提交等
		
		Map<String, String> initDiscsMap = process.getDisciplineMap(disciplineService);
		model.addAttribute("joinDisciplineMap", initDiscsMap);
		
		Map<String,String> initUnitMap = process.getJoinUnitMap(unitService);
		model.addAttribute("joinUnitMap",initUnitMap);
		
		Map<String, String> roundMap = publicityService.getAllPublicityRound();
		model.addAttribute("publicityRoundMap", roundMap);
		
		model.addAttribute("user",user);
		return "PublicAndFeedback/Publicity/objection_gather";
	}

	@RequestMapping("viewObjection_showAndEditObjection")
	@ResponseBody
	public String showAndEditObjection(HttpServletRequest request,	HttpServletResponse response, HttpSession session)
			throws IllegalArgumentException, IllegalAccessException {
		UserSession us = new UserSession(session);
		ObjectionProcess objectionProcess = us.getDsepProcessFactory().getObjectionProcess();

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
		String currentPublicRoundId = request.getParameter("currentRoundId");
		String status = request.getParameter("status");
		
		OriginalObjection queryObjection = new OriginalObjection();
		queryObjection.setCurrentPublicRoundId(currentPublicRoundId);
		if( problemDiscId != null && !problemDiscId.equals("")){
			queryObjection.setProblemDiscId(problemDiscId);
		}
		if( problemUnitId != null && !problemUnitId.equals("")){
			queryObjection.setProblemUnitId(problemUnitId);
		}
		
		PageVM<OriginalObjectionVM> pageVM = objectionProcess.getOriginalObjection(queryObjection,order_flag, orderName, pageIndex, pageSize, status, originalObjectionService);
		return JsonConvertor.obj2JSON(pageVM.getGridData());
	};
	
	/**
	 * 针对某条采集项，获取本单位已提出的异议
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("viewObjection_showExistedObjection")
	@ResponseBody
	public String showExistedObjectionList(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) throws IllegalArgumentException, IllegalAccessException{
		UserSession us = new UserSession(session);
		ObjectionProcess objectionProcess = us.getDsepProcessFactory().getObjectionProcess();
		OriginalObjection queryObjection = new OriginalObjection();
		
		String currentPublicRoundId = request.getParameter("currentRoundId");
		String objectCollectItemId = request.getParameter("objectCollectItemId");
		queryObjection.setCurrentPublicRoundId(currentPublicRoundId);
		queryObjection.setObjectCollectItemId(objectCollectItemId);
		
		PageVM<OriginalObjectionVM> pageVM = objectionProcess.getExistedObjectionList(queryObjection, originalObjectionService);
		return JsonConvertor.obj2JSON(pageVM.getGridData());
	}

	/**
	 * 提交所有的异议
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("viewObjection_submitAllObjection")
	@ResponseBody
	public boolean submitAllObjection(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws IllegalArgumentException, IllegalAccessException {
		UserSession us = new UserSession(session);
		User user = us.getCurrentUser();
		String currentRoundId = request.getParameter("currentRoundId");
		try {
			originalObjectionService.updateSchoolStatusToSubmit(currentRoundId,user
					.getUnitId());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	};

	/**
	 * 学校用户点击删除按钮将未提交的异议改为未通过
	 * @param request
	 * @return
	 */
	@RequestMapping("viewObjection_deleteRow")
	@ResponseBody
	public boolean deleteRowData(HttpServletRequest request,HttpSession session) {
		String objectionId = request.getParameter("objectionId");
		UserSession us = new UserSession(session);
		ObjectionProcess process = us.getDsepProcessFactory().getObjectionProcess();
		return process.deleteObjection(originalObjectionService, objectionId);
	}


	/**
	 * 保存对于异议内容的修改
	 * @param request
	 * @param response
	 * @param session
	 * @param objection
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	@RequestMapping("viewObjection_saveEditRowData")
	@ResponseBody
	public boolean saveEditRowData(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			OriginalObjectionVM objection) throws NoSuchFieldException,
			SecurityException {
		
		UserSession us = new UserSession(session);
		ObjectionProcess process = us.getDsepProcessFactory().getObjectionProcess();
		
		if( process.updateObjectionRowData(objection, originalObjectionService) > 0)
			return true;
		else
			return false;
	}
	
	/**
	 * 中心将所有未处理的异议变为已处理
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping("viewObjection_processAllObjection")
	@ResponseBody
	public boolean processAllObjection(HttpServletRequest request,
			HttpSession session){
		String publicityRoundId = request.getParameter("currentRoundId");
		return originalObjectionService.processAllObjection(publicityRoundId);
	}

	/**
	 * 导出所有已提交的异议到excel中
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 */
	@RequestMapping("viewObjection_downloadSubmitObjection")
	@ResponseBody
	public String viewObjection_downloadSubmitObjection(
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session) {
		UserSession us = new UserSession(session);
		User user = us.getCurrentUser();
		ObjectionProcess process = us.getDsepProcessFactory().getObjectionProcess();
		String realPath = request.getSession().getServletContext()
				.getRealPath("/");
		String currentRoundId = request.getParameter("currentRoundId");
		return process.downloadSubmitObjection(currentRoundId, realPath, originalObjectionService);
	}
	
	/**
	 * 导出所有已处理的异议到excel中
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 */
	@RequestMapping("viewObjection_downloadProcessObjection")
	@ResponseBody
	public String downloadProcessedObjection(HttpServletRequest request,
			HttpSession session){
		UserSession us = new UserSession(session);
		User user = us.getCurrentUser();
		ObjectionProcess process = us.getDsepProcessFactory().getObjectionProcess();
		String realPath = request.getSession().getServletContext()
				.getRealPath("/");
		String currentRoundId = request.getParameter("currentRoundId");
		return process.downloadProcessObjection(currentRoundId, realPath, originalObjectionService);
	}
	
	
	/**
	 * 获取本地数据配置信息
	 */
	@RequestMapping("viewObjection_getViewConfig")
	@ResponseBody
	public String getCollectDataConfig(@RequestParam(value = "entityId") String entityId) {

		ViewConfig viewConfig = dmViewConfigService.getViewConfig(entityId);
		
		String configData = JsonConvertor.obj2JSON(viewConfig);
		return configData;
	}
	
	/**
	 * 获取异议项对应的采集项数据
	 * @return
	 */
	@RequestMapping("viewObjection_getCollectData")
	@ResponseBody
	public String pubDataDetail(HttpServletRequest request,
			HttpServletResponse response,String backupVersionId,String entityId, String itemId){
		String sord = request.getParameter("sord");
		String sidx = request.getParameter("sidx");
		int page = Integer.parseInt(request.getParameter("page"));
		int pageSize = Integer.parseInt(request.getParameter("rows"));
		
		boolean order_flag = false;
		if ("desc".equals(sord)) {
			order_flag = true;
		}
		List<String> itemIds = new ArrayList<String>();
		itemIds.add(itemId);
		JqgridVM jqgridVM = backupService.getCollectDataDetail(entityId, itemIds, backupVersionId, sidx, order_flag, page, pageSize);
		
		/*JqgridVM jqgridVM = collectService.getCollectDataDetail(
				entityId, itemId ,sidx, order_flag, page, pageSize);*/
		
		String result = JsonConvertor.obj2JSON(jqgridVM);
		return result;
	}
	
	/**
	 * 获取证明材料的下载路径
	 * @param id
	 * @return
	 */
	@RequestMapping("viewObjection_getDownloadPath")
	@ResponseBody
	public String downloadAttachment(String proveMaterialId){
		String json = JsonConvertor.obj2JSON(attachmentService.getAttachmentPath(proveMaterialId));
		return json;
	}


	private String getError(String message) {
		JSONObject obj = new JSONObject();
		obj.put("error", 1);
		obj.put("message", message);
		return obj.toString();
	}
}
