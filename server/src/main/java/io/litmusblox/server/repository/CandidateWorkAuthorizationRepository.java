/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.CandidateWorkAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : Sumit
 * Date : 25/07/19
 * Time : 9:28 PM
 * Class Name : CandidateWorkAuthorizationRepository
 * Project Name : server
 */
public interface CandidateWorkAuthorizationRepository extends JpaRepository<CandidateWorkAuthorization, Long> {

    @Transactional
    void deleteByCandidateId(Long candidateId);

}
