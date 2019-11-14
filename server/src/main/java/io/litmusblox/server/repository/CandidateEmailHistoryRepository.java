/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.CandidateEmailHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : Sumit
 * Date : 17/7/19
 * Time : 12:06 PM
 * Class Name : CandidateEmailHistoryRepository
 * Project Name : server
 */
public interface CandidateEmailHistoryRepository extends JpaRepository<CandidateEmailHistory, Long> {

    @Transactional(readOnly = true)
    CandidateEmailHistory findByEmail(String email);

    @Transactional(readOnly = true)
    List<CandidateEmailHistory> findByCandidateIdOrderByIdDesc(Candidate candidateId);
}
