package com.dsep.controller.publicity.publicity;


import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.dsep.common.logger.LoggerTool;
import com.dsep.domain.AttachmentHelper;
import com.dsep.domain.dsepmeta.publicity.PublicityMessage;
import com.dsep.entity.User;
import com.dsep.entity.dsepmeta.OriginalObjection;
import com.dsep.entity.enumeration.AttachmentType;
import com.dsep.service.base.AttachmentService;
import com.dsep.service.base.DiscCategoryService;
import com.dsep.service.base.DisciplineService;
import com.dsep.service.base.UnitService;
import com.dsep.service.publicity.objection.OriginalObjectionService;
import com.dsep.service.publicity.objection.PublicityService;
import com.dsep.service.publicity.process.production.publicity.PublicityProcess;
import com.dsep.util.FileOperate;
import com.dsep.util.JsonConvertor;
import com.dsep.util.MySessionContext;
import com.dsep.util.StringProcess;
import com.dsep.util.UnitTest;
import com.dsep.util.UserSession;

@Controller
@RequestMapping("publicity")
public class ViewPublicityController {
	
	@Resource(name="publicityService")
	private PublicityService publicityService;
	
	@Resource(name="discCategoryService")
	private DiscCategoryService discCategoryService;
	
	@Resource(name="disciplineService")
	private DisciplineService disciplineService;
	
	@Resource(name="originalObjectionService")
	private OriginalObjectionService originalObjectionService;
	
	@Resource(name="unitService")
	private UnitService unitService;
	
	@Resource(name = "attachmentService")
	private AttachmentService attachmentService;
	
	@Resource(name="loggerTool")
	private LoggerTool loggerTool;
	
	/**
	 * 用户点击学科列表框时获取学校列表框里能够显示的学校集合
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping("viewPub_changeDiscipline")
	@ResponseBody
	public String getUnitMap(HttpServletRequest request,HttpSession session){
		UserSession us = new UserSession(session);
		PublicityProcess process = us.getDsepProcessFactory().getPublicityProcess();
		String discId = request.getParameter("discId");
		return JsonConvertor.mapJSON(process.changeDiscipline(discId,unitService));
	}
	
	/**
	 * 用户点击学校列表框时获取学科列表框里能够显示的学校集合
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping("viewPub_changeUnit")
	@ResponseBody
	public String getDiscMap(HttpServletRequest request,HttpSession session){
		UserSession us = new UserSession(session);
		PublicityProcess process = us.getDsepProcessFactory().getPublicityProcess();
		String unitId = request.getParameter("unitId");
	    Map<String,String> result = process.changeUnit(unitId,this.disciplineService);
	    loggerTool.debug(result);
	    return JsonConvertor.mapJSON(result);
	    /*return result;*/
	}
	
	/**
	 * 重置学科下拉框
	 * @param session
	 * @return
	 */
	@RequestMapping("viewPub_resetDisc")
	@ResponseBody
	public String resetDiscMap(HttpSession session,Model model){
		UserSession us = new UserSession(session);
		PublicityProcess process = us.getDsepProcessFactory().getPublicityProcess();
		Map<String,String> discMap = process.getDiscMap(disciplineService);
		model.addAttribute("joinDisciplineMap", discMap);
		for(Map.Entry<String, String> entrySet:discMap.entrySet()){
			System.out.println(entrySet.getKey());
		}
		return JsonConvertor.mapJSON(discMap);
	}
	
	/**
	 * 重置学校下拉框
	 * @param session
	 * @return
	 */
	@RequestMapping("viewPub_resetUnit")
	@ResponseBody
	public String resetUnitMap(HttpSession session){
		UserSession us = new UserSession(session);
		PublicityProcess process = us.getDsepProcessFactory().getPublicityProcess();
		return JsonConvertor.mapJSON(process.getUnitMap(unitService));
	}
	
	/**
	 * 进入公示设置页面，设置要查看的单位
	 * @param model
	 * @param session
	 * @return
	 */
	@RequestMapping("viewPub_unitSet")
	public String setPublicity(Model model,HttpSession session){
		UserSession us = new UserSession(session);
		User user = us.getCurrentUser();
		model.addAttribute("user",user);
		PublicityProcess process = us.getDsepProcessFactory().getPublicityProcess();
		Map<String,String> discMap = process.getDiscMap(disciplineService);
		Map<String,String> unitMap = process.getUnitMap(unitService);
		model.addAttribute("joinUnitMap", JsonConvertor.mapJSON(unitMap));
		model.addAttribute("joinDisciplineMap", JsonConvertor.mapJSON(discMap));
		return "PublicAndFeedback/Publicity/unit_discipline_set";
	}
	
	/**
	 * 进入公示查看页面，查看公示数据
	 * @param request
	 * @param model
	 * @param session
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("viewPub")
	public String viewPublicity(HttpServletRequest request,Model model,HttpSession session) throws IllegalArgumentException, IllegalAccessException
	{
		UserSession us = new UserSession(session);
		User user = us.getCurrentUser();
		Map<String,String> publicityRoundMap = publicityService.getAllPublicityRound();//公示批次Map		Map<String,String> publicityRoundMap = new HashMap<String,String>();
		if( !StringProcess.isNull(request.getParameter("unitId")) ){
			model.addAttribute("choosenUnitId",request.getParameter("unitId"));
		}
		else{
			model.addAttribute("choosenUnitId","");
		}
		if( !StringProcess.isNull(request.getParameter("discId")) ){
			model.addAttribute("choosenDisciplineId",request.getParameter("discId"));
		}
		else{
			model.addAttribute("choosenDisciplineId","");
		}
		model.addAttribute("publicityRoundMap", publicityRoundMap);
		model.addAttribute("user",user);
		return "PublicAndFeedback/Publicity/view_publicity";
	}
	
	/**
	 * 从公示查看页面设置查看单位
	 * @param model
	 * @param session
	 * @param request
	 * @return
	 */
	@RequestMapping("viewPub_setUnit")
	public String returnSetPublicity(Model model,HttpSession session,HttpServletRequest request){
		String formerViewUnitId = request.getParameter("formerViewUnitId");
		String formerViewDiscId = request.getParameter("formerViewDisciplineId");
		UserSession us = new UserSession(session);
		User user = us.getCurrentUser();
		model.addAttribute("user",user);
		PublicityProcess process = us.getDsepProcessFactory().getPublicityProcess();
		Map<String,String> discMap = process.getDiscMap(disciplineService);
		Map<String,String> unitMap = process.getUnitMap(unitService);
		if( formerViewUnitId != null && formerViewUnitId != ""  ){
			model.addAttribute("formerViewUnitId", formerViewUnitId);
		}
		if( formerViewDiscId != null && formerViewDiscId != ""){
			model.addAttribute("formerViewDisciplineId", formerViewDiscId);
		}
		model.addAttribute("joinUnitMap", JsonConvertor.mapJSON(unitMap));
		model.addAttribute("joinDisciplineMap", JsonConvertor.mapJSON(discMap));
		return "PublicAndFeedback/Publicity/unit_discipline_set";
	}
	
	
	
	
	/**
	 * 获取某一公示批次的相关信息，如开启时间，结束时间等
	 * @param request
	 * @return
	 */
	@RequestMapping("viewPub_getPublicityMessage")
	@ResponseBody
	public String getPublicityMessage(HttpServletRequest request){
		String id = request.getParameter("publicity_round_id");
		PublicityMessage pubMessage = publicityService.getPublicityMessage(id);
		String jsonData = JsonConvertor.obj2JSON(pubMessage);
		UnitTest.testPrint(jsonData);
		return jsonData;
	}
	
	/**
	 * 根据实体获取该实体对应的所有异议类型，每一个异议类型对应一个字段
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping("viewPub_getObjectType")
	@ResponseBody
	public Map<String,String> getObjectType(HttpServletRequest request){
		String entityId = request.getParameter("entityId");
		return originalObjectionService.getObjectTypeByEntityId(entityId);
	}
	
	/**
	 * 上传证明材料
	 * @param file
	 * @return
	 */
	@RequestMapping("viewPub_uploadProofFile")
	@ResponseBody
	public String updateProofFile(@RequestParam("file") MultipartFile file,HttpSession session){
		UserSession us = new UserSession(session);
		PublicityProcess process = us.getDsepProcessFactory().getPublicityProcess();
		String path = process.uploadFile(file);
		return JsonConvertor.obj2JSON(path);
	}
	
	/**
	 * 对学科整体提出异议
	 * @return
	 */
	@RequestMapping("viewPub_objectDiscipline")
	public String objectDiscipline(Model model){
		model.addAttribute("objectType", "discipline");
		return "PublicAndFeedback/Publicity/objection_discipline_dialog";
	}
	
	/**
	 * 提出异议
	 * @param dataString
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("viewPub_addOriginalObjection")
	@ResponseBody
	public boolean addOriginalObjection(OriginalObjection dataString,
			@RequestParam String proveMaterialId) throws Exception{
		boolean result = originalObjectionService.addNewObjection(dataString,proveMaterialId);
		return result;
	}
	
	
	/**
	 * 上传证明材料
	 * @param file
	 * @return
	 */
	@RequestMapping("viewPub_uploadFile")
	@ResponseBody
	public String uploadAttachment(HttpServletRequest request, String jsessionid){
		MySessionContext myc= MySessionContext.getInstance();
		HttpSession session = myc.getSession(jsessionid);
		
		UserSession userSession = new UserSession(session);
		User user = userSession.getCurrentUser();
		MultipartFile file = FileOperate.getFile(request);
		AttachmentHelper ah = attachmentService.getAttachmentHelper(file, user.getId(), AttachmentType.PUBLICITY, user.getUnitId());
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
	@RequestMapping("viewPub_getDownloadPath")
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
	@RequestMapping("viewPub_fileDelete")
	@ResponseBody
	public boolean deleteAttachment(HttpServletRequest request){
		boolean result = false;
		try{
			/*String objectionItemId = request.getParameter("objectionItemId");*/
			String proveMaterialId = request.getParameter("attachmentId");
			String path =  attachmentService.getAttachmentPath(proveMaterialId);
			/*result = originalObjectionService.deleteProveMaterial(objectionItemId,proveMaterialId);*/
			FileOperate.delete(path);
			result = true;
		}catch(Exception e){
			return false;
		}
		return result;
	}
}
