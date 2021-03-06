/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.model.JobCapabilityStarRatingMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : Shital Raval
 * Date : 6/9/19
 * Time : 4:52 PM
 * Class Name : ScoringEngineJobBean
 * Project Name : server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoringEngineJobBean {
    Long lbJobId;
    Long expertise;
    List<Capability> capabilities;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Capability {
    long capabilityId;
    int weightage;
    List<JobCapabilityStarRatingMapping> jobCapabilityStarRatingMappings;
}