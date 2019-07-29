/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.litmusblox.server.utils.DateDeserializer;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author : Sumit
 * Date : 24/7/19
 * Time : 4:53 PM
 * Class Name : CandidateCompanyDetails
 * Project Name : server
 */
@Data
@Entity
@Table(name = "CANDIDATE_COMPANY_DETAILS")
public class CandidateCompanyDetails {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CANDIDATE_ID")
    private Long candidateId;

    @Column(name = "COMPANY_NAME")
    private String companyName;

    @Column(name = "DESIGNATION")
    private String designation;

    @Column(name = "SALARY")
    private String salary;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "NOTICE_PERIOD")
    private String noticePeriod;

    @Column(name = "START_DATE")
    @JsonDeserialize(using = DateDeserializer.class)
    private Date startDate;

    @Column(name = "END_DATE")
    @JsonDeserialize(using = DateDeserializer.class)
    private Date endDate;
}