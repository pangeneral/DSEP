package com.dsep.controller.publicity.publicity;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dsep.controller.base.JqGridBaseController;
import com.dsep.service.dsepmeta.dsepmetas.DMBackupService;
import com.dsep.service.publicity.objection.OriginalObjectionService;
import com.dsep.service.publicity.process.production.publicity.PublicityProcess;
import com.dsep.util.JsonConvertor;
import com.dsep.util.UserSession;
import com.dsep.vm.JqgridVM;

@Controller
@RequestMapping("/publicity/viewPub/CollectData")
public class PubJqDataController extends JqGridBaseController{
	@Resource(name="backupService")
	private DMBackupService backupService;
	
	@Resource(name="originalObjectionService")
	private OriginalObjectionService originalObjectionService;
	
	@RequestMapping("collectionData/{entityId}/{versionId}/{catId}")
	@ResponseBody
	public String collectionDataByCatId(@PathVariable(value="entityId")String entityId,
			@PathVariable(value="versionId")String versionId,
			@PathVariable(value="catId")String catId,HttpServletRequest request){
		setRequestParams(request);
		JqgridVM jqgridVM = backupService.getJqGridDataByCatId(entityId, catId, versionId,
				getSearchGroup(), getPageIndex(), getPageSize(), getSidx(), isAsc());
		return JsonConvertor.obj2JSON(jqgridVM);
	}
	
	@RequestMapping("collectionData/{entityId}/{versionId}/{unitId}/{discId}")
	@ResponseBody
	public String collectionDataByDiscId(@PathVariable(value="entityId")String entityId,
			@PathVariable(value="versionId")String versionId,
			@PathVariable(value="unitId")String unitId,
			@PathVariable(value="discId")String discId,HttpServletRequest request,
			HttpSession session) throws IllegalArgumentException, IllegalAccessException{
		setRequestParams(request);
		JqgridVM jqgridVM = backupService.getJqGridData(entityId, unitId, discId, versionId, getSearchGroup(),getPageIndex(),getPageSize(),getSidx(), isAsc());
		UserSession us = new UserSession(session);
		String currentRoundId = request.getParameter("currentRoundId");
		PublicityProcess process = us.getDsepProcessFactory().getPublicityProcess();
		process.processJqgridVM(jqgridVM, currentRoundId, originalObjectionService);
		return JsonConvertor.obj2JSON(jqgridVM);
	}
	
	
	
}
