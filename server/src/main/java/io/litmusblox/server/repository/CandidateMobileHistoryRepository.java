/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.CandidateMobileHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : Sumit
 * Date : 17/7/19
 * Time : 12:09 PM
 * Class Name : CandidateMobileHistoryRepository
 * Project Name : server
 */
public interface CandidateMobileHistoryRepository extends JpaRepository<CandidateMobileHistory, Long> {

    @Transactional(readOnly = true)
    CandidateMobileHistory findByMobileAndCountryCode(String mobile, String countryCode);

    @Transactional(readOnly = true)
    List<CandidateMobileHistory> findByCandidateIdOrderByIdDesc(Candidate candidate);
}
