package com.dsep.controller.publicity.publicity;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dsep.service.publicity.objection.PublicityService;
import com.dsep.util.JsonConvertor;
import com.dsep.vm.PageVM;
import com.dsep.vm.publicity.PublicityManagementVM;

@Controller
@RequestMapping("publicity")
public class PublicityManagementController {
	
	@Resource(name="publicityService")
	private PublicityService publicityService;
	
	@RequestMapping("viewPublicityManagement")
	public String viewPublicityManagement(){
		return "PublicAndFeedback/Publicity/center_publicity_management";
	}
	
	/**
	 * 获取所有的公示轮次集合
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("viewPublicityManagement_getAllPublicityRound")
	@ResponseBody
	public String getPublicityRound(HttpServletRequest request) throws IllegalArgumentException, IllegalAccessException{
		String sord = request.getParameter("sord");
		String orderName = request.getParameter("sidx");
		
		boolean order_flag = false;
		if ("desc".equals(sord)) {
			order_flag = true;
		}
		PageVM<PublicityManagementVM> vm = publicityService.getAllPublicityRoundList(orderName,order_flag);
	    return JsonConvertor.obj2JSON(vm.getGridData());
	}
	
	@RequestMapping("viewPublicityManagement_editPublicity")
	@ResponseBody
	public boolean editPublicityManagement(PublicityManagementVM editRound) throws NoSuchFieldException, SecurityException{
		return publicityService.editPublicity(editRound.getRoundId(), editRound.getRemark());
	}
	
	/**
	 * 关闭当前的公示轮次
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("viewPublicityManagement_closeCurrentRound")
	@ResponseBody
	public boolean closeCurrentPublicityRound() throws IllegalArgumentException, IllegalAccessException{
		return publicityService.publicityFinish();
	}
	
	/**
	 * 重新开启最近关闭公示
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("viewPublicityManagement_reopenCurrentRound")
	@ResponseBody
	public boolean reopenCurrentPublicityRound() throws IllegalArgumentException, IllegalAccessException{
		return publicityService.reopenRecentRound();
	}
	
	
}
