/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

/**
 * @author : Sumit
 * Date : 02/08/19
 * Time : 12:24 PM
 * Class Name : JcmProfileSharingDetails
 * Project Name : server
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "JCM_PROFILE_SHARING_DETAILS")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JcmProfileSharingDetails {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID", unique = true)
    @GeneratedValue(generator="system-uuid")
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="SENDER_ID")
    private User senderId;

    @NotNull
    @Column(name = "RECEIVER_NAME")
    private String receiverName;

    @NotNull
    @Column(name = "RECEIVER_EMAIL")
    private String receiverEmail;

    @NotNull
    @Column(name="JOB_CANDIDATE_MAPPING_ID")
    private Long jobCandidateMappingId;

    @Column(name = "EMAIL_SENT_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date emailSentOn;

    @Column(name = "HIRING_MANAGER_INTEREST")
    private Boolean hiringManagerInterest=false;

    @Column(name = "HIRING_MANAGER_INTEREST_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date hiringManagerInterestDate;
}