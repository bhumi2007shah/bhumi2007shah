/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import io.litmusblox.server.model.JobScreeningQuestions;
import io.litmusblox.server.service.IJobControllerMappingService;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller for REST apis that do not require authentication. For e.g.
 * 1. Activate user
 * 2. Reset password
 * 3. Fetch screening questions for the uuid
 * 4. Capture candidate interest
 * 5. Capture candidate response to screening question chatbot
 *
 * @author : Shital Raval
 * Date : 23/7/19
 * Time : 9:54 AM
 * Class Name : NoAuthController
 * Project Name : server
 */
@CrossOrigin(allowedHeaders = "*")
@RestController
@RequestMapping("/api/noAuth")
@Log4j2
public class NoAuthController {

    @Autowired
    IJobControllerMappingService jobControllerMappingService;

    /**
     * Rest api to get all screening questions for the job
     * @param uuid the uuid corresponding to a unique jcm record
     * @return the list of job screening questions
     * @throws Exception
     */
    @GetMapping("/screeningQuestion")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    String screeningQuestionsForUuid(@RequestParam("uuid") UUID uuid) throws Exception {
        log.info("Received request to retrieve screening questions for candidate: " + uuid);
        long startTime = System.currentTimeMillis();
        List<JobScreeningQuestions> response = jobControllerMappingService.getJobScreeningQuestions(uuid);
        log.info("Completed fetching screening questions in " + (System.currentTimeMillis() - startTime) + "ms.");
         return Util.stripExtraInfoFromResponseBean(response,
                (new HashMap<String, List<String>>(){{
                    put("UserClassFilter", Arrays.asList("displayName"));
                }}),
                new HashMap<String, List<String>>() {{
                    put("CompanyScreeningQuestionFilter", Arrays.asList("createdOn", "createdBy", "updatedOn", "updatedBy","company"));
                    put("UserScreeningQuestionFilter", Arrays.asList("createdOn", "updatedOn","userId"));
                }}
        );
    }


    /**
     * Rest api to capture candidate consent from chatbot
     * @param uuid the uuid corresponding to a unique jcm record
     * @param interest boolean to capture candidate consent
     * @throws Exception
     */
    @PutMapping("/candidateInterest")
    @ResponseStatus(HttpStatus.OK)
    void captureCandidateInterest(@RequestParam("uuid") UUID uuid, @RequestParam("interest") boolean interest) throws Exception {
        log.info("Received candidate interest capture request: " + uuid);
        long startTime = System.currentTimeMillis();
        jobControllerMappingService.captureCandidateInterest(uuid, interest);
        log.info("Completed capturing candidate request in " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    /**
     * Rest api to capture candidate response to screening questions from chatbot
     * @param uuid the uuid corresponding to a unique jcm record
     * @param candidateResponse the response provided by a candidate against each screening question
     * @throws Exception
     */
    @PostMapping("/screeningQuestionResponse")
    @ResponseStatus(HttpStatus.OK)
    void screeningQuestionResponses(@RequestParam("uuid") UUID uuid, @RequestBody Map<Long,List<String>> candidateResponse) throws Exception{
        log.info("Received screening question responses from candidate: " + uuid);
        long startTime = System.currentTimeMillis();
        jobControllerMappingService.saveScreeningQuestionResponses(uuid, candidateResponse);
        log.info("Completed saving candidate response to screening questions in " + (System.currentTimeMillis() - startTime) + "ms.");
    }
}