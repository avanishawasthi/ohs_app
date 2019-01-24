package in.eightfolds.pyro_safety_app.service;
 
 
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import in.eightfolds.commons.excel.Excel;
import in.eightfolds.commons.mail.PostOffice;
import in.eightfolds.commons.spring_security.SecurityUtil;
import in.eightfolds.commons.spring_security.UserInfo;
import in.eightfolds.commons.util.CollectionUtil;
import in.eightfolds.commons.util.Strings;
import in.eightfolds.pyro_safety_app.Constants;
import in.eightfolds.pyro_safety_app.bean.JobAuditDetailsResponse;
import in.eightfolds.pyro_safety_app.bean.JobAuditResponse;
import in.eightfolds.pyro_safety_app.bean.JobDetailQuesAnsReponse;
import in.eightfolds.pyro_safety_app.bean.JobDetailResponse;
import in.eightfolds.pyro_safety_app.bean.JobResponse;
import in.eightfolds.pyro_safety_app.bean.MetaAuditSubCatQuestionResponse;
import in.eightfolds.pyro_safety_app.bean.UserResponse;
import in.eightfolds.pyro_safety_app.bean.VendorSubCategoryResponse;
import in.eightfolds.pyro_safety_app.bean.ViolationLevel;
import in.eightfolds.pyro_safety_app.bean.entity.AppFile;
import in.eightfolds.pyro_safety_app.bean.entity.Authorities;
import in.eightfolds.pyro_safety_app.bean.entity.Job;
import in.eightfolds.pyro_safety_app.bean.entity.JobAuditDetail;
import in.eightfolds.pyro_safety_app.bean.entity.JobDetail;
import in.eightfolds.pyro_safety_app.bean.entity.JobDetailFile;
import in.eightfolds.pyro_safety_app.bean.entity.JobDetailQuesAns;
import in.eightfolds.pyro_safety_app.bean.entity.JobWorkHistory;
import in.eightfolds.pyro_safety_app.bean.entity.MetaActivity;
import in.eightfolds.pyro_safety_app.bean.entity.MetaAuditSubCatQuesOption;
import in.eightfolds.pyro_safety_app.bean.entity.MetaCircle;
import in.eightfolds.pyro_safety_app.bean.entity.Operator;
import in.eightfolds.pyro_safety_app.bean.entity.Site;
import in.eightfolds.pyro_safety_app.bean.entity.TPVendor;
import in.eightfolds.pyro_safety_app.bean.entity.User;
import in.eightfolds.pyro_safety_app.bean.entity.Vendor;
import in.eightfolds.pyro_safety_app.bean.entity.VendorName;
import in.eightfolds.pyro_safety_app.dao.MainDao;
import in.eightfolds.pyro_safety_app.dao.UserDao;



@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
public class MainService {
	
	 
	@Autowired
	private MainDao mainDao;
	@Autowired
	private UserDao userDao;
	
	private static final Logger logger = Logger.getLogger(MainService.class);
	
	@Autowired
	private Constants constants;
	@Autowired
	private PostOffice postOffice;
	
	public static final String ROLE_EMPLOYEE  = "ROLE_EMPLOYEE";
	public static final String ROLE_ENGINEER  = "ROLE_ENGINEER";
	public static final String ROLE_COORDINATOR  = "ROLE_COORDINATOR";
	public static final String ROLE_TECHNICIAN  = "ROLE_TECHNICIAN";
	public static final String ROLE_PROJECT_MANAGER  = "ROLE_PROJECT_MANAGER";
	public static final String ROLE_OHS  = "ROLE_OHS";
	public static final String ROLE_OHS_LEAD  = "ROLE_OHS_LEAD";
	public static final String ROLE_TOP_MANAGEMENT  = "ROLE_TOP_MANAGEMENT";
	public static final String ROLE_SUPER_ADMIN  = "ROLE_SUPER_ADMIN";
	public static final String ROLE_VENDOR  = "ROLE_VENDOR";
	public static final String ROLE_CUSTOMER  = "ROLE_CUSTOMER";
	public static final String ROLE_SITE  = "ROLE_SITE";
	
	public static final int JOB_STATUS_REJECTED  = -1;
	public static final int JOB_STATUS_NEW  = 0;
	public static final int JOB_STATUS_ON_GOING  = 1;
	public static final int JOB_STATUS_PAUSED  = 2;
	public static final int JOB_STATUS_COMPLETED  = 3;
	
	public static final String USER_DEFAULT_PASSWORD  = "Pyro123";

	public List<User> getAllUsers(String search, int page, int pageSize, User user) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return mainDao.getAllUsers(search, page, pageSize,user);
	}

	public Long resetUserPassword(Long userId) {
		User user = mainDao.getUserByUserId(userId);
		String password = Strings.getRandomNumber(6);
		user.setPassword(encodePassword(password));
		Long flag = mainDao.updateUserPassword(user);
		sendNewRegistrationMail(user ,password);
		return flag;
	}
	
	private void sendNewRegistrationMail(User user, String password) {
		if (!Strings.isEmpty(user.getEmail())) {
			String mailSubject = Constants.getNewUserMailSubject();
			mailSubject = mailSubject.replace("<name>", Strings.isEmpty(user.getName()) ? user.getUsername() : user.getName());
			
			String mailBody = Constants.getNewUserMail();
			mailBody = mailBody.replace("<name>", Strings.isEmpty(user.getName()) ? user.getUsername() : user.getName());
			mailBody = mailBody.replace("<appname>", Constants.getAppName());
			mailBody = mailBody.replace("<username>", user.getUsername());
			mailBody = mailBody.replace("<password>", password);

			postOffice.sendMail(0, user.getEmail(), mailSubject, mailBody, false);
		}
	}
	private String encodePassword(String password){
		if(!Strings.isEmpty(password)){
			return new BCryptPasswordEncoder(15).encode(password);
		}
		
		return null;
	}

	public User getUserByUserId(Long id) {
		return mainDao.getUserByUserId(id);
	}

	public long addUser(User user, User loggedInUser, String circleIds, String vendorIds, String customerIds) {
		user.setLocked(false);
		/*if(user.getAuthority().equals(ROLE_EMPLOYEE)) {
			user.setEnabled(false);
		} else {
			user.setEnabled(true);
		}*/
		user.setEnabled(true);
		user.setExpired(false);
		user.setEmailVerified(true);
		user.setMobileVerified(true);
		user.setUsername(user.getMobile());
		String password = USER_DEFAULT_PASSWORD;
		if(!Strings.isEmpty(user.getEmail())) {
			password = Strings.getRandomNumber(4);
		}
		user.setPassword(encodePassword(password));
		long userId = userDao.addUser(user);
		if(loggedInUser.getAuthority().equals(ROLE_COORDINATOR)) {
			List<Long> vendorIdsTemp=getVendorIdsByUserId(loggedInUser.getUserId());
			List<Long> customerIdsTemp=getCustomerIdsByUserId(loggedInUser.getUserId());
			
			if(vendorIdsTemp != null && vendorIdsTemp.size() > 0) {
				vendorIds = Strings.toCSV(vendorIdsTemp);
			}
			if(customerIdsTemp != null && customerIdsTemp.size() > 0) {
				customerIds = Strings.toCSV(customerIdsTemp);
			}
		}
		if(!Strings.isEmpty(circleIds))
		{
			String[] circleArray = circleIds.split(",");
			List<String> circleIdsTemp = Arrays.asList(circleArray);
			userDao.addBulkUserCircles(userId,circleIdsTemp);
		}
		if(!Strings.isEmpty(vendorIds))
		{
			String[] vendorArray = vendorIds.split(",");
			List<String> vendorIdsTemp = Arrays.asList(vendorArray);
			userDao.addBulkUserVendors(userId,vendorIdsTemp);
		}
		if(!Strings.isEmpty(customerIds))
		{
			String[] customerArray = customerIds.split(",");
			List<String> customerIdsTemp = Arrays.asList(customerArray);
			userDao.addBulkUserCustomers(userId,customerIdsTemp);
		}
		userDao.addUserAuthority(user);
		if(user.getTpvendors() != null && user.getAuthority() == "ROLE_TECHNICIAN")
		{		userDao.addReportingto(user);}
		if(!Strings.isEmpty(user.getEmail()) && !user.getAuthority().equals(ROLE_EMPLOYEE)) {
			sendNewRegistrationMail(user,password);
		}
		return userId;
	}

	public long updateUser(User user, User loggedInUser, String circleIds, String vendorIds, String customerIds) {
		User userOld = mainDao.getUserByUserId(user.getUserId());
		userOld.setName(user.getName());
		userOld.setEmail(user.getEmail());
		userOld.setAuthority(user.getAuthority());
		userOld.setVendorId(user.getVendorId());
		userOld.setCustomerId(user.getCustomerId());
		userOld.setActivityGroup(user.getActivityGroup());
		long flag = userDao.updateUser(userOld);
		mainDao.deleteAuthorities(userOld.getUserId()+"");
		userDao.addUserAuthority(userOld);
		int flagTemp =mainDao.deleteUserCirclesByUserId(user.getUserId());
		if(!Strings.isEmpty(circleIds))
		{
			String[] circleArray = circleIds.split(",");
			List<String> circleIdsTemp = Arrays.asList(circleArray);
			userDao.addBulkUserCircles(user.getUserId(),circleIdsTemp);
		}
		 mainDao.deleteUserVendorsByUserId(user.getUserId());
		 mainDao.deleteUserCustomersByUserId(user.getUserId());
		 if(loggedInUser.getAuthority().equals(ROLE_COORDINATOR)) {
				List<Long> vendorIdsTemp=getVendorIdsByUserId(loggedInUser.getUserId());
				List<Long> customerIdsTemp=getCustomerIdsByUserId(loggedInUser.getUserId());
				
				if(vendorIdsTemp != null && vendorIdsTemp.size() > 0) {
					vendorIds = Strings.toCSV(vendorIdsTemp);
				}
				if(customerIdsTemp != null && customerIdsTemp.size() > 0) {
					customerIds = Strings.toCSV(customerIdsTemp);
				}
			}
		 
		 if(!Strings.isEmpty(vendorIds))
			{
				String[] vendorArray = vendorIds.split(",");
				List<String> vendorIdsTemp = Arrays.asList(vendorArray);
				userDao.addBulkUserVendors(user.getUserId(),vendorIdsTemp);
			}
			if(!Strings.isEmpty(customerIds))
			{
				String[] customerArray = customerIds.split(",");
				List<String> customerIdsTemp = Arrays.asList(customerArray);
				userDao.addBulkUserCustomers(user.getUserId(),customerIdsTemp);
			}
		 
		
		return flag;
	}

	public List<JobResponse> getAllJobs(Long activityTypeId, Integer statusId, User user, String search, String startDate, String endDate, int page, int pageSize) {
		return mainDao.getAllJobs(activityTypeId,statusId,user,search,startDate,endDate,page,pageSize);
	}

	public List<JobDetailResponse> getJobDetailsByJobId(Long jobId, Long workId, String search, int page, int pageSize) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		List<JobDetailResponse> jobDetailResponses = mainDao.getJobDetailsByJobId(jobId,workId,search,page,pageSize);
		if(jobDetailResponses != null && jobDetailResponses.size() > 0) {
			List<JobDetailFile> jobDetailFiles = mainDao.getJobDetailFiles();
			Map<?, List<?>> jobDetailFilesMapping = (Map<?, List<?>>) CollectionUtil.toMapList(jobDetailFiles, "jobDetId");
			for(JobDetailResponse jobDetailResponse : jobDetailResponses) {
				
				@SuppressWarnings("unchecked")
				List<JobDetailFile> jobDetailFilesTemp = ((List<JobDetailFile>) jobDetailFilesMapping.get(jobDetailResponse.getJobDetId()));
				if(jobDetailFilesTemp != null && jobDetailFilesTemp.size() > 0) {
					/*String fileIds[] = Strings.toCSV(jobDetailFilesTemp,"fileId").split(",");
					List<Long> fileIdsTemp = new ArrayList<Long>();
					for (String fileId : fileIds) {
						fileIdsTemp.add(Long.parseLong(fileId));
					}*/
					jobDetailResponse.setJobDetailFiles(jobDetailFilesTemp);
					
				}
			}
		}
		
		return jobDetailResponses;
	}

	public List<MetaCircle> getAllCircles() {
		return mainDao.getAllCircles();
	}
	public List<Authorities> getAllAuthorities() {
		List<Authorities> a = mainDao.getAllAuthorities();
		System.out.println("role length");
		System.out.println("role 1st print"+a.get(0));
		System.out.println("role 2nd length"+a.get(1));
		return mainDao.getAllAuthorities();
	}

	public List<Long> getCircleIdsByUserId(Long id) {
		return mainDao.getCircleIdsByUserId(id);
	}
	
	public List<Long> getVendorIdsByUserId(Long id) {
		return mainDao.getVendorIdsByUserId(id);
	}
	
	public List<Long> getCustomerIdsByUserId(Long id) {
		return mainDao.getCustomerIdsByUserId(id);
	}

	public List<JobWorkHistory> getJobWorkHistoryByJobId(Long jobId, Integer statusId, String search, int page, int pageSize) {
		return mainDao.getJobWorkHistoryByJobId(jobId,statusId, search, page, pageSize);
	}

	public List<JobDetailQuesAnsReponse> getJobDetailQuestionAnswers(Long jobDetailId, String search, int page,
			int pageSize) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		List<JobDetailQuesAnsReponse> quesAnsReponses = mainDao.getJobDetailQuestionAnswers(jobDetailId, search, page, pageSize);
		
		if(quesAnsReponses != null && quesAnsReponses.size() > 0) {
			List<MetaAuditSubCatQuesOption> subCatQuesOptions = mainDao.getSubCategoryQuestionOptions();
			Map<?, List<?>> subCatQuesOptionsMapping = (Map<?, List<?>>) CollectionUtil.toMapList(subCatQuesOptions, "quesId");
			for(JobDetailQuesAnsReponse quesAnsReponse : quesAnsReponses) {
				
				@SuppressWarnings("unchecked")
				List<MetaAuditSubCatQuesOption> subCatQuesOptionsTemp = ((List<MetaAuditSubCatQuesOption>) subCatQuesOptionsMapping.get(quesAnsReponse.getQuesId()));
				if(subCatQuesOptionsTemp != null && subCatQuesOptionsTemp.size() > 0) {
					quesAnsReponse.setSubCatQuesOptions(subCatQuesOptionsTemp);
					
				}
			}
		}
		return quesAnsReponses;
	}

	public int deleteUser(Long id) {
		int flag = userDao.deleteUser(id);
		mainDao.deleteAuthorities(id+"");
		return flag;
	}

	public List<JobDetailFile> getJobDetailFiles(long jobDetailId) {
		return mainDao.getJobDetailFiles(jobDetailId);
	}

	public List<JobAuditResponse> getJobAudits(Long jobId, Integer typeId, Long workId, String search, int page, int pageSize) {
		return mainDao.getJobAudits(jobId,typeId, workId, search, page, pageSize);
	}

	public List<JobAuditDetailsResponse> getJobAuditDetails(Long auditId, Integer statusId, String search, int page,
			int pageSize) {
		return mainDao.getJobAuditDetails(auditId,statusId, search, page, pageSize);
	}

	public List<Long> getJobAuditDetailFiles(long jobAuditDetId) {
		return mainDao.getJobAuditDetailFiles(jobAuditDetId);
	}

	public List<MetaActivity> getAllActivityTypes() {
		return mainDao.getAllActivityTypes();
	}

	public List<JobAuditDetailsResponse> getUserAudits(Long auditId, Long userId) {
		List<JobAuditDetailsResponse> auditDetailsResponses = mainDao.getUserAudits(auditId,userId);
		return auditDetailsResponses;
	}

	public JobAuditDetail getJobAuditById(Long auditId) {
		return mainDao.getJobAuditById(auditId);
	}

	public List<Site> getAllSites(String search, int page, int pageSize) {
		return mainDao.getAllSites(search, page, pageSize);
	}

	public Site getSiteBySiteId(Long siteId) {
		return mainDao.getSiteBySiteId(siteId);
	}

	public long addSite(Site site) {
		return mainDao.addSite(site);
	}

	public long updateSite(Site site) {
		return mainDao.updateSite(site);
	}

	public int deleteSite(Long siteId) {
		return mainDao.deleteSite(siteId);
	}

	public List<MetaActivity> getActivityTypes(long userId) {
		User user = mainDao.getUserByUserId(userId);
		List<MetaActivity> activities = mainDao.getActivityTypes();
		if(user.getAuthority().equals("ROLE_COORDINATOR")) {
			activities = mainDao.getActivityTypesByGroupId(user.getActivityGroup());
		}
		return activities;
	}

	public List<Site> getSites(String string) {
		return mainDao.getSites("");
	}

	public List<Vendor> getVendors() {
		return mainDao.getVendors();
	}

	public List<Operator> getCustomers(String string) {
		return mainDao.getCustomers("");
	}
	public List<TPVendor> getTPVendor(String string) {
		System.out.println(mainDao.getTPVendors("").get(0)+"");
		return mainDao.getTPVendors("");
	}

	public List<User> getEngineers(String search) {
		return mainDao.getUsersByRole(search,ROLE_ENGINEER,ROLE_SITE);
	}

	public Job getJobByJobId(Long jobId) {
		return mainDao.getJobByJobId(jobId);
	}

	public long addJob(Job job, long userId) {
		System.out.println("Inside add Job");
		User user = mainDao.getEngineerByUserId(job.getAssignedTo());
		if(user != null) {
			job.setCircleId(user.getCircleId());
		}
		long jobId = mainDao.addJob(job);
		JobWorkHistory jobWorkHistory = new JobWorkHistory();
		jobWorkHistory.setJobId(job.getJobId());
		jobWorkHistory.setAssignedTo(job.getAssignedTo());
		mainDao.addJobWorkHistory(jobWorkHistory);

		System.out.println("Job Added"+job);
		return jobId;
	}

	public long updateJob(Job job) {
		User user = mainDao.getEngineerByUserId(job.getAssignedTo());
		if(user != null) {
			job.setCircleId(user.getCircleId());
		}
		long flag = mainDao.updateJob(job);
		mainDao.updateJobWorkHistory(job.getAssignedTo(),job.getJobId());
		return flag;
	}

	public int deleteJob(Long jobId) {
		return mainDao.deleteJob(jobId);
	}

	public JobResponse getJobResponseByJobId(Long jobId) {
		return mainDao.getJobResponseByJobId(jobId);
	}

	public List<VendorSubCategoryResponse> getAuditReport(Long jobId, Long workId, Long auditId, User user) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			List<VendorSubCategoryResponse> subCategoryResponses = new ArrayList<VendorSubCategoryResponse>();
			List<JobDetail> jobDetails = mainDao.getJobDetailByJobIdWorkId(jobId,workId);
			if(jobDetails != null && jobDetails.size() > 0) {
				subCategoryResponses = mainDao.getJobDetailSubCategories(jobId,workId,auditId,user.getAuthority());
			}
			//TODO Get structure based on workId
			if(subCategoryResponses != null && subCategoryResponses.size() > 0) {
				List<MetaAuditSubCatQuestionResponse> auditSubCatQuestions = null;
				Map<?, List<?>> auditSubCatQuestionsMapping = null;
				String categoryIds = Strings.toCSV(subCategoryResponses,"categoryId");
				String[] categoryIdsArray = categoryIds.split(",");
				String categoryIdsTemp = "";
				for(String categoryId:categoryIdsArray) {
					categoryIdsTemp += "'"+categoryId+"',";
				}
				auditSubCatQuestions = mainDao.getMetaAuditSubCategoryQuestionsByCategoryIds(categoryIdsTemp.substring(0, categoryIdsTemp.length()-1));
				auditSubCatQuestionsMapping = (Map<?, List<?>>) CollectionUtil.toMapList(auditSubCatQuestions, "categoryId");
				
				if(auditSubCatQuestions != null && auditSubCatQuestions.size() > 0) {
					List<MetaAuditSubCatQuesOption> subCatQuesOptions = mainDao.getMetaAuditSubCategoryQuesOptions();
					Map<?, List<?>> subCatQuesOptionsMapping = (Map<?, List<?>>) CollectionUtil.toMapList(subCatQuesOptions, "quesId");
					
					for(MetaAuditSubCatQuestionResponse auditSubCatQuestion : auditSubCatQuestions) {
						@SuppressWarnings("unchecked")
						List<MetaAuditSubCatQuesOption> subCatQuesOptionsTemp = ((List<MetaAuditSubCatQuesOption>) subCatQuesOptionsMapping.get(auditSubCatQuestion.getQuesId()));
						if(subCatQuesOptionsTemp != null && subCatQuesOptionsTemp.size() > 0) {
							auditSubCatQuestion.setAuditSubCatQuesOptions(subCatQuesOptionsTemp);
						}
					}
				}
				
				populateJobStepsData(jobId,workId, subCategoryResponses, auditSubCatQuestionsMapping,user.getUserId(),auditId);
				
			}
			
			return subCategoryResponses;
		
	}
	
	private void populateJobStepsData(Long jobId,long workId, List<VendorSubCategoryResponse> subCategoryResponses,
			Map<?, List<?>> auditSubCatQuestionsMapping, long userId, Long auditId) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		//JobAudit jobAudit = apiDao.getJobAuditByJobIdWorkIdAndAuditBy(job.getJobId(),workId,userId);
		
		for(VendorSubCategoryResponse vendorSubCategoryResponse : subCategoryResponses) {
			@SuppressWarnings("unchecked")
			List<MetaAuditSubCatQuestionResponse> auditSubCatQuestionsTemp = ((List<MetaAuditSubCatQuestionResponse>) auditSubCatQuestionsMapping.get(vendorSubCategoryResponse.getSubCategoryId()));
			if(auditSubCatQuestionsTemp != null && auditSubCatQuestionsTemp.size() > 0) {
				vendorSubCategoryResponse.setSubCatQuestions(auditSubCatQuestionsTemp);
			}
			
			JobDetailResponse jobDetail = mainDao.getJobDetailByJobIdAndSubCategoryId(jobId,vendorSubCategoryResponse.getId(),workId);
			vendorSubCategoryResponse.setJobDetail(jobDetail);
			
			JobAuditDetailsResponse jobAuditDetail = mainDao.getJobAuditDetailByAuditIdAndSubCategoryId(auditId,vendorSubCategoryResponse.getId(),vendorSubCategoryResponse.getCategoryId());
			vendorSubCategoryResponse.setAuditDetailsResponse(jobAuditDetail);

			if(jobAuditDetail != null) {
				List<JobDetailFile> jobAuditDetailFiles = mainDao.getJobAuditDetialFiles(jobAuditDetail.getAuditDetId());
				vendorSubCategoryResponse.setJobAuditDetailFiles(jobAuditDetailFiles);
			}
			
			if(jobDetail != null) {
				List<JobDetailFile> jobDetailFiles = mainDao.getJobDetailFilesByJobDetIdAndSubCategoryId(jobDetail.getJobDetId(),vendorSubCategoryResponse.getId());
				vendorSubCategoryResponse.setJobDetailFiles(jobDetailFiles);
				
				if(vendorSubCategoryResponse.getSubCatQuestions() != null && vendorSubCategoryResponse.getSubCatQuestions().size()>0) {
					List<MetaAuditSubCatQuestionResponse> subCatQuestionResponses = vendorSubCategoryResponse.getSubCatQuestions();
					List<JobDetailQuesAns> jobDetailQuesAns = mainDao.getJobDetailQuesAnsByJobDetId(jobDetail.getJobDetId());
					Map<?, List<?>> jobDetailQuesAnsMapping = (Map<?, List<?>>) CollectionUtil.toMapList(jobDetailQuesAns, "quesId");
					
					for(MetaAuditSubCatQuestionResponse subCatQuestionResponse : subCatQuestionResponses) {
						@SuppressWarnings("unchecked")
						List<JobDetailQuesAns> jobDetailQuesAnsTemp = ((List<JobDetailQuesAns>) jobDetailQuesAnsMapping.get(subCatQuestionResponse.getQuesId()));
						if(jobDetailQuesAnsTemp != null && jobDetailQuesAnsTemp.size() > 0) {
							subCatQuestionResponse.setQuesAnswer(jobDetailQuesAnsTemp.get(0));
						}
					}
				}
			}
			
		}
	}

	public JobAuditResponse getJobAuditResponseByAuditId(Long auditId) {
		return mainDao.getJobAuditResponseByAuditId(auditId);
	}

	public void jobAuditDetailsExportIntoExcel(Long jobId, Long workId, Long auditId, HttpServletResponse response) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		UserInfo userInfo = SecurityUtil.getCurrentUserInfo();
		User user = getUserByUserId(userInfo.getUserId());
		List<VendorSubCategoryResponse> auditResponses = getAuditReport(jobId,workId,auditId,user);
		JobResponse jobResponse = getJobResponseByJobId(jobId);
		JobAuditResponse auditResponse = getJobAuditResponseByAuditId(auditId);
		List<ViolationLevel> violationLevels = getAuditViolationLevels(jobResponse.getVendorId(),workId,auditId);
		try {

			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = (HSSFSheet) workbook.createSheet("JobAuditDetails");
			Font f = workbook.createFont();
			f.setBoldweight(Font.BOLDWEIGHT_BOLD);
			f.setFontHeightInPoints((short) 12);
			
			Font f1 = workbook.createFont();
			f1.setBoldweight(Font.BOLDWEIGHT_NORMAL);
			f1.setFontHeightInPoints((short) 12);

			// Cell style for Header row
			CellStyle csHead = workbook.createCellStyle();
			csHead.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			csHead.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			csHead.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			csHead.setBorderRight(XSSFCellStyle.BORDER_THIN);
			csHead.setBorderTop(XSSFCellStyle.BORDER_THIN);
			csHead.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
			csHead.setFont(f);

			CellStyle csCbd = workbook.createCellStyle();
			csCbd.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			csCbd.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			csCbd.setBorderRight(XSSFCellStyle.BORDER_THIN);
			csCbd.setBorderTop(XSSFCellStyle.BORDER_THIN);
			csCbd.setAlignment(XSSFCellStyle.ALIGN_LEFT);
			csCbd.setFont(f);
			
			CellStyle totalCellStyle = workbook.createCellStyle();
			totalCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			totalCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			totalCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
			totalCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
			totalCellStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
			totalCellStyle.setFont(f1);
			
			CellStyle topBottomCellStyle = workbook.createCellStyle();
			topBottomCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			topBottomCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
			topBottomCellStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
			topBottomCellStyle.setFont(f1);
			
			CellStyle topBottomRightCellStyle = workbook.createCellStyle();
			topBottomRightCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			topBottomRightCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
			topBottomRightCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
			topBottomRightCellStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
			topBottomRightCellStyle.setFont(f);
			
			CellStyle boldTopBottomCellStyle = workbook.createCellStyle();
			boldTopBottomCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			boldTopBottomCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
			boldTopBottomCellStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT);
			boldTopBottomCellStyle.setFont(f);
			
			  /* Center Align Cell Contents */
			CellStyle cellStyleCentered = workbook.createCellStyle();
			cellStyleCentered.setAlignment(XSSFCellStyle.ALIGN_CENTER);
			cellStyleCentered.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			cellStyleCentered.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			cellStyleCentered.setBorderTop(XSSFCellStyle.BORDER_THIN);
			cellStyleCentered.setFont(f);
			
			/* Right Align Cell Contents */
			CellStyle cellStyleRight = workbook.createCellStyle();
			cellStyleRight.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
			cellStyleRight.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			cellStyleRight.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			cellStyleRight.setBorderTop(XSSFCellStyle.BORDER_THIN);
			cellStyleRight.setFont(f);
			// Cell style for Category row
			CellStyle cs = workbook.createCellStyle();
			cs.setFillForegroundColor(IndexedColors.CORAL.getIndex());
			cs.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

			// Generate column headings
			int idx = 1;
			Row row0 = sheet.createRow(idx);
			Cell cell00 = row0.createCell(0);
			Cell cell02 = row0.createCell(2);
			Cell cell01 = row0.createCell(1);
			Cell cell03 = row0.createCell(3);
			String auditType = "";
			if(auditResponse.getType() == 1) {
				auditType = "PHYSICAL SITE AUDIT REPORT";
			} else {
				auditType = "REMOTE SITE AUDIT REPORT";
			}
			URL mainLogoUrl = MediaService.class.getResource(constants.getMainLogo());
	        String mainLogoPath = mainLogoUrl.getPath();
			row0.setHeight((short)1100);
			java.io.File file = new java.io.File(mainLogoPath);

			InputStream logo_image1 = new FileInputStream(
					file.getPath());
			 Image image1 = ImageIO.read(logo_image1);

		        BufferedImage bi1 = createResizedCopy(image1, 950, 700, true);
		        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
		        ImageIO.write( bi1, "png", baos1 );
		        baos1.flush();
		       
		        
		        byte[] bytes1 = baos1.toByteArray();
		      
			int picIndex1 = workbook.addPicture(bytes1,
					workbook.PICTURE_TYPE_PNG);
			logo_image1.close();
			HSSFPatriarch drawing1 = sheet.createDrawingPatriarch();
			ClientAnchor my_anchor1 = new HSSFClientAnchor();
			my_anchor1.setCol1(0);
			my_anchor1.setRow1(idx);
			HSSFPicture my_picture1 = drawing1.createPicture(
					my_anchor1, picIndex1);
			
			my_picture1.resize(0.1);
			bi1 = null;
			baos1 = null;
	        logo_image1 = null;
	        image1 = null;
			cell00.setCellStyle(boldTopBottomCellStyle);
			cell01.setCellValue(auditType);
			cell02.setCellStyle(boldTopBottomCellStyle);
			cell01.setCellStyle(cellStyleCentered);
			cell03.setCellStyle(topBottomRightCellStyle);
			sheet.addMergedRegion(new CellRangeAddress(
					idx, //from first row (0-based)
					idx, //to last row  (0-based)
		            1, //from first column (0-based)
		            3  //to last column  (0-based)
		    ));

			idx++;
			
			Row row1 = sheet.createRow(idx);
			Cell cell1 = row1.createCell(0);
			Cell cell2 = row1.createCell(1);
			Cell cell3 = row1.createCell(2);
			Cell cell4 = row1.createCell(3);
			SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat format2 = new SimpleDateFormat("dd MMMM YYYY");
			cell1.setCellValue("Date : "+format2.format(format1.parse(jobResponse.getCreatedTime())));
			cell1.setCellStyle(csCbd);
			//cell2.setCellValue(jobResponse.getCreatedTime());
			cell3.setCellValue("Site ID / Ref No : "+jobResponse.getSiteId());
			cell3.setCellStyle(cellStyleRight);
			//cell4.setCellValue(jobResponse.getSiteId());
			cell2.setCellStyle(topBottomCellStyle);
			cell4.setCellStyle(topBottomRightCellStyle);
			sheet.addMergedRegion(new CellRangeAddress(
					idx, //from first row (0-based)
					idx, //to last row  (0-based)
		            0, //from first column (0-based)
		            1  //to last column  (0-based)
		    ));
			sheet.addMergedRegion(new CellRangeAddress(
					idx, //from first row (0-based)
					idx, //to last row  (0-based)
		            2, //from first column (0-based)
		            3  //to last column  (0-based)
		    ));
			idx++;
			
			Row row2 = sheet.createRow(idx);
			Cell cell21 = row2.createCell(0);
			Cell cell22 = row2.createCell(1);
			Cell cell23 = row2.createCell(2);
			Cell cell24 = row2.createCell(3);
			cell21.setCellValue("Location : "+jobResponse.getSite());
			cell21.setCellStyle(csCbd);
			//cell22.setCellValue(jobResponse.getSite());
			cell23.setCellValue("Auditor Name : "+auditResponse.getAuditedByName());
			cell23.setCellStyle(cellStyleRight);
			//cell24.setCellValue(auditResponse.getAuditedByName());
			cell22.setCellStyle(topBottomCellStyle);
			cell24.setCellStyle(topBottomRightCellStyle);
			sheet.addMergedRegion(new CellRangeAddress(
					idx, //from first row (0-based)
					idx, //to last row  (0-based)
		            0, //from first column (0-based)
		            1  //to last column  (0-based)
		    ));
			sheet.addMergedRegion(new CellRangeAddress(
					idx, //from first row (0-based)
					idx, //to last row  (0-based)
		            2, //from first column (0-based)
		            3  //to last column  (0-based)
		    ));
			idx++;
			
			Row row3 = sheet.createRow(idx);
			Cell cell31 = row3.createCell(0);
			Cell cell32 = row3.createCell(1);
			Cell cell33 = row3.createCell(2);
			Cell cell34 = row3.createCell(3);
			cell31.setCellValue("Client	: "+jobResponse.getCustomerName());
			cell31.setCellStyle(topBottomRightCellStyle);
			//cell32.setCellValue(jobResponse.getCustomerName());
			cell32.setCellStyle(topBottomCellStyle);
			cell33.setCellStyle(topBottomCellStyle);
			cell34.setCellStyle(topBottomRightCellStyle);
			sheet.addMergedRegion(new CellRangeAddress(
					idx, //from first row (0-based)
					idx, //to last row  (0-based)
		            0, //from first column (0-based)
		            3  //to last column  (0-based)
		    ));
			idx++;
			
			Row row4 = sheet.createRow(idx);
			Cell cell41 = row4.createCell(0);
			Cell cell42 = row4.createCell(1);
			Cell cell43 = row4.createCell(2);
			Cell cell44 = row4.createCell(3);
			cell41.setCellValue("Activity Carried Out : "+jobResponse.getActivityTypeName());
			cell41.setCellStyle(topBottomRightCellStyle);
			cell42.setCellStyle(topBottomCellStyle);
			//cell42.setCellValue(jobResponse.getActivityTypeName());
			cell43.setCellStyle(topBottomCellStyle);
			cell44.setCellStyle(topBottomRightCellStyle);
			sheet.addMergedRegion(new CellRangeAddress(
					idx, //from first row (0-based)
					idx, //to last row  (0-based)
		            0, //from first column (0-based)
		            3  //to last column  (0-based)
		    ));
			idx++;
			
			Row row5 = sheet.createRow(idx);
			Cell cell51 = row5.createCell(0);
			Cell cell52 = row5.createCell(1);
			Cell cell53 = row5.createCell(2);
			Cell cell54 = row5.createCell(3);
			cell51.setCellValue("Team Details : "+jobResponse.getAssignedToName()+" (engg) ,"+jobResponse.getVendorName()+" (vendor) ");
			cell51.setCellStyle(topBottomRightCellStyle);
			cell52.setCellStyle(topBottomCellStyle);
			//cell52.setCellValue(jobResponse.getAssignedToName()+" (engg) ,"+jobResponse.getVendorName()+" (vendor) ");
			cell53.setCellStyle(topBottomCellStyle);
			cell54.setCellStyle(topBottomRightCellStyle);
			sheet.addMergedRegion(new CellRangeAddress(
					idx, //from first row (0-based)
					idx, //to last row  (0-based)
		            0, //from first column (0-based)
		            3  //to last column  (0-based)
		    ));
			idx++;
			
			Row row6 = sheet.createRow(idx);
			Cell cell61 = row6.createCell(0);
			Cell cell62 = row6.createCell(1);
			Cell cell63 = row6.createCell(2);
			Cell cell64 = row6.createCell(3);
			cell61.setCellValue("S.No");
			cell61.setCellStyle(csCbd);
			cell62.setCellValue("Observations");
			cell62.setCellStyle(csCbd);
			cell63.setCellValue("Photographs");
			cell63.setCellStyle(csCbd);
			cell64.setCellValue("Remarks / Violation Level");
			cell64.setCellStyle(csCbd);
			idx++;
			int i=1;
			for (VendorSubCategoryResponse auditResponseTemp : auditResponses) {
				Row row7 = sheet.createRow(idx);
				row7.setHeight((short)1200);
				Cell cell71 = row7.createCell(0);
				Cell cell72 = row7.createCell(1);
				Cell cell73 = row7.createCell(2);
				Cell cell74 = row7.createCell(3);
				cell71.setCellStyle(totalCellStyle);
				cell72.setCellStyle(totalCellStyle);
				cell73.setCellStyle(totalCellStyle);
				cell74.setCellStyle(totalCellStyle);
				cell71.setCellValue(i);
				cell72.setCellValue(auditResponseTemp.getCategoryName());
				List<JobDetailFile> jobAuditDetailFiles = auditResponseTemp.getJobAuditDetailFiles();
				List<AppFile> appFiles = null;
				if(jobAuditDetailFiles != null && jobAuditDetailFiles.size() > 0) {
					String fileIds = Strings.toCSV(jobAuditDetailFiles,"fileId");
					appFiles = mainDao.getAppFilesByFileIds(fileIds);
					if (appFiles != null) {
						for (AppFile appFile : appFiles) {
							if (appFile != null) {

								String mediaSotragePath = constants.getMediaPath()
										+ File.separator + appFile.getFilePath();
								
								try {
									InputStream logo_image = new FileInputStream(
											mediaSotragePath);
									Image image = ImageIO.read(logo_image);
									
							        BufferedImage bi = createResizedCopy(image, 640, 800, true);
							        ByteArrayOutputStream baos = new ByteArrayOutputStream();
							        ImageIO.write( bi, "png", baos );
							        baos.flush();
							       
							        
							        byte[] bytes = baos.toByteArray();
							      
								int picIndex = workbook.addPicture(bytes,
										workbook.PICTURE_TYPE_PNG);
								logo_image.close();
								CreationHelper helper = sheet.getWorkbook().getCreationHelper();
								HSSFPatriarch drawing = sheet.createDrawingPatriarch();
								ClientAnchor my_anchor = helper.createClientAnchor();
								my_anchor.setAnchorType(my_anchor.MOVE_AND_RESIZE);
								my_anchor.setCol1(2);
								my_anchor.setRow1(idx);
								
								HSSFPicture my_picture = drawing.createPicture(
										my_anchor, picIndex);
								
								my_picture.resize(0.1);
									bi = null;
									baos = null;
							        logo_image = null;
							        image = null;
								} catch (Exception e) {
									System.out.println(e);
								}
							}
						}
					}
				}
				
				cell74.setCellValue(auditResponseTemp.getViolationLevelName());
				idx++;
				i++;
			}
			
			int tempIdx = 2;
			Row rowTemp2 = sheet.getRow(tempIdx);
			if(rowTemp2 != null) {
				Cell cell5 = rowTemp2.createCell(5);
				Cell cell6 = rowTemp2.createCell(6);
				Cell cell7 = rowTemp2.createCell(7);
				Cell cell8 = rowTemp2.createCell(8);
				cell5.setCellStyle(csHead);
				cell6.setCellStyle(csHead);
				cell7.setCellStyle(csHead);
				cell8.setCellStyle(csHead);
				cell5.setCellValue("OHS NON CONERMANCE COUNTS");
				sheet.addMergedRegion(new CellRangeAddress(
						tempIdx, //from first row (0-based)
						tempIdx, //to last row  (0-based)
			            5, //from first column (0-based)
			            8  //to last column  (0-based)
			    ));
			} else {
				rowTemp2 = sheet.createRow(tempIdx);
				Cell cell5 = rowTemp2.createCell(5);
				Cell cell6 = rowTemp2.createCell(6);
				Cell cell7 = rowTemp2.createCell(7);
				Cell cell8 = rowTemp2.createCell(8);
				cell5.setCellStyle(csHead);
				cell6.setCellStyle(csHead);
				cell7.setCellStyle(csHead);
				cell8.setCellStyle(csHead);
				cell5.setCellValue("OHS NON CONERMANCE COUNTS");
				sheet.addMergedRegion(new CellRangeAddress(
						tempIdx, //from first row (0-based)
						tempIdx, //to last row  (0-based)
			            5, //from first column (0-based)
			            8  //to last column  (0-based)
			    ));
			}
			tempIdx++;
			
			for (ViolationLevel violationLevel : violationLevels) {
				Row rowTemp = sheet.getRow(tempIdx);
				if(rowTemp != null) {
					Cell cell5 = rowTemp.createCell(5);
					Cell cell6 = rowTemp.createCell(6);
					Cell cell7 = rowTemp.createCell(7);
					Cell cell8 = rowTemp.createCell(8);
					cell5.setCellStyle(csHead);
					cell6.setCellStyle(csHead);
					cell7.setCellStyle(csHead);
					cell8.setCellStyle(csHead);
					cell5.setCellValue(violationLevel.getLevelName());
					cell6.setCellValue(Integer.parseInt(violationLevel.getLevelCount()) == 0 ? "NIL" : violationLevel.getLevelCount());
					sheet.addMergedRegion(new CellRangeAddress(
							tempIdx, //from first row (0-based)
							tempIdx, //to last row  (0-based)
				            6, //from first column (0-based)
				            8  //to last column  (0-based)
				    ));
				} else {
					rowTemp = sheet.createRow(tempIdx);
					Cell cell5 = rowTemp.createCell(5);
					Cell cell6 = rowTemp.createCell(6);
					Cell cell7 = rowTemp.createCell(7);
					Cell cell8 = rowTemp.createCell(8);
					cell5.setCellStyle(csHead);
					cell6.setCellStyle(csHead);
					cell7.setCellStyle(csHead);
					cell8.setCellStyle(csHead);
					cell5.setCellValue(violationLevel.getLevelName());
					cell6.setCellValue(Integer.parseInt(violationLevel.getLevelCount()) == 0 ? "NIL" : violationLevel.getLevelCount());
					sheet.addMergedRegion(new CellRangeAddress(
							tempIdx, //from first row (0-based)
							tempIdx, //to last row  (0-based)
				            6, //from first column (0-based)
				            8  //to last column  (0-based)
				    ));
				}
				tempIdx++;
			}
			// Adjust column width according to data
			for (int j = 0; j < 4; j++) {
				sheet.autoSizeColumn(j);
			}

			response.setHeader(
					"Content-Disposition",
					"attachment; filename=\"Job_Audit_Details_"
							+ System.currentTimeMillis() + ".xls\";");
			// response.setHeader("Content-Length", workbook. + "");

			workbook.write(response.getOutputStream());

		} catch (Exception e) {
			System.out.println(e);
		}

	}
	
	static BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha) {
	    int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
	    BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
	    Graphics2D g = scaledBI.createGraphics();
	    if (preserveAlpha) {
	        g.setComposite(AlphaComposite.Src);
	    }
	    g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
	    g.dispose();
	    return scaledBI;
	}

	public List<ViolationLevel> getAuditViolationLevels(Long vendorId, Long workId, Long auditId) {
		return mainDao.getAuditViolationLevels(vendorId,workId,auditId);
	}

	public String uploadExcel(CommonsMultipartFile fileUpload) {
		 File inputWorkbook = null;
		try {
			String tempDirPath = constants.getMediaPath() + File.separator
					+ File.separator + File.separator;
			inputWorkbook = new File(tempDirPath);
			if (!inputWorkbook.exists()) {
				inputWorkbook.mkdirs();
			}

			FileItem fileItem = fileUpload.getFileItem();
			String fullPath = tempDirPath + File.separator
					+ System.currentTimeMillis() + "."
					+ FilenameUtils.getExtension(fileItem.getName());
			inputWorkbook = new File(fullPath);
			fileItem.write(inputWorkbook);

			return fullPath;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public boolean addUserBulkUpload(String fullPath, long userId) throws Exception {
		List<UserResponse> users = userBulkUpload(fullPath);
		boolean err = validateUsers(users);
		User loggedInuser = mainDao.getUserByUserId(userId);
		if(!err) {
			addBulkUsers(users,loggedInuser,userId);
		}
		return err;
	}
	
	@Async
	private void addBulkUsers(List<UserResponse> users, User loggedInuser, long userId) {
		for (UserResponse userResponse : users) {
			userResponse.setCreatedBy(userId);
			addUser(userResponse.getUser(),loggedInuser, userResponse.getCircleIds(), userResponse.getVendorIds(), userResponse.getCustomerIds());
		}
	}

	private boolean validateUsers(List<UserResponse> users) {
		boolean err = false;
		for (UserResponse userResponse : users) {
			if(Strings.isEmpty(userResponse.getName()) || Strings.isEmpty(userResponse.getMobile()) || Strings.isEmpty(userResponse.getAuthority())) {
				err = true;
			}
			
			if(!Strings.isEmpty(userResponse.getEmail())) {
				String EMAIL_REGEX ="^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"; //includes domain name type email validation also
			      
			      if(!Strings.isEmpty(userResponse.getEmail()) && !userResponse.getEmail().matches(EMAIL_REGEX)) {
			    	  err = true;
			      }
			}
		}
		
		return err;
	}

	@Async
	private List<UserResponse> userBulkUpload(String pathname) throws Exception {
			try {

				File inputWorkbook = new File(pathname);
				Workbook wb = WorkbookFactory.create(inputWorkbook);
				if (wb instanceof HSSFWorkbook) {
					wb = (HSSFWorkbook) wb;
				} else if (wb instanceof XSSFWorkbook) {
					wb = (XSSFWorkbook) wb;
				}

				Sheet sheet = null;

				if (wb instanceof HSSFWorkbook) {
					sheet = (HSSFSheet) wb.getSheetAt(0);
				} else if (wb instanceof XSSFWorkbook) {
					sheet = (XSSFSheet) wb.getSheetAt(0);
				}

				return processUserBulkUpload(sheet, wb);

				

			} catch (Exception e) {
				e.printStackTrace();

				throw e;
			}
	}

	private List<UserResponse> processUserBulkUpload(Sheet sheet, Workbook wb) {
			List<UserResponse> users = new ArrayList<UserResponse>();
			if (sheet != null) {
				Iterator<Row> rows = sheet.rowIterator();

				int indexVal = 0;

				while (rows.hasNext()) {

					Row row = rows.next();

					System.out.println("Row No.: " + row.getRowNum());
					// once get a row its time to iterate through cells.
					Iterator<Cell> cells = row.cellIterator();
					int i = row.getRowNum();

					if (i == 0) {
						continue;
					}

					if (CellUtil.getCell(row, indexVal).getCellType() != Cell.CELL_TYPE_BLANK) {

						UserResponse user = new UserResponse();

						while (cells.hasNext()) {
							Cell cell = null;
							if (wb instanceof HSSFWorkbook) {
								cell = (HSSFCell) cells.next();
							} else if (wb instanceof XSSFWorkbook) {
								cell = (XSSFCell) cells.next();

							}

							int j = cell.getColumnIndex();
							if (j > 21) {
								break;
							}

							System.out.println("Row Column No.: " + i + " " + j);
							String content = Excel.getCellContent(cell);

							switch (j) {
							case 0:
								if (!Strings.isEmpty(content)) {
									user.setName(content);
								}  
								break;

							case 1:
								if (!Strings.isEmpty(content)) {
									user.setEmail(content);
								}
								break;
							case 2:
								if (!Strings.isEmpty(content)) {
									user.setMobile(content);
								}
								break;	
							case 3:
								if (!Strings.isEmpty(content)) {
									user.setAuthority(content);
								}
								break;
							case 4:
								if (!Strings.isEmpty(content)) {
									user.setVendorIds(content);
								}
								break;
							case 5:
								if (!Strings.isEmpty(content)) {
									user.setCustomerIds(content);
								}
								break;
							case 6:
								if (!Strings.isEmpty(content)) {
									user.setActivityGroup(Integer.parseInt(content));
								}
								break;
							case 7:
								if (!Strings.isEmpty(content)) {
									user.setCircleIds(content);
								}
								break;
							}

						}
						users.add(user);

					} else {
						if (CellUtil.getCell(row, indexVal).getCellType() == Cell.CELL_TYPE_BLANK) {
							if (i >= 8) {
								break;
							}
						}
						 
					}
					
				}

			}
			return users;
			
	}

	public boolean addJobBulkUpload(String fullPath, long userId) throws Exception {
		List<Job> jobs = jobBulkUpload(fullPath);
		boolean err = validateJobs(jobs);
		if(!err) {
			addBulkJobs(userId, jobs);
		}
		return err;
	}

	@Async
	private void addBulkJobs(long userId, List<Job> jobs) {
		for (Job job : jobs) {
			job.setCreatedBy(userId);
			addJob(job, userId);
		}
	}

	private boolean validateJobs(List<Job> jobs) throws Exception {
		boolean err = false;
		
		for (Job job : jobs) {
			if(job.getSiteId() == null || job.getSiteId() == 0 || job.getActivityType() == null || job.getActivityType() == 0 || job.getVendorId() == null
					|| job.getVendorId() == 0 || job.getCustomerId() == null || job.getCustomerId() == 0 || Strings.isEmpty(job.getDueDate()) || job.getAssignedTo() == null
					|| job.getAssignedTo() == 0) {
				err = true;
			}
		}
		
		return err;
		
	}

	private List<Job> jobBulkUpload(String pathname) throws Exception {
			try {

				File inputWorkbook = new File(pathname);
				Workbook wb = WorkbookFactory.create(inputWorkbook);
				if (wb instanceof HSSFWorkbook) {
					wb = (HSSFWorkbook) wb;
				} else if (wb instanceof XSSFWorkbook) {
					wb = (XSSFWorkbook) wb;
				}

				Sheet sheet = null;

				if (wb instanceof HSSFWorkbook) {
					sheet = (HSSFSheet) wb.getSheetAt(0);
				} else if (wb instanceof XSSFWorkbook) {
					sheet = (XSSFSheet) wb.getSheetAt(0);
				}

				return processJobBulkUpload(sheet, wb);

				

			} catch (Exception e) {
				e.printStackTrace();

				throw e;
			}
	}
	
	private List<Job> processJobBulkUpload(Sheet sheet, Workbook wb) {
		List<Job> jobs = new ArrayList<Job>();
		if (sheet != null) {
			Iterator<Row> rows = sheet.rowIterator();

			int indexVal = 1;

			while (rows.hasNext()) {

				Row row = rows.next();

				System.out.println("Row No.: " + row.getRowNum());
				// once get a row its time to iterate through cells.
				Iterator<Cell> cells = row.cellIterator();
				int i = row.getRowNum();

				if (i == 0) {
					continue;
				}

				if (CellUtil.getCell(row, indexVal).getCellType() != Cell.CELL_TYPE_BLANK) {

					Job job = new Job();

					while (cells.hasNext()) {
						Cell cell = null;
						if (wb instanceof HSSFWorkbook) {
							cell = (HSSFCell) cells.next();
						} else if (wb instanceof XSSFWorkbook) {
							cell = (XSSFCell) cells.next();

						}

						int j = cell.getColumnIndex();
						if (j > 21) {
							break;
						}

						System.out.println("Row Column No.: " + i + " " + j);
						String content = Excel.getCellContent(cell);

						switch (j) {
						case 0:
							if (!Strings.isEmpty(content)) {
								job.setSiteId(Long.parseLong(content));
							}  
							break;

						case 1:
							if (!Strings.isEmpty(content)) {
								job.setActivityType(Integer.parseInt(content));
							}  
							break;
						case 2:
							if (!Strings.isEmpty(content)) {
								job.setVendorId(Long.parseLong(content));
							} 
							break;	
						case 3:
							if (!Strings.isEmpty(content)) {
								job.setCustomerId(Long.parseLong(content));
							} 
							break;
						case 4:
							if (!Strings.isEmpty(content)) {
								job.setDueDate(content);
							} 
							break;
						case 5:
							if (!Strings.isEmpty(content)) {
								job.setAssignedTo(Long.parseLong(content));
							} 
							break;
						case 6:
							if (!Strings.isEmpty(content)) {
								job.setNote(content);
							} 
							break;
						}

					}
					jobs.add(job);

				} else {
					if (CellUtil.getCell(row, indexVal).getCellType() == Cell.CELL_TYPE_BLANK) {
						if (i >= 3) {
							break;
						}
					}
					 
				}
				
			}

		}
		return jobs;
		
}

	public List<Vendor> getUserVendors(long userId) {
		return mainDao.getUserVendors(userId);
	}
	
	public List<VendorName> getUserVendorsName(long userId) {
		return mainDao.getUserVendorsName(userId);
	}

	public List<Operator> getUserCustomers(long userId) {
		return mainDao.getUserCustomers(userId);
	}

	public List<User> getCoordinators() {
		return mainDao.getCoordinators(MainService.ROLE_COORDINATOR);
	}
	public List<User> getTechnician() {
		return mainDao.getTechnician(MainService.ROLE_TECHNICIAN);
	}

	public List<User> getUserEngineers(long userId) {
		return mainDao.getUserEngineers(userId);
	}

	public List<User> getEngineersByVendorIdAndCustomerId(Long vendorId, Long customerId) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		List<User> vBasedEngineers = mainDao.getEngineersByVendorId(vendorId);
		return mainDao.getEngineersByCustomerIdAndUserIds(customerId,(vBasedEngineers != null && vBasedEngineers.size() > 0) ? Strings.toCSV(vBasedEngineers,"userId") : ""+0);
	}

	public void downloadUserTemplate(HttpServletResponse response) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		 
		String headers[] = {"Name","Email","Mobile","Role","Vendor","Customer","Activity Group","Circle"};
		
		workbook = Excel.generate("User Template", headers, null, null, 0);
		response.setHeader(
			"Content-Disposition",
			"attachment; filename=\"Pyro_Safety_App_"
					+ System.currentTimeMillis() + ".xls\";");
		// response.setHeader("Content-Length", workbook. + "");

		workbook.write(response.getOutputStream());
	}

	public void downloadJobTemplate(HttpServletResponse response) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		 
		String headers[] = {"Site","Activity Type","Vendor","Customer","Due Date","Assigned To","Note"};
		
		workbook = Excel.generate("Job Template", headers, null, null, 0);
		response.setHeader(
			"Content-Disposition",
			"attachment; filename=\"Pyro_Safety_App_"
					+ System.currentTimeMillis() + ".xls\";");
		// response.setHeader("Content-Length", workbook. + "");

		workbook.write(response.getOutputStream());
	}
}