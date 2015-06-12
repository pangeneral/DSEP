package com.dsep.controller.publicity.prepublicity;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dsep.service.publicity.prepublic.PrePublicityService;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

@Controller
@RequestMapping("publicity/prepub/JqOper")
public class PrePubJqOperController {
	
	@Resource(name="prePublicityService")
	private PrePublicityService prePublicityService;
	
	@RequestMapping("/backupEdit/{entityId}/{primaryKey}/{versionId}/{unitId}" +
			"/{discId}/{seqNo}")
	@ResponseBody
	public String backupEdit(@PathVariable(value="entityId")String entityId,
			@PathVariable(value="primaryKey")String primaryKey,
			@PathVariable(value="versionId")String versionId,
			@PathVariable(value="unitId")String unitId,
			@PathVariable(value="discId")String discId,
			@PathVariable(value="seqNo")String seqNo,HttpServletRequest request){
		String oper=request.getParameter("oper");
		if("del".equals(oper)){
			if(prePublicityService.deleteBackData(entityId, versionId, primaryKey, seqNo, unitId, discId)){
				return "success";
			}else{
				return "error";
			}
		}
		return "error";	
	}
	
	@RequestMapping("unprepub_wholeEntity")
	@ResponseBody
	public boolean unprepubWholeEntity(HttpServletRequest request){
		String entityId = request.getParameter("entityId");
		String versionId = request.getParameter("versionId");
		return prePublicityService.notPubBackData(entityId, versionId,"");
	}

	@RequestMapping("prepub_wholeEntity")
	@ResponseBody
	public boolean prepubWholeEntity(HttpServletRequest request){
		String entityId = request.getParameter("entityId");
		String versionId = request.getParameter("versionId");
		return prePublicityService.pubBackData(entityId, versionId,"");
	}
	
	/**
	 * 不公示某条数据项
	 * @param request
	 * @return
	 */
	@RequestMapping("unprepub_data")
	@ResponseBody
	public boolean unprepubData(HttpServletRequest request){
		String entityId = request.getParameter("entityId");
		String versionId = request.getParameter("versionId");
		String pkValue = request.getParameter("pkValue");
		return prePublicityService.notPubBackData(entityId, versionId,pkValue);
	}
	
	/**
	 * 公示某条数据项
	 * @param request
	 * @return
	 */
	@RequestMapping("prepub_data")
	@ResponseBody
	public boolean prepubData(HttpServletRequest request){
		String entityId = request.getParameter("entityId");
		String versionId = request.getParameter("versionId");
		String pkValue = request.getParameter("pkValue");
		return prePublicityService.pubBackData(entityId, versionId,pkValue);
	}
	
	/**
	 * 不公示选中的数据项
	 * @param request
	 * @return
	 */
	@RequestMapping("unprepub_dataList")
	@ResponseBody
	public boolean unprepubDataList(HttpServletRequest request){
		String entityId = request.getParameter("entityId");
		String versionId = request.getParameter("versionId");
		List<String> pkValueList = Arrays.asList(request.getParameter("idList").split(","));
		return prePublicityService.notPubBackDataList(entityId, versionId,pkValueList);
	}
	
	/**
	 * 公示选中的数据项
	 * @param request
	 * @return
	 */
	@RequestMapping("prepub_dataList")
	@ResponseBody
	public boolean prepubDataList(HttpServletRequest request){
		String entityId = request.getParameter("entityId");
		String versionId = request.getParameter("versionId");
		List<String> pkValueList = Arrays.asList(request.getParameter("idList").split(","));
		return prePublicityService.pubBackDataList(entityId, versionId,pkValueList);
	}
	
}
