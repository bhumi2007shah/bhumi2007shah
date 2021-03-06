/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author : Sumit
 * Date : 3/7/19
 * Time : 3:04 PM
 * Class Name : Country
 * Project Name : server
 */
@Data
@Entity
@Table(name = "COUNTRY")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Country implements Serializable {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "COUNTRY_NAME")
    private String countryName;

    @NotNull
    @Column(name = "COUNTRY_CODE")
    private String countryCode;

    @NotNull
    @Column(name = "MAX_MOBILE_LENGTH")
    private Long maxMobileLength;

    @Column(name = "COUNTRY_SHORT_CODE")
    private String countryShortCode;

    public Country(){}

    public Country(String countryName, String countryCode, Long maxMobileLength, String countryShortCode) {
        this.countryName = countryName;
        this.countryCode = countryCode;
        this.maxMobileLength = maxMobileLength;
        this.countryShortCode = countryShortCode;
    }
}
