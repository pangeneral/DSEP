package com.dsep.controller.publicity.feedback;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dsep.service.publicity.feedback.FeedbackManagementService;
import com.dsep.util.JsonConvertor;
import com.dsep.vm.PageVM;
import com.dsep.vm.feedback.FeedbackManagementVM;


@Controller
@RequestMapping("feedback")
public class FeedbackManagementController {
	@Resource(name="feedbackManagementService")
	private FeedbackManagementService feedbackManagementService;
	
	@RequestMapping("feedbackManagement")
	public String feedbackManagement(){
		return "PublicAndFeedback/FeedBack/center_feedback_management";
	}
	
	/**
	 * 获取所有的反馈批次集合
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("feedbackManagement_getAllFeedbackRound")
	@ResponseBody
	public String getFeedbackRound(HttpServletRequest request) throws IllegalArgumentException, IllegalAccessException{
		String sord = request.getParameter("sord");
		String orderName = request.getParameter("sidx");
		
		boolean order_flag = false;
		if ("desc".equals(sord)) {
			order_flag = true;
		}
		PageVM<FeedbackManagementVM> vm = feedbackManagementService.getAllFeedbackRoundList(orderName,order_flag);
	    return JsonConvertor.obj2JSON(vm.getGridData());
	}
	
	@RequestMapping("feedbackManagement_editFeedback")
	@ResponseBody
	public boolean editFeedbackManagement(FeedbackManagementVM editRound) throws NoSuchFieldException, SecurityException{
		return feedbackManagementService.editFeedback(editRound.getRoundId(), editRound.getRemark());
	}
	
	/**
	 * 关闭当前的反馈批次
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("feedbackManagement_closeCurrentRound")
	@ResponseBody
	public boolean closeCurrentFeedbackRound() throws IllegalArgumentException, IllegalAccessException{
		return feedbackManagementService.feedbackFinish();
	}
}
