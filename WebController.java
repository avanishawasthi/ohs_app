package in.eightfolds.pyro_safety_app.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import in.eightfolds.commons.spring_security.SecurityUtil;
import in.eightfolds.commons.spring_security.UserInfo;
import in.eightfolds.commons.util.adv.Log;
import in.eightfolds.pyro_safety_app.Constants;
import in.eightfolds.pyro_safety_app.bean.JobAuditDetailsResponse;
import in.eightfolds.pyro_safety_app.bean.JobAuditResponse;
import in.eightfolds.pyro_safety_app.bean.JobDetailQuesAnsReponse;
import in.eightfolds.pyro_safety_app.bean.JobDetailResponse;
import in.eightfolds.pyro_safety_app.bean.JobResponse;
import in.eightfolds.pyro_safety_app.bean.VendorSubCategoryResponse;
import in.eightfolds.pyro_safety_app.bean.ViolationLevel;
import in.eightfolds.pyro_safety_app.bean.entity.Doc;
import in.eightfolds.pyro_safety_app.bean.entity.Job;
import in.eightfolds.pyro_safety_app.bean.entity.JobAuditDetail;
import in.eightfolds.pyro_safety_app.bean.entity.JobDetailFile;
import in.eightfolds.pyro_safety_app.bean.entity.JobWorkHistory;
import in.eightfolds.pyro_safety_app.bean.entity.MetaActivity;
import in.eightfolds.pyro_safety_app.bean.entity.MetaCircle;
import in.eightfolds.pyro_safety_app.bean.entity.Operator;
import in.eightfolds.pyro_safety_app.bean.entity.Site;
import in.eightfolds.pyro_safety_app.bean.entity.User;
import in.eightfolds.pyro_safety_app.bean.entity.Vendor;
import in.eightfolds.pyro_safety_app.service.MainService;
import in.eightfolds.pyro_safety_app.validator.DocValidator;
import in.eightfolds.pyro_safety_app.validator.JobValidator;
import in.eightfolds.pyro_safety_app.validator.SiteValidator;
import in.eightfolds.pyro_safety_app.validator.UserValidator;

@Controller
@RequestMapping(value = "/web")
public class WebController {

	@Autowired
	private Constants constants;
	@Autowired
	private MainService mainService;
	@Autowired
	private UserValidator userValidator;
	@Autowired
    private SiteValidator siteValidator;
	@Autowired
    private DocValidator docValidator;
	@Autowired
	private JobValidator jobValidator;
	
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String home(Model model) throws Exception {
		return "redirect:/web/user/management";
	}
	
	@RequestMapping(value = "/user/management", method = RequestMethod.GET)
	public String userManagement(
			Model model, 
			@RequestParam(value = "search", required = false, defaultValue = "") String search,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "-1") int pageSize,HttpServletResponse response) throws IOException, SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (pageSize < 0) {
			pageSize = constants.getPageSize();
		}
		
		 int index=0;
		 if(page>0){
		  index=(page-1)*pageSize;
		 }

		 model.addAttribute("indexNo",index);
		UserInfo userInfo = SecurityUtil.getCurrentUserInfo();
		User user = mainService.getUserByUserId(userInfo.getUserId());
		List<User> users = mainService.getAllUsers(search,page,pageSize,user);
		model.addAttribute("users", users);
		setPagination(model, users, page);
		model.addAttribute("page", page);
		model.addAttribute("search", search);
		 return "user_management";
	}
	
	@RequestMapping(value = "/new/user", method = RequestMethod.GET)
	public String addUser(Model model) throws Exception {
		User user = new User();
		List<MetaCircle> circles = mainService.getAllCircles();
		List<Vendor> vendors = mainService.getVendors();
		List<Operator> customers = mainService.getCustomers("");
		model.addAttribute("user", user);
		model.addAttribute("circles", circles);
		model.addAttribute("vendors", vendors);
		model.addAttribute("customers", customers);
		model.addAttribute("mode", "New");
		model.addAttribute("page", 1);
		return "user";
	}
	
	@RequestMapping(value = "/user/{id}/edit/{page}", method = RequestMethod.GET)
	public String editUser(
		@PathVariable("id") Long id,
		@PathVariable("page") int page,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		Model model) throws Exception {
		
		User user = mainService.getUserByUserId(id);
		List<MetaCircle> circles = mainService.getAllCircles();
		List<Long> circleIds=mainService.getCircleIdsByUserId(id);
		List<Long> vendorIds=mainService.getVendorIdsByUserId(id);
		List<Long> customerIds=mainService.getCustomerIdsByUserId(id);
		List<Vendor> vendors = mainService.getVendors();
		List<Operator> customers = mainService.getCustomers("");
		model.addAttribute("user", user);
		model.addAttribute("circles", circles);
		model.addAttribute("circleIds", circleIds);
		model.addAttribute("vendorIds", vendorIds);
		model.addAttribute("customerIds", customerIds);
		model.addAttribute("vendors", vendors);
		model.addAttribute("customers", customers);
		model.addAttribute("mode", "Edit");
		model.addAttribute("page", page);
		model.addAttribute("search", search);
		
		return "user";
	}
	
	@RequestMapping(value = "/user/save/{page}", method = RequestMethod.POST)
	public String saveUser(
		@ModelAttribute("user") User user,
		@PathVariable("page") int page,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		@RequestParam(value = "circleIds", required = false, defaultValue = "") String circleIds,
		@RequestParam(value = "vendorIds", required = false, defaultValue = "") String vendorIds,
		@RequestParam(value = "customerIds", required = false, defaultValue = "") String customerIds,
		final RedirectAttributes redirectAttributes,BindingResult result,Model model) throws Exception {
		UserInfo userInfo = SecurityUtil.getCurrentUserInfo();
		User loggedInUser = mainService.getUserByUserId(userInfo.getUserId());
		List<MetaCircle> circles = mainService.getAllCircles();
		model.addAttribute("circles", circles);
		model.addAttribute("circleIds", circleIds);
		model.addAttribute("vendorIds", vendorIds);
		model.addAttribute("customerIds", customerIds);
		userValidator.validate(user, result);
		if (result.hasErrors()) {
			List<Vendor> vendors = mainService.getVendors();
			List<Operator> customers = mainService.getCustomers("");
			model.addAttribute("vendors", vendors);
			model.addAttribute("customers", customers);
			if(user.getUserId() == 0)
			{
				model.addAttribute("mode","New");
				
			}
			else
			{
				model.addAttribute("mode","Edit");
			}
			return "user";
		}
		if(user.getUserId() == 0){
			user.setCreatedBy(userInfo.getUserId());
		  long flag = mainService.addUser(user,loggedInUser,circleIds,vendorIds,customerIds);
			if (flag > 0) {
				redirectAttributes.addFlashAttribute("successMessage", "User added successfully.");
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "Sorry!! please try again.");
			}
		  }else{
		   long flag = mainService.updateUser(user,loggedInUser,circleIds,vendorIds,customerIds);	
			if (flag > 0) {
					redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "Sorry!! please try again.");
			}
		}
		return "redirect:/web/user/management?page=" + page+"&search="+search;
	}
	
	@RequestMapping(value = "/delete/user/{id}/{page}", method = RequestMethod.GET)
	public String deleteUser(
		@PathVariable("id") Long id,
		@PathVariable("page") int page,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		final RedirectAttributes redirectAttributes,
		Model model) throws Exception {
		
		int flag=mainService.deleteUser(id);
		if(flag>0)
		{
			redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
		}
		else
		{
			redirectAttributes.addFlashAttribute("errorMessage", "Sorry!! please try again.");
		}
		return "redirect:/web/user/management?page=" + page+"&search="+search;
	}
	
	@RequestMapping(value = "/jobs", method = RequestMethod.GET)
	public String jobs(
			Model model, @RequestParam(value = "activityTypeId", required = false, defaultValue = "") Long activityTypeId,
			@RequestParam(value = "statusId", required = false, defaultValue = "") Integer statusId,
			@RequestParam(value = "search", required = false, defaultValue = "") String search,
			@RequestParam(value = "startDate", required = false, defaultValue = "") String startDate,
			@RequestParam(value = "endDate", required = false, defaultValue = "") String endDate,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "-1") int pageSize,HttpServletResponse response) throws IOException, SQLException {
			
		if (pageSize < 0) {
			pageSize = constants.getPageSize();
		}
		
		 int index=0;
		 if(page>0){
		  index=(page-1)*pageSize;
		 }

		model.addAttribute("indexNo",index);
		UserInfo userInfo = SecurityUtil.getCurrentUserInfo();
		User user = mainService.getUserByUserId(userInfo.getUserId());
		List<JobResponse> jobs = mainService.getAllJobs(activityTypeId,statusId,user,search,startDate,endDate,page,pageSize);
		List<MetaActivity> activityTypes = mainService.getAllActivityTypes();
		model.addAttribute("jobs", jobs);
		model.addAttribute("activityTypes", activityTypes);
		setPagination(model, jobs, page);
		model.addAttribute("page", page);
		model.addAttribute("search", search);
		model.addAttribute("activityTypeId", activityTypeId);
		model.addAttribute("statusId", statusId);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		return "jobs";
	}
	
	@RequestMapping(value = "/new/job", method = RequestMethod.GET)
	public String addJob(Model model) throws Exception {
		Job job = new Job();
		UserInfo userInfo = SecurityUtil.getCurrentUserInfo();
		User user = mainService.getUserByUserId(userInfo.getUserId());
		List<MetaActivity> activities = mainService.getActivityTypes(userInfo.getUserId());
		List<Site> sites = mainService.getSites("");
		List<Vendor> vendors = mainService.getUserVendors(userInfo.getUserId());
		List<Operator> customers = mainService.getUserCustomers(userInfo.getUserId());
		List<User> engineers = mainService.getEngineers("");
		List<User> coordinators = mainService.getCoordinators();
		model.addAttribute("job", job);
		model.addAttribute("activities", activities);
		model.addAttribute("sites", sites);
		model.addAttribute("vendors", vendors);
		model.addAttribute("customers", customers);
		model.addAttribute("engineers", engineers);
		model.addAttribute("coordinators", coordinators);
		model.addAttribute("mode", "New");
		model.addAttribute("page", 1);
		if(user.getAuthority().equals(MainService.ROLE_COORDINATOR)) {
			model.addAttribute("coordinatorId", user.getUserId());
		}
		return "job";
	}
	
	@RequestMapping(value = "/job/{jobId}/edit/{page}", method = RequestMethod.GET)
	public String editJob(
		@PathVariable("jobId") Long jobId,
		@PathVariable("page") int page,
		@RequestParam(value = "activityTypeId", required = false, defaultValue = "") Long activityTypeId,
		@RequestParam(value = "statusId", required = false, defaultValue = "") Integer statusId,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		@RequestParam(value = "startDate", required = false, defaultValue = "") String startDate,
		@RequestParam(value = "endDate", required = false, defaultValue = "") String endDate,
		Model model) throws Exception {
		
		Job job = mainService.getJobByJobId(jobId);
		UserInfo userInfo = SecurityUtil.getCurrentUserInfo();
		User user = mainService.getUserByUserId(userInfo.getUserId());
		List<MetaActivity> activities = mainService.getActivityTypes(userInfo.getUserId());
		List<Site> sites = mainService.getSites("");
		List<Vendor> vendors = mainService.getUserVendors(userInfo.getUserId());
		List<Operator> customers = mainService.getUserCustomers(userInfo.getUserId());
		List<User> engineers = mainService.getEngineers("");
		List<User> coordinators = mainService.getCoordinators();
		model.addAttribute("job", job);
		model.addAttribute("activities", activities);
		model.addAttribute("sites", sites);
		model.addAttribute("vendors", vendors);
		model.addAttribute("customers", customers);
		model.addAttribute("engineers", engineers);
		model.addAttribute("coordinators", coordinators);
		model.addAttribute("vendorId", job.getVendorId());
		model.addAttribute("operatorId", job.getCustomerId());
		model.addAttribute("engineerId", job.getAssignedTo());
		model.addAttribute("typeStatus", 0);
		model.addAttribute("mode", "Edit");
		model.addAttribute("page", page);
		model.addAttribute("search", search);
		model.addAttribute("activityTypeId", activityTypeId);
		model.addAttribute("statusId", statusId);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		if(user.getAuthority().equals(MainService.ROLE_COORDINATOR)) {
			model.addAttribute("coordinatorId", user.getUserId());
		}
		return "job";
	}
	
	@RequestMapping(value = "/job/save/{page}", method = RequestMethod.POST)
	public String saveJob(
		@ModelAttribute("job") Job job,
		@PathVariable("page") int page,
		@RequestParam(value = "activityTypeId", required = false, defaultValue = "0") Long activityTypeId,
		@RequestParam(value = "statusId", required = false, defaultValue = "0") Integer statusId,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		@RequestParam(value = "startDate", required = false, defaultValue = "") String startDate,
		@RequestParam(value = "endDate", required = false, defaultValue = "") String endDate,
		final RedirectAttributes redirectAttributes,BindingResult result,Model model) throws Exception {
		UserInfo userInfo = SecurityUtil.getCurrentUserInfo();
		User user = mainService.getUserByUserId(userInfo.getUserId());
		boolean err = false;
		if(!user.getAuthority().equals("ROLE_COORDINATOR")) {
			if(job.getCreatedBy() == null || job.getCreatedBy() == 0) {
				err = true;
				model.addAttribute("createdByErrMsg", "Please select job owner");
			}
		} else {
			job.setCreatedBy(user.getUserId());
			model.addAttribute("coordinatorId", user.getUserId());
		}
		jobValidator.validate(job, result);
		model.addAttribute("typeStatus", 0);
		if (result.hasErrors() || err) {
			List<MetaActivity> activities = mainService.getActivityTypes(userInfo.getUserId());
			List<Site> sites = mainService.getSites("");
			List<Vendor> vendors = mainService.getUserVendors(userInfo.getUserId());
			List<Operator> customers = mainService.getUserCustomers(userInfo.getUserId());
			List<User> engineers = mainService.getEngineers("");
			List<User> coordinators = mainService.getCoordinators();
			model.addAttribute("activities", activities);
			model.addAttribute("sites", sites);
			model.addAttribute("vendors", vendors);
			model.addAttribute("customers", customers);
			model.addAttribute("engineers", engineers);
			model.addAttribute("coordinators", coordinators);
			model.addAttribute("vendorId", job.getVendorId());
			model.addAttribute("operatorId", job.getCustomerId());
			model.addAttribute("engineerId", job.getAssignedTo());
			if(job.getJobId() == null)
			{
				model.addAttribute("mode","New");
				
			}
			else
			{
				model.addAttribute("mode","Edit");
			}
			return "job";
		}
		if(job.getJobId() == null){
		  long flag = mainService.addJob(job,userInfo.getUserId());
			if (flag > 0) {
				redirectAttributes.addFlashAttribute("successMessage", "Job added successfully.");
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "Sorry!! please try again.");
			}
		  }else{
		   long flag = mainService.updateJob(job);	
			if (flag > 0) {
					redirectAttributes.addFlashAttribute("successMessage", "Job updated successfully.");
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "Sorry!! please try again.");
			}
		}
		return "redirect:/web/jobs?page=" + page+"&search="+search+"&activityTypeId="+activityTypeId+"&statusId="+statusId+"&startDate="+startDate+"&endDate="+endDate;
	}
	
	@RequestMapping(value = "/delete/job/{jobId}/{page}", method = RequestMethod.GET)
	public String deleteJob(
		@PathVariable("jobId") Long jobId,
		@PathVariable("page") int page,
		@RequestParam(value = "activityTypeId", required = false, defaultValue = "") Long activityTypeId,
		@RequestParam(value = "statusId", required = false, defaultValue = "") Integer statusId,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		@RequestParam(value = "startDate", required = false, defaultValue = "") String startDate,
		@RequestParam(value = "endDate", required = false, defaultValue = "") String endDate,
		final RedirectAttributes redirectAttributes,
		Model model) throws Exception {
		
		int flag=mainService.deleteJob(jobId);
		if(flag>0)
		{
			redirectAttributes.addFlashAttribute("successMessage", "Job deleted successfully.");
		}
		else
		{
			redirectAttributes.addFlashAttribute("errorMessage", "Sorry!! please try again.");
		}
		return "redirect:/web/jobs?page=" + page+"&search="+search+"&activityTypeId="+activityTypeId+"&statusId="+statusId+"&startDate="+startDate+"&endDate="+endDate;
	}
	
	@RequestMapping(value = "/coordinator/vendors", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody List<Vendor> getVendors(Model model,
			@RequestBody long coordinatorIdVal) {
		List<Vendor> vendors = mainService.getUserVendors(coordinatorIdVal);
		return vendors;
	}
	
	@RequestMapping(value = "/coordinator/customers", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody List<Operator> getCustomers(Model model,
			@RequestBody long coordinatorIdVal) {
		List<Operator> customers = mainService.getUserCustomers(coordinatorIdVal);
		return customers;
	}
	
	@RequestMapping(value = "/vendor/customer/engineers", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
	public @ResponseBody List<User> getCustomers(Model model,
			@RequestParam(value="vendorId",required=false,defaultValue="") Long vendorId,
			@RequestParam(value="customerId",required=false,defaultValue="") Long customerId) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		List<User> engineers = mainService.getEngineersByVendorIdAndCustomerId(vendorId,customerId);
		return engineers;
	}
	
	@RequestMapping(value = "/job/{jobId}/work/{workId}/details", method = RequestMethod.GET)
	public String jobDetails(
			Model model,@PathVariable("jobId") Long jobId,@PathVariable("workId") Long workId,
			@RequestParam(value = "search", required = false, defaultValue = "") String search,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "-1") int pageSize,HttpServletResponse response) throws IOException, SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (pageSize < 0) {
			pageSize = constants.getPageSize();
		}
		
		 int index=0;
		 if(page>0){
		  index=(page-1)*pageSize;
		 }

		 model.addAttribute("indexNo",index);
		List<JobDetailResponse> jobDetails = mainService.getJobDetailsByJobId(jobId,workId,search,page,pageSize);
		model.addAttribute("jobDetails", jobDetails);
		setPagination(model, jobDetails, page);
		model.addAttribute("page", page);
		model.addAttribute("search", search);
		model.addAttribute("jobId", jobId);
		model.addAttribute("workId", workId);
		return "job_details";
	}
	
	@RequestMapping(value = "/job/detail/files", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody List<JobDetailFile> getJobDetailFiles(Model model,
			@RequestBody long jobDetailId) {
		List<JobDetailFile> jobDetailFiles = mainService.getJobDetailFiles(jobDetailId);
		return jobDetailFiles;
	}
	
	
	@RequestMapping(value = "/job/detail/questions/files", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody JobDetailResponse getJobDetailQuestionsFiles(Model model,
			@RequestParam(value = "jobId", required = false, defaultValue = "") Long jobId,
			@RequestParam(value = "workId", required = false, defaultValue = "") Long workId,
			@RequestParam(value = "jobDetId", required = false, defaultValue = "") Long jobDetId) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		JobDetailResponse jobDetailResponse = new JobDetailResponse();
		List<JobDetailQuesAnsReponse> jobDetailQuesAnsReponses = mainService.getJobDetailQuestionAnswers(jobDetId,"",1,Integer.MAX_VALUE);
		List<JobDetailFile> jobDetailFiles = mainService.getJobDetailFiles(jobDetId);
		jobDetailResponse.setJobDetailQuesAnsReponses(jobDetailQuesAnsReponses);
		jobDetailResponse.setJobDetailFiles(jobDetailFiles);
		return jobDetailResponse;
	}
	
	@RequestMapping(value = "/job/audit/detail/files", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody List<Long> getJobAuditDetailFiles(Model model,
			@RequestBody long jobAuditDetId) {
		List<Long> jobAuditDetailFiles = mainService.getJobAuditDetailFiles(jobAuditDetId);
		return jobAuditDetailFiles;
	}
	
	@RequestMapping(value = "/job/{jobId}/work/{workId}/audits", method = RequestMethod.GET)
	public String jobWorkAudits(
			Model model,@PathVariable("jobId") Long jobId,@PathVariable("workId") Long workId,
			@RequestParam(value = "typeId", required = false, defaultValue = "") Integer typeId,
			@RequestParam(value = "search", required = false, defaultValue = "") String search,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "-1") int pageSize,HttpServletResponse response) throws IOException, SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (pageSize < 0) {
			pageSize = constants.getPageSize();
		}
		
		 int index=0;
		 if(page>0){
		  index=(page-1)*pageSize;
		 }

		 model.addAttribute("indexNo",index);
		List<JobAuditResponse> auditResponses = mainService.getJobAudits(jobId,typeId,workId,search,page,pageSize);
		model.addAttribute("auditResponses", auditResponses);
		setPagination(model, auditResponses, page);
		model.addAttribute("page", page);
		model.addAttribute("search", search);
		model.addAttribute("jobId", jobId);
		model.addAttribute("workId", workId);
		model.addAttribute("typeId", typeId);
		return "job_audits";
	}
	
	@RequestMapping(value = "/job/audit/{auditId}/{jobId}/{workId}/{auditedBy}/details", method = RequestMethod.GET)
	public String jobAuditDetails(
			Model model,@PathVariable("jobId") Long jobId,@PathVariable("workId") Long workId,
			@PathVariable("auditId") Long auditId,@PathVariable("auditedBy") Long auditedBy,
			@RequestParam(value = "statusId", required = false, defaultValue = "") Integer statusId,
			@RequestParam(value = "search", required = false, defaultValue = "") String search,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "-1") int pageSize,HttpServletResponse response) throws IOException, SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (pageSize < 0) {
			pageSize = constants.getPageSize();
		}
		
		 int index=0;
		 if(page>0){
		  index=(page-1)*pageSize;
		 }

		model.addAttribute("indexNo",index);
		List<JobAuditDetailsResponse> auditDetailsResponses = mainService.getJobAuditDetails(auditId,statusId,search,page,pageSize);
		User user = mainService.getUserByUserId(auditedBy);
		JobAuditDetail auditDetail = mainService.getJobAuditById(auditId);
		JobResponse jobResponse = mainService.getJobResponseByJobId(jobId);
		List<JobResponse> jobs = new ArrayList<JobResponse>();
		jobs.add(jobResponse);
		model.addAttribute("auditDetailsResponses", auditDetailsResponses);
		setPagination(model, auditDetailsResponses, page);
		model.addAttribute("jobs", jobs);
		model.addAttribute("page", page);
		model.addAttribute("search", search);
		model.addAttribute("jobId", jobId);
		model.addAttribute("workId", workId);
		model.addAttribute("auditId", auditId);
		model.addAttribute("auditedBy", auditedBy);
		model.addAttribute("auditedByName", user.getName());
		model.addAttribute("auditTime", auditDetail != null ? auditDetail.getCreatedTime() : "");
		model.addAttribute("statusId", statusId);
		return "audit_details";
	}
	
	@RequestMapping(value = "/job/audit/{auditId}/{jobId}/{workId}/export", method = RequestMethod.GET)
	public String exportPreferencesIntoExcel(Model model,
			@PathVariable("jobId") Long jobId,@PathVariable("workId") Long workId,
			@PathVariable("auditId") Long auditId,
			HttpServletResponse response) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		mainService
				.jobAuditDetailsExportIntoExcel(jobId,workId,auditId, response);

		return null;
	}
	
	@RequestMapping(value = "/job/audit/{auditId}/{jobId}/{workId}/report", method = RequestMethod.GET)
	public String jobAuditReport(
			Model model,@PathVariable("jobId") Long jobId,@PathVariable("workId") Long workId,
			@PathVariable("auditId") Long auditId,HttpServletResponse response) throws IOException, SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		UserInfo userInfo = SecurityUtil.getCurrentUserInfo();
		User user = mainService.getUserByUserId(userInfo.getUserId());
		List<VendorSubCategoryResponse> auditResponses = mainService.getAuditReport(jobId,workId,auditId,user);
		JobResponse jobResponse = mainService.getJobResponseByJobId(jobId);
		JobAuditResponse auditResponse = mainService.getJobAuditResponseByAuditId(auditId);
		List<ViolationLevel> violationLevels = mainService.getAuditViolationLevels(jobResponse.getVendorId(),workId,auditId);
		model.addAttribute("auditResponses", auditResponses);
		model.addAttribute("jobResponse", jobResponse);
		model.addAttribute("auditResponse", auditResponse);
		model.addAttribute("violationLevels", violationLevels);
		model.addAttribute("jobId", jobId);
		model.addAttribute("workId", workId);
		model.addAttribute("auditId", auditId);
		return "audit_report";
	}
	
	@RequestMapping(value = "/user/{userId}/audits/{auditId}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public @ResponseBody List<JobAuditDetailsResponse> getUserAudits(Model model,@PathVariable("userId") Long userId,@PathVariable("auditId") Long auditId) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return mainService.getUserAudits(auditId,userId);
	}
	
	@RequestMapping(value = "/job/{jobId}/work/history", method = RequestMethod.GET)
	public String jobWorkHistory(
			Model model,@PathVariable("jobId") Long jobId,
			@RequestParam(value = "statusId", required = false, defaultValue = "") Integer statusId,
			@RequestParam(value = "search", required = false, defaultValue = "") String search,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "-1") int pageSize,HttpServletResponse response) throws IOException, SQLException {
		if (pageSize < 0) {
			pageSize = constants.getPageSize();
		}
		
		 int index=0;
		 if(page>0){
		  index=(page-1)*pageSize;
		 }

		 model.addAttribute("indexNo",index);
		List<JobWorkHistory> workHistories = mainService.getJobWorkHistoryByJobId(jobId,statusId,search,page,pageSize);
		model.addAttribute("workHistories", workHistories);
		setPagination(model, workHistories, page);
		model.addAttribute("page", page);
		model.addAttribute("search", search);
		model.addAttribute("statusId", statusId);
		return "job_work_history";
	}
	
	@RequestMapping(value = "/job/{jobId}/detail/{jobDetailId}/{workId}/question/answers", method = RequestMethod.GET)
	public String jobDetailQuestionAnswers(
			Model model,@PathVariable("jobDetailId") Long jobDetailId,@PathVariable("workId") Long workId,@PathVariable("jobId") Long jobId,
			@RequestParam(value = "search", required = false, defaultValue = "") String search,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "-1") int pageSize,HttpServletResponse response) throws IOException, SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (pageSize < 0) {
			pageSize = constants.getPageSize();
		}
		
		 int index=0;
		 if(page>0){
		  index=(page-1)*pageSize;
		 }

		 model.addAttribute("indexNo",index);
		List<JobDetailQuesAnsReponse> jobDetailQuesAnsReponses = mainService.getJobDetailQuestionAnswers(jobDetailId,search,page,pageSize);
		model.addAttribute("jobDetailQuesAnsReponses", jobDetailQuesAnsReponses);
		setPagination(model, jobDetailQuesAnsReponses, page);
		model.addAttribute("page", page);
		model.addAttribute("search", search);
		model.addAttribute("jobDetailId", jobDetailId);
		model.addAttribute("workId", workId);
		model.addAttribute("jobId", jobId);
		return "job_detail_ques_ans";
	}
	/*@RequestMapping(value = "/rate/card/{id}/delete/{page}", method = RequestMethod.GET)
	public String deleteRateCard(
		Model model,
		@PathVariable("id") Long id,
		@PathVariable("page") int page,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		final RedirectAttributes redirectAttributes) throws Exception {
		
		long flag = mainService.deleteRateCardById(id);
		
		if (flag > 0) {
			redirectAttributes.addFlashAttribute("successMessage", "Rate Card deleted successfully.");
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "Sorry!! please try again.");
		}
		return "redirect:/web/rate/cards?page=" + page+"&search="+search;
	}*/
	
	@RequestMapping(value = "/user/{userId}/reset/password/{page}", method = RequestMethod.GET)
	public String resetUserPassword(
		@PathVariable("userId") Long userId,
		@PathVariable("page") int page,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		final RedirectAttributes redirectAttributes,
		Model model) throws Exception {
		
		Long flag = mainService.resetUserPassword(userId);

		model.addAttribute("page", page);
		model.addAttribute("search", search);
		
		if (flag > 0) {
			redirectAttributes.addFlashAttribute("successMessage", "Reset password successfully.");
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "Sorry!! please try again.");
		}
	  
	    return "redirect:/web/user/management?page=" + page+"&search="+search;
	}
	@RequestMapping(value = "/docs", method = RequestMethod.GET)
	public String docs(
			Model model, 
			@RequestParam(value = "search", required = false, defaultValue = "") String search,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "-1") int pageSize,HttpServletResponse response) throws IOException, SQLException {
		if (pageSize < 0) {
			pageSize = constants.getPageSize();
		}
		
		 int index=0;
		 if(page>0){
		  index=(page-1)*pageSize;
		 }

		model.addAttribute("indexNo",index);
		List<Doc> docs = mainService.getAllDocs(search,page,pageSize);
		
		model.addAttribute("docs", docs);
		setPagination(model, docs, page);
		model.addAttribute("page", page);
		model.addAttribute("search", search);
		return "docs";
	}
	@RequestMapping(value = "/new/doc", method = RequestMethod.GET)
	public String addDoc(Model model) throws Exception {
		Doc doc = new Doc();
		//Doc doc = new Doc();
		List<User> engineers = mainService.getEngineers("");
		model.addAttribute("doc", doc);
		model.addAttribute("engineers", engineers);
		model.addAttribute("mode", "New");
		model.addAttribute("page", 1);
		return "doc";
	}
	@RequestMapping(value = "/doc/save/{page}", method = RequestMethod.POST)
	public String saveDoc(
		@ModelAttribute("doc") Doc doc,
		@PathVariable("page") int page,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		@RequestParam("fileItem1") CommonsMultipartFile fileItem1,
		@RequestParam("fileItem2") CommonsMultipartFile fileItem2,
		@RequestParam("fileItem3") CommonsMultipartFile fileItem3,
		final RedirectAttributes redirectAttributes,BindingResult result,Model model) throws Exception {
		UserInfo userInfo = SecurityUtil.getCurrentUserInfo();
		User user = mainService.getUserByUserId(userInfo.getUserId());
		
		
		  if(fileItem1 == null || fileItem1.getSize() <= 0 || fileItem3 == null ||
		  fileItem3.getSize() <= 0 || fileItem3 == null || fileItem3.getSize() <= 0) {
		  redirectAttributes.addFlashAttribute("message",
		  "Please Select File To Upload"); return "redirect:/web/new/doc"; }
		 
		
		String fullPath1 = mainService.uploadDoc(fileItem1,1);
		String fullPath2 = mainService.uploadDoc(fileItem2,2);
		String fullPath3 = mainService.uploadDoc(fileItem3,3);
		doc.setfarmCert(fullPath1);
		doc.setfirstaidCert(fullPath2);
		doc.setmedicalCert(fullPath3);
		
		if(doc.getorgType().equalsIgnoreCase("PYRO")) {
			doc.setorgName("PYRO");
			User empdata = mainService.getUserByUserId(doc.getempId());
			doc.setempName(empdata.getName());
		}else {
			doc.setempId(doc.getempId2()); 
		}
		
		docValidator.validate(doc, result);
		if (result.hasErrors()) {
			System.out.println("result"+result.toString());
			if(doc.getId() == null)
			{
				model.addAttribute("mode","New");
				
			}
			else
			{
				model.addAttribute("mode","Edit");
			}
			return "doc";
		}
		if(doc.getId() == null){
			
		doc.setinsertBy(user.getUserId());
		doc.setinsertUser(user.getName());
		doc.setstatus("INITIATED");
		//System.out.println("Data In addition"+doc.toString());
			
		//System.out.println("ServiceID"+userInfo.getServiceId());
		  long flag = mainService.addDoc(doc);
			if (flag > 0) {
				redirectAttributes.addFlashAttribute("successMessage", "Documents added successfully.");
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "Sorry!! please try again.");
			}
		  }else{
			/*
			 * long flag = mainService.updateSite(doc); if (flag > 0) {
			 * redirectAttributes.addFlashAttribute("successMessage",
			 * "Documents updated successfully."); } else {
			 * redirectAttributes.addFlashAttribute("errorMessage",
			 * "Sorry!! please try again."); }
			 */
		}
		return "redirect:/web/docs?page=" + page+"&search="+search;
	}	
	@RequestMapping(value = "/doc/{docId}/edit/{page}", method = RequestMethod.GET)
	public String editDoc(
		@PathVariable("docId") Long docId,
		@PathVariable("page") int page,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		Model model) throws Exception {
		
		Doc doc = mainService.getDocById(docId);
		System.out.println("Data in edit----"+doc.toString());
		/* List<MetaCircle> circles = mainService.getAllCircles(); */
		model.addAttribute("doc", doc);
		/* model.addAttribute("circles", circles); */
		model.addAttribute("mode", "Edit");
		model.addAttribute("page", page);
		model.addAttribute("search", search);
		
		return "doc_edit";
	}
	@RequestMapping(value = "/doc/reject/{Id}/{page}", method = RequestMethod.GET)
	public String rejectDoc(
		@PathVariable("Id") Long Id,
		@PathVariable("page") int page,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		final RedirectAttributes redirectAttributes,
		Model model) throws Exception {
		UserInfo userInfo = SecurityUtil.getCurrentUserInfo();
		User user = mainService.getUserByUserId(userInfo.getUserId());
		int flag=mainService.rejectDoc(Id,user.getUserId(),user.getName());
		if(flag>0)
		{
			redirectAttributes.addFlashAttribute("successMessage", "Documents Rejected.");
		}
		else
		{
			redirectAttributes.addFlashAttribute("errorMessage", "Sorry!! please try again.");
		}
		return "redirect:/web/docs?page=" + page+"&search="+search;
	}
	@RequestMapping(value = "/doc/approve/{Id}/{page}", method = RequestMethod.GET)
	public String approveDoc(
		@PathVariable("Id") Long Id,
		@PathVariable("page") int page,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		final RedirectAttributes redirectAttributes,
		Model model) throws Exception {
		UserInfo userInfo = SecurityUtil.getCurrentUserInfo();
		User user = mainService.getUserByUserId(userInfo.getUserId());
		int flag=mainService.approveDoc(Id,user.getUserId(),user.getName());
		if(flag>0)
		{
			redirectAttributes.addFlashAttribute("successMessage", "Documents Approved.");
		}
		else
		{
			redirectAttributes.addFlashAttribute("errorMessage", "Sorry!! please try again.");
		}
		return "redirect:/web/docs?page=" + page+"&search="+search;
	}
	@RequestMapping(value = "/delete/doc/{Id}/{page}", method = RequestMethod.GET)
	public String deleteDoc(
		@PathVariable("Id") Long Id,
		@PathVariable("page") int page,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		final RedirectAttributes redirectAttributes,
		Model model) throws Exception {
		UserInfo userInfo = SecurityUtil.getCurrentUserInfo();
		User user = mainService.getUserByUserId(userInfo.getUserId());
		int flag=mainService.deleteDoc(Id,user.getUserId(),user.getName());
		if(flag>0)
		{
			redirectAttributes.addFlashAttribute("successMessage", "Documents deleted successfully.");
		}
		else
		{
			redirectAttributes.addFlashAttribute("errorMessage", "Sorry!! please try again.");
		}
		return "redirect:/web/docs?page=" + page+"&search="+search;
	}
	@RequestMapping(value = "/sites", method = RequestMethod.GET)
	public String sites(
			Model model, 
			@RequestParam(value = "search", required = false, defaultValue = "") String search,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "-1") int pageSize,HttpServletResponse response) throws IOException, SQLException {
			
		if (pageSize < 0) {
			pageSize = constants.getPageSize();
		}
		
		 int index=0;
		 if(page>0){
		  index=(page-1)*pageSize;
		 }

		model.addAttribute("indexNo",index);
		List<Site> sites = mainService.getAllSites(search,page,pageSize);
		model.addAttribute("sites", sites);
		setPagination(model, sites, page);
		model.addAttribute("page", page);
		model.addAttribute("search", search);
		return "sites";
	}
	
	@RequestMapping(value = "/new/site", method = RequestMethod.GET)
	public String addSite(Model model) throws Exception {
		Site site = new Site();
		List<MetaCircle> circles = mainService.getAllCircles();
		model.addAttribute("site", site);
		model.addAttribute("circles", circles);
		model.addAttribute("mode", "New");
		model.addAttribute("page", 1);
		return "site";
	}
	
	@RequestMapping(value = "/site/{siteId}/edit/{page}", method = RequestMethod.GET)
	public String editSite(
		@PathVariable("siteId") Long siteId,
		@PathVariable("page") int page,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		Model model) throws Exception {
		
		Site site = mainService.getSiteBySiteId(siteId);
		List<MetaCircle> circles = mainService.getAllCircles();
		model.addAttribute("site", site);
		model.addAttribute("circles", circles);
		model.addAttribute("mode", "Edit");
		model.addAttribute("page", page);
		model.addAttribute("search", search);
		
		return "site";
	}
	
	@RequestMapping(value = "/site/save/{page}", method = RequestMethod.POST)
	public String saveSite(
		@ModelAttribute("site") Site site,
		@PathVariable("page") int page,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		final RedirectAttributes redirectAttributes,BindingResult result,Model model) throws Exception {
		
		siteValidator.validate(site, result);
		if (result.hasErrors()) {
			List<MetaCircle> circles = mainService.getAllCircles();
			model.addAttribute("circles", circles);
			if(site.getSiteId() == null)
			{
				model.addAttribute("mode","New");
				
			}
			else
			{
				model.addAttribute("mode","Edit");
			}
			return "site";
		}
		if(site.getSiteId() == null){
		  long flag = mainService.addSite(site);
			if (flag > 0) {
				redirectAttributes.addFlashAttribute("successMessage", "Site added successfully.");
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "Sorry!! please try again.");
			}
		  }else{
		   long flag = mainService.updateSite(site);	
			if (flag > 0) {
					redirectAttributes.addFlashAttribute("successMessage", "Site updated successfully.");
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "Sorry!! please try again.");
			}
		}
		return "redirect:/web/sites?page=" + page+"&search="+search;
	}
	
	@RequestMapping(value = "/delete/site/{siteId}/{page}", method = RequestMethod.GET)
	public String deleteSite(
		@PathVariable("siteId") Long siteId,
		@PathVariable("page") int page,
		@RequestParam(value = "search", required = false, defaultValue = "") String search,
		final RedirectAttributes redirectAttributes,
		Model model) throws Exception {
		
		int flag=mainService.deleteSite(siteId);
		if(flag>0)
		{
			redirectAttributes.addFlashAttribute("successMessage", "Site deleted successfully.");
		}
		else
		{
			redirectAttributes.addFlashAttribute("errorMessage", "Sorry!! please try again.");
		}
		return "redirect:/web/sites?page=" + page+"&search="+search;
	}
	
	@RequestMapping(value = "/upload/users", method = RequestMethod.POST)
    public String uploadUsers(@RequestParam("fileItem") CommonsMultipartFile fileItem,
            final RedirectAttributes redirectAttributes)
            throws Exception {
           
			if(fileItem == null || fileItem.getSize() <= 0)
			{
				redirectAttributes.addFlashAttribute("message", "Please Browse Excel Sheet To Upload");
				return "redirect:/web/user/management";
			}
			
			try {
				if (fileItem.getFileItem() != null
						&& fileItem.getFileItem().getSize() > 0) {
					if (!fileItem.getContentType().equalsIgnoreCase(
							"application/vnd.ms-excel")
							&& !fileItem
									.getContentType()
									.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {

						redirectAttributes.addFlashAttribute("message", "Please Upload Excel Sheet");
						return "redirect:/web/user/management";
					}
				}

				String fullPath = mainService.uploadExcel(fileItem);

				if (fullPath != null) {
					UserInfo userInfo = SecurityUtil.getCurrentUserInfo();
					boolean err = mainService.addUserBulkUpload(fullPath,userInfo.getUserId());
					if(err) {
						redirectAttributes.addFlashAttribute("errorMessage", "Some data is wrong in excel file");
					} else {
						
						redirectAttributes.addFlashAttribute("successMessage", "Users saved successfully");
					}
				}
			} catch (Exception e) {
				}
			
			return "redirect:/web/user/management";
    }
	
	@RequestMapping(value = "/user/template/download", method = RequestMethod.GET)
    public String userTemplateDownload(final RedirectAttributes redirectAttributes,HttpServletResponse response)
            throws Exception {
           
			mainService.downloadUserTemplate(response);
			
			return null;
    }
	
	@RequestMapping(value = "/upload/jobs", method = RequestMethod.POST)
    public String uploadJobs(@RequestParam("fileItem") CommonsMultipartFile fileItem,
            final RedirectAttributes redirectAttributes)
            throws Exception {
           
			if(fileItem == null || fileItem.getSize() <= 0)
			{
				redirectAttributes.addFlashAttribute("message", "Please Browse Excel Sheet To Upload");
				return "redirect:/web/jobs";
			}
			
			try {
				if (fileItem.getFileItem() != null
						&& fileItem.getFileItem().getSize() > 0) {
					if (!fileItem.getContentType().equalsIgnoreCase(
							"application/vnd.ms-excel")
							&& !fileItem
									.getContentType()
									.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {

						redirectAttributes.addFlashAttribute("message", "Please Upload Excel Sheet");
						return "redirect:/web/jobs";
					}
				}

				String fullPath = mainService.uploadExcel(fileItem);

				if (fullPath != null) {
					UserInfo userInfo = SecurityUtil.getCurrentUserInfo();
					boolean err = mainService.addJobBulkUpload(fullPath,userInfo.getUserId());
					if(err) {
						redirectAttributes.addFlashAttribute("errorMessage", "Some data is wrong in excel file");
					} else {
						
						redirectAttributes.addFlashAttribute("successMessage", "Jobs saved successfully");
					}

				}
			} catch (Exception e) {
				}
			
			return "redirect:/web/jobs";
    }
	
	@RequestMapping(value = "/job/template/download", method = RequestMethod.GET)
    public String jobTemplateDownload(final RedirectAttributes redirectAttributes,HttpServletResponse response)
            throws Exception {
           
			mainService.downloadJobTemplate(response);
			
			return null;
    }
	
	private void setPagination(Model model, Collection<?> collection, int page) {
		setPagination(model, collection, page, null);
	}

	private void setPagination(Model model, Collection<?> collection, int page,
			String prefix) {
		if (prefix == null) {
			prefix = "";
		}

		if (page == 1) {
			model.addAttribute(prefix + "previousDisabled", "disabled");
			model.addAttribute(prefix + "previousPage", page);
		} else {
			model.addAttribute(prefix + "previousDisabled", "");
			model.addAttribute(prefix + "previousPage", page - 1);
		}

		if (collection != null && collection.size() >= constants.getPageSize()) {
			model.addAttribute(prefix + "nextDisabled", "");
			model.addAttribute(prefix + "nextPage", page + 1);
		} else {
			model.addAttribute(prefix + "nextDisabled", "disabled");
			model.addAttribute(prefix + "nextPage", page);
		}

		model.addAttribute("page", page);
	}
	
	@ExceptionHandler(Exception.class)
	public ModelAndView handleError(HttpServletRequest request, Exception exception) {
		Log.error(this.getClass(), exception.toString(), exception);
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("url", request.getRequestURL());
		mav.addObject("exception", exception);
		mav.setViewName("errorPage");
		
		return mav;
	}

}


