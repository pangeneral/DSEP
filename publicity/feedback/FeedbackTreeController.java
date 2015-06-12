package com.dsep.controller.publicity.feedback;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dsep.service.publicity.feedback.FeedbackManagementService;
import com.dsep.util.JsonConvertor;


@Controller
@RequestMapping("feedback")
public class FeedbackTreeController {

	@Resource(name="feedbackManagementService")
	private FeedbackManagementService feedbackManagementService;
	
	
	
}
