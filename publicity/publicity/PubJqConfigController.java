package com.dsep.controller.publicity.publicity;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dsep.service.dsepmeta.dsepmetas.DMViewConfigService;
import com.dsep.util.JsonConvertor;

@Controller
@RequestMapping("/publicity/viewPub")
public class PubJqConfigController {
	
	@Resource(name="dmViewConfigService")
	private DMViewConfigService viewConfigService;
	
	@RequestMapping("/initPubJqgrid/{entityId}")
	@ResponseBody
	public String initPubJqgrid(@PathVariable(value="entityId")String entityId){
		return JsonConvertor.obj2JSON(viewConfigService.getViewConfig(entityId));
	}
}
