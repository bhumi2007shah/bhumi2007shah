/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author : Sumit
 * Date : 4/7/19
 * Time : 2:23 PM
 * Class Name : CandidateEmailHistory
 * Project Name : server
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "CANDIDATE_EMAIL_HISTORY")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CandidateEmailHistory implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CANDIDATE_ID")
    private Candidate candidate;

    @NotNull
    @Column(name = "EMAIL")
    private String email;

    @NotNull
    @Column(name = "CREATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn = new Date();

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CREATED_BY")
    private User createdBy;

    public CandidateEmailHistory(@NotNull Candidate candidateId, @NotNull String email, @NotNull Date createdOn, @NotNull User createdBy) {
        this.candidate = candidateId;
        this.email = email;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
    }
}
