package com.dsep.controller.publicity.prepublicity;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dsep.controller.base.JqGridBaseController;
import com.dsep.service.dsepmeta.dsepmetas.DMBackupService;
import com.dsep.util.JsonConvertor;
import com.dsep.vm.JqgridVM;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

@Controller
@RequestMapping("/publicity/prepub/CollectData")
public class CenterPrePubDataController extends JqGridBaseController{
	
	@Resource(name="backupService")
	private DMBackupService backupService;
	
	@RequestMapping("collectionData/{entityId}/{versionId}/{catId}")
	@ResponseBody
	public String collectionData(@PathVariable(value="entityId")String entityId,
			@PathVariable(value="versionId")String versionId,
			@PathVariable(value="catId")String catId,HttpServletRequest request){
		setRequestParams(request);
		JqgridVM jqgridVM = backupService.getJqGridDataByCatId(entityId, catId, versionId,
				getSearchGroup(), getPageIndex(), getPageSize(), getSidx(), isAsc());
		return JsonConvertor.obj2JSON(jqgridVM);
	}
	
	
}
