/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Model class for JOB_CANDIDATE_MAPPING table
 *
 * @author : Shital Raval
 * Date : 10/7/19
 * Time : 2:15 PM
 * Class Name : JobCandidateMapping
 * Project Name : server
 */
@Data
@Entity
@Table(name="JOB_CANDIDATE_MAPPING")
public class JobCandidateMapping implements Serializable {

    private static final long serialVersionUID = 6868521896546285047L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "JOB_ID")
    private Job jobId;

    @NotNull
    @OneToOne
    @JoinColumn(name = "CANDIDATE_ID")
    private Candidate candidateId;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "STAGE")
    private MasterData stage;

    @NotNull
    @Column(name = "CREATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="CREATED_BY")
    private User createdBy;

    @Column(name = "UPDATED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedOn;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="UPDATED_BY")
    private User updatedBy;
}
