package com.dsep.controller.publicity.publicity;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dsep.common.logger.LoggerTool;
import com.dsep.service.dsepmeta.dsepmetas.DMCollectService;
import com.dsep.util.JsonConvertor;

@Controller
@RequestMapping("/publicity/viewPub")
public class PubTreeConfigController {
	
	@Resource(name="loggerTool")
	private LoggerTool loggerTool;
	@Resource(name="collectService")
	private DMCollectService collectService;
	
	@RequestMapping("/initPubTreeByCatId/{catId}")
	@ResponseBody
	public String initPubTreeByCatId(@PathVariable(value="catId")String catId){
		return JsonConvertor.obj2JSON(collectService.getDisciplineCollectTreesByCatId(catId));
	}
	
	
	@RequestMapping("/initPubTreeByDiscId/{discId}/{publicityRoundId}")
	@ResponseBody
	public String initPubTreeByDiscId(@PathVariable(value="discId")String discId,
		@PathVariable(value="publicityRoundId") String publicityRoundId){
		String result = JsonConvertor.obj2JSON(collectService.getDisciplineCollectTreesInPublicity(publicityRoundId, discId)); 
		return result;
	}
}
