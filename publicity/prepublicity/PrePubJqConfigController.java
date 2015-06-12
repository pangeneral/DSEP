package com.dsep.controller.publicity.prepublicity;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dsep.common.logger.LoggerTool;
import com.dsep.domain.dsepmeta.viewconfig.ViewConfig;
import com.dsep.service.dsepmeta.dsepmetas.DMViewConfigService;
import com.dsep.util.JsonConvertor;

@Controller
@RequestMapping("publicity/prepub/JqConfig")
public class PrePubJqConfigController {
	
	@Resource(name="loggerTool")
	private LoggerTool loggerTool;	
	@Resource(name="dmViewConfigService")
	private DMViewConfigService viewConfigService;
	
	@RequestMapping("/initPrePubJqgrid/{entityId}")
	@ResponseBody
	public String initPrePubJqgrid(@PathVariable(value="entityId")String entityId){
		loggerTool.warn("初始化Jqgrid!");
		/*if("C201304".equals(entityId)){
			return "{\"message\":\"checkEditor\"}";
		}*/
		ViewConfig viewConfig = viewConfigService.getViewConfig(entityId);
		String configData=JsonConvertor.obj2JSON(viewConfig);
		return configData;
	}
}
