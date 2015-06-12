package com.dsep.controller.publicity.feedback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dsep.entity.User;
import com.dsep.entity.dsepmeta.DataModifyHistory;
import com.dsep.entity.dsepmeta.FeedbackResponse;
import com.dsep.entity.enumeration.EnumModule;
import com.dsep.entity.enumeration.datamodify.ModifyType;
import com.dsep.entity.enumeration.feedback.ResponseType;
import com.dsep.service.base.AttachmentService;
import com.dsep.service.base.DisciplineService;
import com.dsep.service.base.UnitService;
import com.dsep.service.datamodify.DataModifyService;
import com.dsep.service.dsepmeta.dsepmetas.DMBackupService;
import com.dsep.service.dsepmeta.dsepmetas.DMCollectService;
import com.dsep.service.dsepmeta.dsepmetas.DMViewConfigService;
import com.dsep.service.publicity.feedback.FeedbackResponseService;
import com.dsep.service.publicity.process.production.feedback.FeedbackProcess;
import com.dsep.util.JsonConvertor;
import com.dsep.util.StringProcess;
import com.dsep.util.UserSession;
import com.dsep.vm.PageVM;
import com.dsep.vm.feedback.DataModifyHistoryVM;

@Controller
@RequestMapping("feedback")
public class ModifyHistoryController extends FeedbackController{
	
	@Resource(name="disciplineService")
	private DisciplineService disciplineService;
	
	@Resource(name="feedbackResponseService")
	private FeedbackResponseService feedbackResponseService;
	
	@Resource(name="unitService")
	private UnitService unitService;
	
	@Resource(name="dataModifyService")
	private DataModifyService dataModifyService;
	
	@Resource(name="dmViewConfigService")
	private DMViewConfigService dmViewConfigService;
	
	@Resource(name="backupService")
	private DMBackupService backupService;
	
	@Resource(name="collectService")
	private DMCollectService collectService;
	
	
	@Resource(name = "attachmentService")
	private AttachmentService attachmentService;

	
	@RequestMapping("modifyHistory")
	public String modifyHistory(Model model,HttpSession session){
		UserSession userSession = new UserSession(session);
		User user = userSession.getCurrentUser();
		//获取异议处理类
		FeedbackProcess process = userSession.getDsepProcessFactory().getFeedbackProcess();
		EnumModule module = new ModifyType();
		
		Map<String,String> joinDiscMap = process.getDiscMap(disciplineService);
		Map<String,String> joinUnitMap = process.getUnitMap(unitService);
		Map<String,String> modifyTypeMap = module.getEnumMap();
		model.addAttribute("joinDisciplineMap", joinDiscMap);
		model.addAttribute("joinUnitMap", joinUnitMap);
		model.addAttribute("modifyTypeMap",modifyTypeMap);
		model.addAttribute("user",user);
		return "PublicAndFeedback/DataModify/modify_history";
	}
	
	
	@RequestMapping("modifyHistory_getModifyHistory")
	@ResponseBody
	public String getModifyHistory(HttpServletRequest request) throws IllegalArgumentException, IllegalAccessException{
		
		String sord = request.getParameter("sord");
		String orderName = request.getParameter("sidx");
		int pageIndex = Integer.parseInt(request.getParameter("page"));
		int pageSize = Integer.parseInt(request.getParameter("rows"));
		boolean order_flag = false;
		if ("desc".equals(sord)) {
			order_flag = true;
		}
		
		DataModifyHistory queryHistory = new DataModifyHistory();
		String modifyDiscId = request.getParameter("modifyDiscId");
		String modifyUnitId = request.getParameter("modifyUnitId");
		String modifyType = request.getParameter("modifyType");
		
		queryHistory.setModifyType(modifyType);
		
		if( !StringProcess.isNull(modifyDiscId) ){
			queryHistory.setDiscId(modifyDiscId);
		}
		if( !StringProcess.isNull(modifyUnitId) ){
			queryHistory.setUnitId(modifyUnitId);
		}
		
		PageVM<DataModifyHistoryVM> historyVM = dataModifyService.getDataModifyHistoryVM(pageIndex, pageSize, order_flag, orderName, queryHistory);
		return JsonConvertor.obj2JSON(historyVM.getGridData());
	}
	
	/**
	 * 获取本地数据配置信息
	 */
	@RequestMapping("modifyHistory_getViewConfig")
	@ResponseBody
	public String getCollectDataConfig(@RequestParam(value = "entityId") String entityId) {
		return super.getCollectDataConfig(dmViewConfigService, entityId);
	}
	
	/**
	 * 获取证明材料的下载路径
	 * @param id
	 * @return
	 */
	@RequestMapping("modifyHistory_getDownloadPath")
	@ResponseBody
	public String downloadAttachment(String proveMaterialId){
		String json = JsonConvertor.obj2JSON(attachmentService.getAttachmentPath(proveMaterialId));
		return json;
	}
	
	/**
	 * 获取反馈项对应的采集项数据
	 * @return
	 */
	@RequestMapping("modifyHistory_getCollectData")
	@ResponseBody
	public String pubDataDetail(HttpServletRequest request,
			HttpServletResponse response,String entityId,String backupVersionId){
		List<String> itemIds = new ArrayList<String>();
		itemIds.addAll(Arrays.asList(request.getParameter("itemIds")));
		if( itemIds.size() == 0 || itemIds.get(0) == null){
			String itemId = request.getParameter("itemId");
			itemIds.add(itemId);
		}
		return super.pubDataDetail(request, response, entityId, itemIds, backupVersionId,backupService, collectService);
	}
	
	@RequestMapping("modifyHistory_modifyDownload")
	@ResponseBody
	public String modifyHistory_modifyDownload(HttpServletRequest request, HttpServletResponse response,HttpSession session){
		String realPath = request.getSession().getServletContext().getRealPath("/");
		String discipline = request.getParameter("discipline");
		String unit = request.getParameter("unit");
		String modifyType = request.getParameter("modifyType");
		DataModifyHistory dataModifyHistory = new DataModifyHistory();
		if(discipline.equals("") && unit.equals("") == false){
			dataModifyHistory.setUnitId(unit);
			dataModifyHistory.setModifyType(modifyType);
		}
		else if(unit.equals("") && discipline.equals("") == false){
			dataModifyHistory.setDiscId(discipline);
			dataModifyHistory.setModifyType(modifyType);
		}
		else if(discipline.equals("") && unit.equals("")){
			dataModifyHistory.setModifyType(modifyType);}
		else{
			dataModifyHistory.setUnitId(unit);
			dataModifyHistory.setDiscId(discipline);
			dataModifyHistory.setModifyType(modifyType);
		}
		return dataModifyService.modifyHistory_modifyDownload(dataModifyHistory,realPath);
	}
	
	@RequestMapping("modifyHistory_deleteDownload")
	@ResponseBody
	public String modifyHistory_deleteDownload(HttpServletRequest request, HttpServletResponse response,HttpSession session){
		String realPath = request.getSession().getServletContext().getRealPath("/");
		String discipline = request.getParameter("discipline");
		String unit = request.getParameter("unit");
		String modifyType = request.getParameter("modifyType");
		DataModifyHistory dataModifyHistory = new DataModifyHistory();
		if(discipline.equals("") && unit.equals("") == false){
			dataModifyHistory.setUnitId(unit);
			dataModifyHistory.setModifyType(modifyType);
		}
		else if(unit.equals("") && discipline.equals("") == false){
			dataModifyHistory.setDiscId(discipline);
			dataModifyHistory.setModifyType(modifyType);
		}
		else if(discipline.equals("") && unit.equals("")){
			dataModifyHistory.setModifyType(modifyType);}
		else{
			dataModifyHistory.setUnitId(unit);
			dataModifyHistory.setDiscId(discipline);
			dataModifyHistory.setModifyType(modifyType);
		}
		return dataModifyService.modifyHistory_deleteDownload(dataModifyHistory,realPath);
	}
	
}
