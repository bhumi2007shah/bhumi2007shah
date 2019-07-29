/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.CandidateEducationDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : Sumit
 * Date : 25/07/19
 * Time : 5:52 PM
 * Class Name : CandidateEducationDetailsRepository
 * Project Name : server
 */
public interface CandidateEducationDetailsRepository extends JpaRepository<CandidateEducationDetails, Long> {

    @Transactional
   // @Query(value = "DELETE FROM CANDIDATE_EDUCATION_DETAILS ced WHERE ced.CANDIDATE_ID =: candidateId", nativeQuery = true)
    void deleteByCandidateId(/*@Param("candidateId")*/ Long candidateId);
}
