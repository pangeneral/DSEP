package com.dsep.controller.publicity.prepublicity;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dsep.common.logger.LoggerTool;
import com.dsep.service.dsepmeta.dsepmetas.DMCollectService;
import com.dsep.util.JsonConvertor;

@Controller
@RequestMapping("publicity/prepub/treeConfig")
public class PrePubTreeConfigController {
	@Resource(name="loggerTool")
	private LoggerTool loggerTool;
	@Resource(name="collectService")
	private DMCollectService collectService;
	
	@RequestMapping("initPrePubTree/{discCategory}/{publicityRoundId}")
	@ResponseBody
	public String initPrePubTree(@PathVariable(value="discCategory")String discCategory,
			@PathVariable(value="publicityRoundId")String publicityRoundId){
		return JsonConvertor.obj2JSON(collectService.getCollectionTreeInPublicity(publicityRoundId, discCategory));
	}
}
