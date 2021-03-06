/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.*;
import io.litmusblox.server.service.*;
import io.litmusblox.server.service.impl.ml.RolePredictionBean;
import io.litmusblox.server.utils.RestClient;
import io.litmusblox.server.utils.SentryUtil;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.naming.OperationNotSupportedException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Implementation class for JobService
 *
 * @author : Shital Raval
 * Date : 2/7/19
 * Time : 9:47 AM
 * Class Name : JobService
 * Project Name : server
 */
@Service
@Log4j2

public class JobService implements IJobService {

    @Resource
    JobRepository jobRepository;

    @Resource
    CompanyRepository companyRepository;

    @Resource
    UserRepository userRepository;

    @Resource
    JobScreeningQuestionsRepository jobScreeningQuestionsRepository;

    @Resource
    JobCandidateMappingRepository jobCandidateMappingRepository;

    @Resource
    TempSkillsRepository tempSkillsRepository;

    @Resource
    JobKeySkillsRepository jobKeySkillsRepository;

    @Resource
    JobCapabilitiesRepository jobCapabilitiesRepository;

    @Resource
    SkillMasterRepository skillMasterRepository;

    @Resource
    MasterDataRepository masterDataRepository;

    @Resource
    CompanyAddressRepository companyAddressRepository;

    @Resource
    CompanyBuRepository companyBuRepository;

    @Resource
    CompanyStageStepRepository companyStageStepRepository;

    @Resource
    JobHiringTeamRepository jobHiringTeamRepository;

    @Resource
    JcmCommunicationDetailsRepository jcmCommunicationDetailsRepository;

    @Resource
    WeightageCutoffByCompanyMappingRepository weightageCutoffByCompanyMappingRepository;

    @Resource
    WeightageCutoffMappingRepository weightageCutoffMappingRepository;

    @Resource
    JcmProfileSharingDetailsRepository jcmProfileSharingDetailsRepository;

    @Autowired
    IScreeningQuestionService screeningQuestionService;

    @Resource
    JobHistoryRepository jobHistoryRepository;

    @Resource
    JobCapabilityStarRatingMappingRepository jobCapabilityStarRatingMappingRepository;

    @Resource
    CvRatingRepository cvRatingRepository;

    @Resource
    JobStageStepRepository jobStageStepRepository;

    @Value("${mlApiUrl}")
    private String mlUrl;

    @Value("${scoringEngineBaseUrl}")
    private String scoringEngineBaseUrl;

    @Value("${scoringEngineAddJobUrlSuffix}")
    private String scoringEngineAddJobUrlSuffix;

    @Transactional
    public Job addJob(Job job, String pageName) throws Exception {//add job with respective pageName

        if(null != job.getStatus() && job.getStatus().equals(IConstant.JobStatus.ARCHIVED))
            throw new ValidationException("Can't edit job because job in Archived state", HttpStatus.UNPROCESSABLE_ENTITY);

        User recruiter = null;
        User hiringManager = null;
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        log.info("Received request to add job for page " + pageName + " from user: " + loggedInUser.getEmail());
        long startTime = System.currentTimeMillis();

        if (pageName.equalsIgnoreCase(IConstant.AddJobPages.capabilities.name())) {
            //delete all existing records in the job_capability_star_rating_mapping table for the current job
            jobCapabilityStarRatingMappingRepository.deleteByJobId(job.getId());
            jobCapabilityStarRatingMappingRepository.flush();
        }
        Job oldJob = null;

        if (null != job.getId()) {
            //get handle to existing job object
            oldJob = jobRepository.findById(job.getId()).orElse(null);
           // oldJob = tempJobObj.isPresent() ? tempJobObj.get() : null;
        }

        //set recruiter
        if(null != job.getRecruiter() && null != job.getRecruiter().getId()){
            recruiter =  userRepository.findById(job.getRecruiter().getId()).orElse(null);
            if(null != recruiter)
                job.setRecruiter(recruiter);
        }

        //set hiringManager
        if(null != job.getHiringManager() && null != job.getHiringManager().getId()){
            hiringManager =  userRepository.findById(job.getHiringManager().getId()).orElse(null);
            if(null != hiringManager)
                job.setHiringManager(hiringManager);
        }

        switch (IConstant.AddJobPages.valueOf(pageName)) {
            case overview:
                addJobOverview(job, oldJob, loggedInUser);
                break;
            case screeningQuestions:
                addJobScreeningQuestions(job, oldJob, loggedInUser);
                break;
            case keySkills:
                addJobKeySkills(job, oldJob, loggedInUser);
                break;
            case capabilities:
                addJobCapabilities(job, oldJob, loggedInUser);
                break;
            case jobDetail:
                addJobDetail(job, oldJob, loggedInUser);
                break;
            case hiringTeam:
                addJobHiringTeam(job, oldJob, loggedInUser);
                job.setJobHiringTeamList(jobHiringTeamRepository.findByJobId(job.getId()));
                break;
            case expertise:
                addJobExpertise(job, oldJob);
                break;
            default:
                throw new OperationNotSupportedException("Unknown page: " + pageName);
        }

        populateDataForNextPage(job, pageName);

        log.info("Completed processing request to add job in " + (System.currentTimeMillis() - startTime) + "ms");
        return job;
    }

    private void populateDataForNextPage(Job job, String pageName) throws Exception {
        int currentPageIndex = MasterDataBean.getInstance().getJobPageNamesInOrder().indexOf(pageName);
        if (currentPageIndex != -1) {
            if(MasterDataBean.getInstance().getJobPageNamesInOrder().get(currentPageIndex).equals(IConstant.AddJobPages.overview.name())) {
                log.info("Setting jobkeyskills in job object");
                job.setJobKeySkillsList(jobKeySkillsRepository.findByJobId(job.getId()));
                log.info("Setting capabilities in job object");
                job.setJobCapabilityList(jobCapabilitiesRepository.findByJobId(job.getId()));
            }
            switch (IConstant.AddJobPages.valueOf(MasterDataBean.getInstance().getJobPageNamesInOrder().get(currentPageIndex+1))) {
                case keySkills:
                    //populate key skills for the job
                    job.setJobKeySkillsList(jobKeySkillsRepository.findByJobId(job.getId()));
                    break;
                case capabilities:
                    //populate the capabilities for the job
                    job.setJobCapabilityList(jobCapabilitiesRepository.findByJobId(job.getId()));
                    break;
                /*case hiringTeam:
                    job.setJobHiringTeamList(jobHiringTeamRepository.findByJobId(job.getId()));
                    break;*/
                default:
                    break;
            }
        }
    }


    /**
     * Fetch details of currently logged in user and
     * query the repository to find the list of all jobs
     *
     * @param archived flag indicating if only archived jobs need to be fetched
     * @param companyName name of the company for which jobs have to be found
     * @return List of jobs created by the logged in user
     */
    @Transactional(readOnly = true)
    public JobWorspaceResponseBean findAllJobsForUser(boolean archived, String companyName) throws Exception {

        log.info("Received request to request to find all jobs for user for archived = " + archived);
        long startTime = System.currentTimeMillis();

        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JobWorspaceResponseBean responseBean = new JobWorspaceResponseBean();
        String msg = loggedInUser.getEmail() + ", " + companyName + ": ";

        List<Company> companyList = new ArrayList<>();
        switch(loggedInUser.getRole()) {
            case IConstant.UserRole.Names.CLIENT_ADMIN:
                log.info(msg + "Request from Client Admin, all jobs for the company will be returned");
                companyList = new ArrayList<>();
                companyList.add(loggedInUser.getCompany());
                jobsForCompany(responseBean, archived, companyList);
                break;
            case IConstant.UserRole.Names.RECRUITMENT_AGENCY:
                if (Util.isNull(companyName))
                    throw new ValidationException("Missing Company name in request", HttpStatus.UNPROCESSABLE_ENTITY);
                Company company = companyRepository.findByCompanyNameIgnoreCaseAndCompanyType(companyName, IConstant.CompanyType.AGENCY.getValue());
                if(null == company)
                    throw new ValidationException("Recruitment agency not found for : "+companyName, HttpStatus.UNPROCESSABLE_ENTITY);
                List<Company> companies = companyRepository.findByRecruitmentAgencyId(company.getId());
                jobsForCompany(responseBean, archived, companies);
                break;
            case IConstant.UserRole.Names.SUPER_ADMIN:
                if (Util.isNull(companyName))
                    throw new ValidationException("Missing Company name in request", HttpStatus.UNPROCESSABLE_ENTITY);
                log.info(msg + "Request from Super Admin for jobs of Company");
                Company companyObjToUse = companyRepository.findByCompanyNameIgnoreCaseAndRecruitmentAgencyIdIsNull(companyName);
                companyList.add(companyObjToUse);
                if (null == companyObjToUse)
                    throw new ValidationException("Company not found : " + companyName, HttpStatus.UNPROCESSABLE_ENTITY);
                jobsForCompany(responseBean, archived, companyList);
                break;
            default:
                jobsForLoggedInUser(responseBean, archived, loggedInUser);
        }
        log.info(msg + "Completed processing request to find all jobs for user in " + (System.currentTimeMillis() - startTime) + "ms");
        return responseBean;
    }

    private void jobsForLoggedInUser(JobWorspaceResponseBean responseBean, boolean archived, User loggedInUser) {
        long startTime = System.currentTimeMillis();
        if (archived) {
            responseBean.setListOfJobs(jobRepository.findByCreatedByAndDateArchivedIsNotNullOrderByCreatedOnDesc(loggedInUser));
            responseBean.setArchivedJobs(responseBean.getListOfJobs().size());
            responseBean.setOpenJobs((jobRepository.countByCreatedByAndDateArchivedIsNull(loggedInUser)).intValue());
        } else {
            responseBean.setListOfJobs(jobRepository.findByCreatedByAndDateArchivedIsNullOrderByCreatedOnDesc(loggedInUser));
            responseBean.setOpenJobs(responseBean.getListOfJobs().size());
            responseBean.setArchivedJobs((jobRepository.countByCreatedByAndDateArchivedIsNotNull(loggedInUser)).intValue());
        }
        log.info("Got " + responseBean.getListOfJobs().size() + " jobs in " + (System.currentTimeMillis() - startTime) + "ms");
        getCandidateCountByStage(responseBean.getListOfJobs());
    }

    private void jobsForCompany(JobWorspaceResponseBean responseBean, boolean archived, List<Company> companyList) {
        long startTime = System.currentTimeMillis();
        companyList.stream().forEach(company -> {
            if (archived) {
                responseBean.setListOfJobs(jobRepository.findByCompanyIdAndDateArchivedIsNotNullOrderByCreatedOnDesc(company));
                responseBean.setArchivedJobs(responseBean.getListOfJobs().size());
                responseBean.setOpenJobs((jobRepository.countByCompanyIdAndDateArchivedIsNull(company)).intValue());
            } else {
                responseBean.setListOfJobs(jobRepository.findByCompanyIdAndDateArchivedIsNullOrderByCreatedOnDesc(company));
                responseBean.setOpenJobs(responseBean.getListOfJobs().size());
                responseBean.setArchivedJobs((jobRepository.countByCompanyIdAndDateArchivedIsNotNull(company)).intValue());
            }
        });
        log.info("Got " + responseBean.getListOfJobs().size() + " jobs in " + (System.currentTimeMillis() - startTime) + "ms");
        getCandidateCountByStage(responseBean.getListOfJobs());
    }

    private void getCandidateCountByStage(List<Job> jobs) {
        if(jobs != null & jobs.size() > 0) {
            long startTime = System.currentTimeMillis();
            //Converting list of jobs into map, so each job is available by key
            Map<Long, Job> jobsMap = jobs.stream().collect(Collectors.toMap(Job::getId, Function.identity()));
            log.info("Getting candidate count for " + jobs.size() + " jobs");
            try {
                List<Long> jobIds = new ArrayList<>();
                jobIds.addAll(jobsMap.keySet());
                //get counts by stage for ALL job ids in 1 db call
                List<Object[]> stageCountList = jobCandidateMappingRepository.findCandidateCountByStageJobIds(jobIds);
                //Format results in a map<jobId, resultset>
                Map<Long, List<Object[]>> stageCountMapByJobId = stageCountList.stream().collect(groupingBy(obj -> ((Integer) obj[0]).longValue()));
                log.info("Got stageCountByJobIds, row count: " + stageCountMapByJobId.size());
                //Loop through map to assign count by stage for each job
                stageCountMapByJobId.forEach((key, value) -> {
                    Job job = jobsMap.get(key);
                    value.stream().forEach(objArray -> {
                        job.getCandidateCountByStage().put(objArray[1].toString(), ((BigInteger) objArray[2]).intValue());
                    });
                });
                log.info("Got candidate count by stage for " + jobs.size() + " jobs in " + (System.currentTimeMillis() - startTime) + "ms");
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    /**
     * For the specified job, retrieve
     * 1. list candidates for job for specified stage
     * 2. count of candidates by each stage
     *
     * @return response bean with all details
     * @throws Exception
     */
    @Transactional
    public SingleJobViewResponseBean getJobViewById(Long jobId, String stage) throws Exception {
        log.info("Received request to request to find a list of all candidates for job: {} and stage {} ",jobId, stage);
        long startTime = System.currentTimeMillis();
        //If the job is not published, do not process the request
        Job job = jobRepository.getOne(jobId);


        if (null == job) {
            StringBuffer info = new StringBuffer("Invalid job id ").append(jobId);
            log.info(info.toString());
            Map<String, String> breadCrumb = new HashMap<>();
            breadCrumb.put("Job Id ",jobId.toString());
            breadCrumb.put("detail", info.toString());
            throw new WebException("Invalid job id " + jobId,  HttpStatus.UNPROCESSABLE_ENTITY, breadCrumb);
        }
        else {
            User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(!loggedInUser.getRole().equals(IConstant.UserRole.Names.SUPER_ADMIN) && !job.getCompanyId().getId().equals(loggedInUser.getCompany().getId()))
                throw new WebException(IErrorMessages.JOB_COMPANY_MISMATCH, HttpStatus.UNAUTHORIZED);
            if(job.getStatus().equals(IConstant.JobStatus.DRAFT.getValue())) {
                StringBuffer info = new StringBuffer(IErrorMessages.JOB_NOT_LIVE).append(job.getStatus());
                log.info(info.toString());
                Map<String, String> breadCrumb = new HashMap<>();
                breadCrumb.put("Job Id", job.getId().toString());
                breadCrumb.put("detail", info.toString());
                throw new WebException(IErrorMessages.JOB_NOT_LIVE, HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }

        SingleJobViewResponseBean responseBean = new SingleJobViewResponseBean();

        List<JobCandidateMapping> jcmList = jobCandidateMappingRepository.findByJobAndStage(job, jobStageStepRepository.findStageIdForJob(jobId, stage));

        jcmList.forEach(jcmFromDb-> {
            jcmFromDb.setJcmCommunicationDetails(jcmCommunicationDetailsRepository.findByJcmId(jcmFromDb.getId()));
            jcmFromDb.setCvRating(cvRatingRepository.findByJobCandidateMappingId(jcmFromDb.getId()));

            List<JcmProfileSharingDetails>jcmProfileSharingDetails = jcmProfileSharingDetailsRepository.findByJobCandidateMappingId(jcmFromDb.getId());
            jcmProfileSharingDetails.forEach(detail->{
                detail.setHiringManagerName(detail.getProfileSharingMaster().getReceiverName());
                detail.setHiringManagerEmail(detail.getProfileSharingMaster().getReceiverEmail());
            });
            jcmFromDb.setInterestedHiringManagers(
                    jcmProfileSharingDetails
                            .stream()
                            .filter( jcmProfileSharingDetail -> jcmProfileSharingDetail.getHiringManagerInterestDate()!=null && jcmProfileSharingDetail.getHiringManagerInterest())
                            .collect(Collectors.toList())
            );

            jcmFromDb.setNotInterestedHiringManagers(
                    jcmProfileSharingDetails
                            .stream()
                            .filter( jcmProfileSharingDetail -> jcmProfileSharingDetail.getHiringManagerInterestDate()!=null && !jcmProfileSharingDetail.getHiringManagerInterest())
                            .collect(Collectors.toList())
            );

            jcmFromDb.setNotRespondedHiringManagers(
                    jcmProfileSharingDetails
                            .stream()
                            .filter( jcmProfileSharingDetail -> jcmProfileSharingDetail.getHiringManagerInterestDate()==null )
                            .collect(Collectors.toList())
            );
        });
        Collections.sort(jcmList);

        responseBean.setCandidateList(jcmList);

        Map<Long, String> stagesForJob = convertObjectArrayToMap(jobRepository.findStagesForJob(jobId));

        List<Object[]> stageCountList = jobCandidateMappingRepository.findCandidateCountByStage(jobId);

        stageCountList.stream().forEach(objArray -> {
            String key = stagesForJob.get(((Integer) objArray[0]).longValue());
            if (null == responseBean.getCandidateCountByStage().get(key))
                responseBean.getCandidateCountByStage().put(key, ((BigInteger) objArray[1]).intValue());
            else //stage exists in response bean, add the count of the other step to existing value
                responseBean.getCandidateCountByStage().put(key,responseBean.getCandidateCountByStage().get(key)  + ((BigInteger) objArray[1]).intValue());
        });
        log.info("Completed processing request to find candidates for job {}  and stage: {} in {} ms.", jobId, stage ,(System.currentTimeMillis() - startTime));

        return responseBean;
    }

    private Map<Long, String> convertObjectArrayToMap(List<Object[]> stagesForJob) {
        Map<Long, String> convertedMap = new HashMap<>(stagesForJob.size());
        stagesForJob.stream().forEach(objArray -> {
            convertedMap.put(((Integer) objArray[0]).longValue(), objArray[1].toString());
        });
        return convertedMap;
    }

    private void addJobOverview(Job job, Job oldJob, User loggedInUser) { //method for add job for Overview page
        boolean deleteExistingJobStageStep = (null != job.getId());

        //validate title
        if (job.getJobTitle().length() > IConstant.TITLE_MAX_LENGTH)  //Truncate job title if it is greater than max length
            job.setJobTitle(job.getJobTitle().substring(0, IConstant.TITLE_MAX_LENGTH));
        Company userCompany = null;
        if(null != job.getCompanyId())
            userCompany = companyRepository.getOne(job.getCompanyId().getId());

        if (null == userCompany) {
            throw new ValidationException("Cannot find company for current job", HttpStatus.EXPECTATION_FAILED);
        }

        job.setCompanyId(userCompany);
        String historyMsg = "Created";
        if(deleteExistingJobStageStep){
            jobStageStepRepository.deleteByJobId(job.getId());
            jobStageStepRepository.flush();
        }

        if (null != oldJob && !oldJob.getStatus().equals(IConstant.JobStatus.PUBLISHED)) {//only update existing job
            if(null != job.getHiringManager())
                oldJob.setHiringManager(job.getHiringManager());
            if(null != job.getRecruiter())
                oldJob.setRecruiter(job.getRecruiter());

            oldJob.setJobTitle(job.getJobTitle());
            oldJob.setJobDescription(job.getJobDescription());
            oldJob.setUpdatedBy(loggedInUser);
            oldJob.setUpdatedOn(new Date());
            oldJob = jobRepository.save(oldJob);
            historyMsg = "Updated";

            //remove all data from job_key_skills and job_capabilities
            jobKeySkillsRepository.deleteByJobId(job.getId());
            jobCapabilitiesRepository.deleteByJobId(job.getId());

            jobKeySkillsRepository.flush();
            jobCapabilitiesRepository.flush();

        } else if(null == oldJob){ //Create new entry for job
            job.setCreatedOn(new Date());
            job.setMlDataAvailable(false);
            job.setStatus(IConstant.JobStatus.DRAFT.getValue());
            job.setCreatedBy(loggedInUser);

            //End of code to be removed
            oldJob = jobRepository.save(job);
        }
        //TODO: remove one JobRepository call
        //Add job stage step for this job
        addJobStageStep(oldJob);
        //Add Job details
        addJobDetail(job, oldJob, loggedInUser);

        saveJobHistory(job.getId(), historyMsg + " job overview", loggedInUser);

        if(null != oldJob && !oldJob.getStatus().equals(IConstant.JobStatus.PUBLISHED)){
            //make a call to ML api to obtain skills and capabilities
            if(MasterDataBean.getInstance().getConfigSettings().getMlCall()==1) {
                if(null == job.getSelectedRole())
                    job.setSelectedRole(" ");
                try {
                    RolePredictionBean rolePredictionBean = new RolePredictionBean();
                    RolePredictionBean.RolePrediction rolePrediction= new RolePredictionBean.RolePrediction();
                    rolePrediction.setJobTitle(job.getJobTitle());
                    rolePrediction.setJobDescription(job.getJobDescription());
                    rolePrediction.setRecruiterRoles(job.getSelectedRole());
                    rolePredictionBean.setRolePrediction(rolePrediction);
                    callMl(rolePredictionBean, job.getId(), job);
                    if(null == oldJob) {
                        job.setMlDataAvailable(true);
                        jobRepository.save(job);
                    }
                    else {
                        oldJob.setMlDataAvailable(true);
                        jobRepository.save(oldJob);
                    }
                } catch (Exception e) {
                    log.error("Error while fetching data from ML: " + e.getMessage());
                    job.setMlErrorMessage(IErrorMessages.ML_DATA_UNAVAILABLE);
                }
            }
        }
        //populate key skills for the job
        job.setJobKeySkillsList(jobKeySkillsRepository.findByJobId(job.getId()));
    }

    private void callMl(RolePredictionBean requestBean, long jobId, Job job) throws Exception {
        log.info("inside callMl method");
        String mlResponse = null;
        String mlRequest = null;
        Map breadCrumb = new HashMap<String, String>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> roles = new ArrayList<>();

            if(null != job.getSelectedRole()){
                requestBean.getRolePrediction().setRecruiterRoles(job.getSelectedRole());
            }
            objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mlRequest = objectMapper.writeValueAsString(requestBean);
            log.info("Sending request to ml for LB job id : "+jobId);
            mlResponse = RestClient.getInstance().consumeRestApi(mlRequest, mlUrl, HttpMethod.POST,null);
            log.info("Response received: " + mlResponse);
            log.info("Getting response from ml for LB job id : "+jobId);
            long startTime = System.currentTimeMillis();

            //add data in breadCrumb
            breadCrumb.put("Job Id: ", String.valueOf(jobId));
            breadCrumb.put("Request", mlRequest);
            breadCrumb.put("Response", mlResponse);

            MLResponseBean responseBean = objectMapper.readValue(mlResponse, MLResponseBean.class);

            //if ml status is jdc_jtm_Error
            if(responseBean.getRolePrediction().getStatus().equalsIgnoreCase(IConstant.MlRolePredictionStatus.JDC_JTM_ERROR.getValue()) ||
            responseBean.getRolePrediction().getStatus().equalsIgnoreCase(IConstant.MlRolePredictionStatus.JDC_JTN_ERROR.getValue()) ||
                    responseBean.getRolePrediction().getStatus().equalsIgnoreCase(IConstant.MlRolePredictionStatus.JDB_JTM_ERROR.getValue())){
                log.info("ml response status is " +responseBean.getRolePrediction().getStatus()+" for job id : "+jobId);
                responseBean.getRolePrediction().getJdRoles().forEach(role -> {
                    roles.add(role.getRoleName());
                });
                responseBean.getRolePrediction().getJtRoles().forEach(role -> {
                    roles.add(role.getRoleName());
                });
                job.setRoles(roles);
                return;
            }else if(responseBean.getRolePrediction().getStatus().equalsIgnoreCase(IConstant.MlRolePredictionStatus.NO_ERROR.getValue())){
                //if ml status is no_Error
                log.info("ml response status is no_Error for job id : "+jobId);
                int numUniqueSkills = handleSkillsFromML(responseBean.getTowerGeneration().getSkills(), jobId);
                if(numUniqueSkills != responseBean.getTowerGeneration().getSkills().size()) {
                    log.error(IErrorMessages.ML_DATA_DUPLICATE_SKILLS + mlResponse);
                    SentryUtil.logWithStaticAPI(null, IErrorMessages.ML_DATA_DUPLICATE_SKILLS + mlResponse, breadCrumb);
                }
                Set<Integer> uniqueCapabilityIds = new HashSet<>();
                handleCapabilitiesFromMl(responseBean.getTowerGeneration().getSuggestedCapabilities(), jobId, true, uniqueCapabilityIds);
                handleCapabilitiesFromMl(responseBean.getTowerGeneration().getAdditionalCapabilities(), jobId, false, uniqueCapabilityIds);
            }else{
                SentryUtil.logWithStaticAPI(null, "ml status is different than expected or suff_error", breadCrumb);
                if(responseBean.getRolePrediction().getStatus().equalsIgnoreCase(IConstant.MlRolePredictionStatus.SUFF_ERROR.getValue())) {
                    //if ml status is suff_Error
                    log.info("ml response status is suff_Error for job id : " + jobId);
                    throw new ValidationException("There was no enough data in JD and JT for this job : " + jobId, HttpStatus.BAD_REQUEST);
                }
            }
            log.info("Time taken to process ml data: " + (System.currentTimeMillis() - startTime) + "ms.");

        }catch(Exception e) {
            log.error("Error While processing ml call : "+e.getMessage());
            SentryUtil.logWithStaticAPI(null, "Error While processing ml call : "+e.getMessage(), breadCrumb);
        }
    }

    /**
     * Method to handle all skills provided by ML
     *
     * @param skillsList List of skills obtained from ML
     * @param jobId the job id for which the skills have to persisted
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private int handleSkillsFromML(List<Skills> skillsList, long jobId) throws Exception {
        log.info("Size of skill list: " + skillsList.size());
        Set<Skills> uniqueSkills = skillsList.stream().collect(Collectors.toSet());
        List<JobKeySkills> jobKeySkillsToSave = new ArrayList<>(uniqueSkills.size());
        uniqueSkills.forEach(skill-> {
            //find a skill from the master table for the skill name provided
            SkillsMaster skillFromDb = skillMasterRepository.findBySkillNameIgnoreCase(skill.getName());
            //if none if found, add a skill
            if (null == skillFromDb) {
                skillFromDb = new SkillsMaster(skill.getName());
                skillMasterRepository.save(skillFromDb);
            }
            //add a record in job_key_skills with this skill id
            jobKeySkillsToSave.add(new JobKeySkills(skillFromDb, true,true, new Date(), (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal(), jobId));
        });
        jobKeySkillsRepository.saveAll(jobKeySkillsToSave);
        return uniqueSkills.size();
    }

    /**
     * Method to handle all capabilities provided by ML
     *
     * @param capabilitiesList
     * @param jobId
     * @param selectedByDefault
     * @param uniqueCapabilityIds
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void handleCapabilitiesFromMl(List<Capabilities> capabilitiesList, long jobId, boolean selectedByDefault, Set<Integer> uniqueCapabilityIds) throws Exception {
        log.info("Size of capabilities list to process: " + capabilitiesList.size());
        List<JobCapabilities> jobCapabilitiesToSave = new ArrayList<>(capabilitiesList.size());
        capabilitiesList.forEach(capability->{
            if (capability.getCapCode() !=0 && !uniqueCapabilityIds.contains(capability.getCapCode())) {
                jobCapabilitiesToSave.add(new JobCapabilities(Long.valueOf(capability.getCapCode()), capability.getCapability(), selectedByDefault, mapWeightage(capability.getCapabilityWeight()), new Date(), (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), jobId));
                uniqueCapabilityIds.add(capability.getCapCode());
            }
        });
        jobCapabilitiesRepository.saveAll(jobCapabilitiesToSave);
    }

    private int mapWeightage(int capabilityWeight) {
        if(capabilityWeight <= 2)
            return 2;
        else if (capabilityWeight <=6)
            return 6;
        return 10;
    }

    private void addJobScreeningQuestions(Job job, Job oldJob, User loggedInUser) throws Exception { //method for add screening questions

        //commented out the check as per ticket #146
        /*
        if (job.getJobScreeningQuestionsList().size() > MasterDataBean.getInstance().getConfigSettings().getMaxScreeningQuestionsLimit()) {
            throw new ValidationException(IErrorMessages.SCREENING_QUESTIONS_VALIDATION_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
        }
        */
        if(null != oldJob && oldJob.getStatus().equals(IConstant.JobStatus.PUBLISHED)){
            return;
        }
        String historyMsg = "Added";

        if (null != oldJob.getJobScreeningQuestionsList() && oldJob.getJobScreeningQuestionsList().size() > 0) {
            historyMsg = "Updated";
            jobScreeningQuestionsRepository.deleteAll(oldJob.getJobScreeningQuestionsList());//delete old job screening question list
        }

        job.getJobScreeningQuestionsList().forEach(n -> {
            n.setCreatedBy(loggedInUser.getId());
            n.setCreatedOn(new Date());
            n.setJobId(job.getId());
            n.setUpdatedOn(new Date());
            n.setUpdatedBy(loggedInUser.getId());
        });
        jobScreeningQuestionsRepository.saveAll(job.getJobScreeningQuestionsList());
        saveJobHistory(job.getId(), historyMsg + " screening questions", loggedInUser);

        //populate key skills for the job
       // job.setJobKeySkillsList(jobKeySkillsRepository.findByJobId(job.getId()));
    }

    private void addJobKeySkills(Job job, Job oldJob, User loggedInUser) throws Exception { //update and add new key skill
        if(null != oldJob && oldJob.getStatus().equals(IConstant.JobStatus.PUBLISHED))
            return;

        List<JobKeySkills> mlProvidedKeySkills = jobKeySkillsRepository.findByJobIdAndMlProvided(oldJob.getId(), true);

        //if there were key skills suggested by ML, and the request for add job - key skills has a 0 length array, throw an error, otherwise, proceed
        if (mlProvidedKeySkills.size() > 0 && null != job.getJobKeySkillsList() && job.getJobKeySkillsList().isEmpty()) {
            throw new ValidationException("Job key skills " + IErrorMessages.EMPTY_AND_NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
        }

        //delete all key skills where MlProvided=false
        List<JobKeySkills> userProvidedJobKeySkillslist = jobKeySkillsRepository.findByJobIdAndMlProvided(job.getId(), false);
        if (userProvidedJobKeySkillslist.size() > 0) {
            jobKeySkillsRepository.deleteAll(userProvidedJobKeySkillslist);
        }
        jobKeySkillsRepository.flush();


        //For each keyskill in the request (will have only the mlProvided true ones), update the values for selected
        Map<Long, JobKeySkills> newSkillValues = new HashMap();
        job.getJobKeySkillsList().stream().forEach(jobKeySkill -> newSkillValues.put(jobKeySkill.getSkillId().getId(), jobKeySkill));

        oldJob.getJobKeySkillsList().forEach(oldKeySkill -> {
            if (oldKeySkill.getMlProvided()) {
                JobKeySkills newValue = newSkillValues.get(oldKeySkill.getSkillId().getId());
                oldKeySkill.setSelected(newValue.getSelected());
                oldKeySkill.setUpdatedOn(new Date());
                oldKeySkill.setUpdatedBy(loggedInUser);
            }
        });

        //get all skillMaster and tempskills master data
        Map<String, Long> skillsMasterMapByName = new HashMap<>();
        Map<Long, SkillsMaster> skillsMasterMap = new HashMap<>();
        List<SkillsMaster> skillsMasterList = skillMasterRepository.findAll();
        skillsMasterList.forEach(skillsMaster -> {
            skillsMasterMapByName.put(skillsMaster.getSkillName(), skillsMaster.getId());
            skillsMasterMap.put(skillsMaster.getId(), skillsMaster);
        });

        Map<String, Long> tempSkillsMapByName = new HashMap<>();
        Map<Long, TempSkills> tempSkillsMap = new HashMap<>();
        tempSkillsRepository.findAll().forEach(tempSkill -> {
            tempSkillsMapByName.put(tempSkill.getSkillName(), tempSkill.getId());
            tempSkillsMap.put(tempSkill.getId(), tempSkill);
        });

        //For each user entered key skill, do the following:
        //(i) check if the skill is already present in the ml key skill list for the job. If yes, skip this skill
        //(ii) if not, check if the skill is present in the skills master table. If yes, use that skill id to insert a record in the db
        //(iii) if not, check if the skill is present in the temp key skills table. If yes, use that id and insert a record in the db with temp key skill id column populated
        //(iv) if not, enter a record in the temp key skills table and use the id of the newly inserted record to insert a record in job key skill table with the temp key skill id column populated

        for (String userSkills : job.getUserEnteredKeySkill()) {

            if (skillsMasterMapByName.keySet().contains(userSkills)) {
                Long skillId = skillsMasterMapByName.get(userSkills);
                //does the skill match one of the those provided by ML?
                if (null != newSkillValues.get(skillId)) {
                    //found a match, skip this skill
                    continue;
                } else {
                    //no match found in mlProvided skill, add a record
                    jobKeySkillsRepository.save(new JobKeySkills(skillsMasterMap.get(skillId), false, true, new Date(), loggedInUser, job.getId()));
                }

            }
            //check if the user entered skill exists in the temp skills table
            else if (tempSkillsMapByName.keySet().contains(userSkills)) {
                Long tempSkillId = tempSkillsMapByName.get(userSkills);
                jobKeySkillsRepository.save(new JobKeySkills(tempSkillsMap.get(tempSkillId), false, true, new Date(), loggedInUser, job.getId()));

            }
            //this is a new skill, add to temp skills and refer to jobkeyskills table
            else {
                TempSkills tempSkills = tempSkillsRepository.save(new TempSkills(userSkills, false));
                jobKeySkillsRepository.save(new JobKeySkills(tempSkills, false, true, new Date(), loggedInUser, job.getId()));
            }
        }
        saveJobHistory(job.getId(), "Added key skills", loggedInUser);
        //populate the capabilities for the job
        job.setJobCapabilityList(jobCapabilitiesRepository.findByJobId(job.getId()));
    }


    private void addJobCapabilities(Job job, Job oldJob, User loggedInUser) { //add job capabilities

        if(null != oldJob && oldJob.getStatus().equals(IConstant.JobStatus.PUBLISHED))
            return;

        //if there are capabilities that were returned from ML, and the request for add job - capabilities has a 0 length array, throw an error, otherwise, proceed
        if (oldJob.getJobCapabilityList().size() > 0 && null != job.getJobCapabilityList() && job.getJobCapabilityList().isEmpty()) {
            throw new ValidationException("Job Capabilities " + IErrorMessages.EMPTY_AND_NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
        }

        int selectedCapabilityCount = job.getJobCapabilityList().stream().filter(capability->capability.getSelected()).collect(Collectors.toList()).size();

        if(selectedCapabilityCount>MasterDataBean.getInstance().getConfigSettings().getMaxCapabilities())
            throw new ValidationException("Job Capabilities more than max capabilities limit for jobId : "+ job.getId(), HttpStatus.BAD_REQUEST);

        //For each capability in the request, update the values for selected and importance_level
        Map<Long, JobCapabilities> newCapabilityValues = new HashMap();
        job.getJobCapabilityList().stream().forEach(jobCapability -> newCapabilityValues.put(jobCapability.getId(), jobCapability));

        log.info("Capability count: Old Job: " + oldJob.getJobCapabilityList().size() + " new capability list size: " + newCapabilityValues.size());

        oldJob.getJobCapabilityList().forEach(oldCapability -> {
            JobCapabilities newValue = newCapabilityValues.get(oldCapability.getId());
            if(newValue.getSelected()) {
                List<WeightageCutoffByCompanyMapping> wtgCompanyMappings = weightageCutoffByCompanyMappingRepository.findByCompanyIdAndWeightage(oldJob.getCompanyId().getId(), newValue.getWeightage());
                if (null != wtgCompanyMappings && wtgCompanyMappings.size() > 0) {
                    wtgCompanyMappings.stream().forEach(starRatingMapping -> oldCapability.getJobCapabilityStarRatingMappingList().add(new JobCapabilityStarRatingMapping(newValue.getId(), oldJob.getId(), newValue.getWeightage(), starRatingMapping.getCutoff(), starRatingMapping.getPercentage(), starRatingMapping.getStarRating())));

                } else {
                    List<WeightageCutoffMapping> weightageCutoffMappings = weightageCutoffMappingRepository.findByWeightage(newValue.getWeightage());
                    if (null != weightageCutoffMappings && weightageCutoffMappings.size() > 0) {
                        weightageCutoffMappings.stream().forEach(starRatingMapping -> oldCapability.getJobCapabilityStarRatingMappingList().add(new JobCapabilityStarRatingMapping(newValue.getId(), oldJob.getId(), newValue.getWeightage(), starRatingMapping.getCutoff(), starRatingMapping.getPercentage(), starRatingMapping.getStarRating())));
                    }
                }
            }
            oldCapability.setWeightage(newValue.getWeightage());
            oldCapability.setSelected(newValue.getSelected());
            if(newValue.getSelected())
                oldCapability.setWeightage(newValue.getWeightage());
            oldCapability.setUpdatedOn(new Date());
            oldCapability.setUpdatedBy(loggedInUser);
        });


        //29th July: Do not auto-publish the job. The job should be explicitly published by means of an API call
        //oldJob.setStatus(IConstant.JobStatus.PUBLISHED.getValue());
        //oldJob.setDatePublished(new Date());
        jobRepository.save(oldJob);
        saveJobHistory(job.getId(), "Added capabilities", loggedInUser);

        job.getJobCapabilityList().clear();
        job.getJobCapabilityList().addAll(oldJob.getJobCapabilityList());
    }

    private void addJobDetail(Job job, Job oldJob, User loggedInUser) {//add job details

        MasterDataBean masterDataBean = MasterDataBean.getInstance();

        oldJob.setCompanyJobId(job.getCompanyJobId());
        oldJob.setNoOfPositions(job.getNoOfPositions());

        //Update Function
        if (null == masterDataBean.getFunction().get(job.getFunction().getId())) {
            //throw new ValidationException("In Job, function " + IErrorMessages.NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
            log.error("In Job, function " + IErrorMessages.NULL_MESSAGE + job.getId());
        }else{
            oldJob.setFunction(job.getFunction());
        }

        //Update Currency
        if (null == job.getCurrency()) {
            // throw new ValidationException("In Job, Currency " + IErrorMessages.NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
            log.error("In Job, Currency " + IErrorMessages.NULL_MESSAGE + job.getId());
        }else{
            oldJob.setCurrency(job.getCurrency());
        }

        List<CompanyAddress> companyAddressList = companyAddressRepository.findByCompanyId(loggedInUser.getCompany().getId());
        List<CompanyBu> companyBuList = companyBuRepository.findByCompanyId(loggedInUser.getCompany().getId());

        Map<Long, CompanyBu> companyBuMap = new HashMap<>();
        Map<Long, CompanyAddress> companyAddressMap = new HashMap<>();

        companyBuList.forEach(companyBu -> companyBuMap.put(companyBu.getId(), companyBu));
        companyAddressList.forEach(companyAddress -> companyAddressMap.put(companyAddress.getId(), companyAddress));

        //Update Job and Interview Location
        if(null != job.getJobLocation() && null != job.getInterviewLocation()){
            if (companyAddressList.isEmpty() || null == companyAddressMap.get(job.getJobLocation().getId())
                    || null == companyAddressMap.get(job.getInterviewLocation().getId())) {
                // throw new ValidationException("In Job, company address " + IErrorMessages.NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
                log.error("In Job, company address " + IErrorMessages.NULL_MESSAGE + job.getId());
            }else{
                oldJob.setInterviewLocation(companyAddressMap.get(job.getInterviewLocation().getId()));
                oldJob.setJobLocation(companyAddressMap.get(job.getJobLocation().getId()));
            }
        }

        //Update Bu
        if(null != job.getBuId()){
            if (companyBuList.isEmpty() || null == companyBuMap.get(job.getBuId().getId())) {
                // throw new ValidationException("In Job, company bu " + IErrorMessages.NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
                log.error("In Job, company bu " + IErrorMessages.NULL_MESSAGE + job.getId());
            }else{
                oldJob.setBuId(companyBuMap.get(job.getBuId().getId()));
            }
        }

        //Update ExperienceRange
        if(null != job.getExperienceRange() && null != masterDataBean.getExperienceRange().get(job.getExperienceRange().getId())){
            oldJob.setExperienceRange(job.getExperienceRange());
        }else{
            // throw new ValidationException("In Job, experience Range " + IErrorMessages.NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
            log.error("In Job, ExperienceRange " + IErrorMessages.NULL_MESSAGE + job.getId());
        }

        //Update Salary
        oldJob.setMinSalary(job.getMinSalary());
        oldJob.setMaxSalary(job.getMaxSalary());

        if(!oldJob.getStatus().equals(IConstant.JobStatus.PUBLISHED)){

            //Update Education
            if (null == masterDataBean.getEducation().get(job.getEducation().getId())) {
                //throw new ValidationException("In Job, education " + IErrorMessages.NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
                log.error("In Job, education " + IErrorMessages.NULL_MESSAGE + job.getId());
            }else{
                oldJob.setEducation(job.getEducation());
            }

            //Update Notice period
            oldJob.setNoticePeriod(job.getNoticePeriod());
        }

        oldJob.setUpdatedOn(new Date());
        jobRepository.save(oldJob);

        //populate all users for the company of current user
        List<User> userList = userRepository.findByCompanyId(loggedInUser.getCompany().getId());
        job.getUsersForCompany().addAll(userList);
    }

    private void addJobHiringTeam(Job job, Job oldJob, User loggedInUser) throws Exception {
        log.info("inside addJobHiringTeam");
        if(null != oldJob){
            jobHiringTeamRepository.deleteByJobId(oldJob.getId());
            jobHiringTeamRepository.flush();
        }
        AtomicLong i = new AtomicLong();
        if(null != job.getHiringTeamStepMapping() && job.getHiringTeamStepMapping().size()>0) {
            List<JobHiringTeam> jobHiringTeamList = new ArrayList<>(job.getJobHiringTeamList().size());
            job.getHiringTeamStepMapping().forEach(stageStep -> {
                jobHiringTeamList.add(new JobHiringTeam(oldJob.getId(), JobStageStep.builder().id(stageStep.get(0)).build(), User.builder().id(stageStep.get(1)).build(), i.longValue(), new Date(), loggedInUser));
                i.getAndIncrement();
            });
            jobHiringTeamRepository.saveAll(jobHiringTeamList);
            oldJob.setJobHiringTeamList(jobHiringTeamList);
            jobHiringTeamRepository.flush();
        }
        jobRepository.save(oldJob);
    }

    private void addJobExpertise(Job job, Job oldJob){

        if(null != oldJob && oldJob.getStatus().equals(IConstant.JobStatus.PUBLISHED))
            return;

        MasterDataBean masterDataBean = MasterDataBean.getInstance();
        if(null == masterDataBean.getExpertise().get(job.getExpertise().getId())){
            throw new ValidationException("In Job, Expertise " + IErrorMessages.NULL_MESSAGE + job.getId(), HttpStatus.BAD_REQUEST);
        }
        oldJob.setExpertise(job.getExpertise());
        jobRepository.save(oldJob);
    }

    /**
     * Service method to publish a job
     *
     * @param jobId id of the job to be published
     */
    @Transactional
    public void publishJob(Long jobId) throws Exception {
        log.info("Received request to publish job with id: " + jobId);
        Job publishedJob = changeJobStatus(jobId,IConstant.JobStatus.PUBLISHED.getValue());
        log.info("Completed publishing job with id: " + jobId);
        if(publishedJob.getJobCapabilityList().size() == 0)
            log.info("No capabilities exist for the job: " + jobId + " Scoring engine api call will NOT happen");
        else if(jobCapabilitiesRepository.findByJobIdAndSelected(jobId, true).size() == 0)
            log.info("No capabilities have been selected for the job: {}. Scoring engine api call will NOT happen", jobId);
        else {
            log.info("Calling Scoring Engine Api to create a job");
            try {
                String scoringEngineResponse = RestClient.getInstance().consumeRestApi(convertJobToRequestPayload(jobId, publishedJob), scoringEngineBaseUrl + scoringEngineAddJobUrlSuffix, HttpMethod.POST, null);
                publishedJob.setScoringEngineJobAvailable(true);
                jobRepository.save(publishedJob);
            } catch (Exception e) {
                log.error("Error during create job api call on Scoring engine: " + e.getMessage());
            }
        }
    }

    private void addJobStageStep(Job job) {
        List<CompanyStageStep> companyStageSteps = companyStageStepRepository.findByCompanyId(job.getCompanyId());
        List<JobStageStep> jobStageSteps = new ArrayList<>(companyStageSteps.size());
        for(CompanyStageStep companyStageStep: companyStageSteps) {
            jobStageSteps.add(JobStageStep.builder().jobId(job.getId()).stageStepId(companyStageStep).createdBy(job.getCreatedBy()).createdOn(new Date()).build());
        }
        jobStageStepRepository.saveAll(jobStageSteps);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private String convertJobToRequestPayload(Long jobId, Job publishedJob) throws Exception {
        List<JobCapabilities> jobCapabilities = jobCapabilitiesRepository.findByJobIdAndSelected(jobId,true);
        MasterData expertise = null;
        if(null != publishedJob.getExpertise())
            expertise = MasterDataBean.getInstance().getExpertise().get(publishedJob.getExpertise().getId());

        List<Capability> capabilityList = new ArrayList<>(jobCapabilities.size());
        jobCapabilities.stream().forEach(jobCapability -> {
            capabilityList.add(new Capability(jobCapability.getCapabilityId(), jobCapability.getWeightage(), jobCapability.getJobCapabilityStarRatingMappingList()));
        });
        ScoringEngineJobBean jobRequestBean;
        if(null != expertise)
             jobRequestBean = new ScoringEngineJobBean(jobId, Long.parseLong(expertise.getValueToUSe()), capabilityList);
        else
             jobRequestBean = new ScoringEngineJobBean(jobId, null, capabilityList);

        return (new ObjectMapper()).writeValueAsString(jobRequestBean);
    }

    /**
     * Service method to archive a job
     *
     * @param jobId id of the job to be archived
     */
    @Transactional
    public void archiveJob(Long jobId) {
        log.info("Received request to archive job with id: " + jobId);
        changeJobStatus(jobId,IConstant.JobStatus.ARCHIVED.getValue());
        log.info("Completed archiving job with id: " + jobId);
    }

    /**
     * Service method to unarchive a job
     *
     * @param jobId id of the job to be unarchived
     */
    @Transactional
    public void unarchiveJob(Long jobId) throws Exception {
        log.info("Received request to unarchive job with id: " + jobId);
        changeJobStatus(jobId,null);
        log.info("Completed unarchiving job with id: " + jobId);
    }

    /**
     * common method to Publish, Archive or Unarchive a job
     * @param jobId the job on which the operation is to be performed
     * @param status the status to be set. If the job is being unarchived, the status will be sent as null
     */
    private Job changeJobStatus(Long jobId, String status) {
        Job job = jobRepository.getOne(jobId);
        if (null == job) {
            throw new WebException("Job with id " + jobId + "does not exist", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if(null == status) {
            //check that the old status of job is archived
            if (!IConstant.JobStatus.ARCHIVED.getValue().equals(job.getStatus()))
                throw new WebException(IErrorMessages.JOB_NOT_ARCHIVED, HttpStatus.UNPROCESSABLE_ENTITY);
            if(null == job.getDatePublished())
                job.setStatus(IConstant.JobStatus.DRAFT.getValue());
            else
                job.setStatus(IConstant.JobStatus.PUBLISHED.getValue());

            job.setDateArchived(null);
        }
        else  {
            if (status.equals(IConstant.JobStatus.ARCHIVED.getValue()))
                job.setDateArchived(new Date());
            else
                job.setDatePublished(new Date());
            job.setStatus(status);
        }
        job.setUpdatedOn(new Date());
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        job.setUpdatedBy(loggedInUser);
        saveJobHistory(job.getId(), "Status changed to " +job.getStatus(), loggedInUser);
        return jobRepository.save(job);
    }

    @Transactional
    public Job getJobDetails(Long jobId) throws Exception {
        Job job = jobRepository.findById(jobId).orElse(null);
        if (null == job) {
            throw new WebException("Job with id " + jobId + " does not exist", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return job;
    }

    private void saveJobHistory(Long jobId, String historyMsg, User loggedInUser) {
        jobHistoryRepository.save(new JobHistory(jobId, historyMsg, loggedInUser));
    }

    @Transactional
    public List<JobHistory>getJobHistory(Long jobId)throws Exception{
        Job job = jobRepository.findById(jobId).orElse(null);
        if (null == job) {
            throw new WebException("Job with id " + jobId + "does not exist ", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return jobHistoryRepository.findByJobIdOrderByIdDesc(jobId);
    }

    /**
     * Service method to return the stage steps for a job
     *
     * @param jobId the job id for which stage steps are to be returned
     * @return list of stage steps
     * @throws Exception
     */
    @Transactional(readOnly = true)
    public List<JobStageStep> getJobStageStep(Long jobId) throws Exception {
        log.info("Received request to find stage steps for job with id {}", jobId);
        long startTime = System.currentTimeMillis();
        List<JobStageStep> returnList = jobStageStepRepository.findByJobId(jobId);
        log.info("Completed finding stage steps for jobId {} in {} ms", jobId, (System.currentTimeMillis() - startTime));
        return returnList;
    }
}