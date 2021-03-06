/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.service.CompanyWorspaceBean;
import io.litmusblox.server.service.ICompanyService;
import io.litmusblox.server.service.UserWorkspaceBean;
import io.litmusblox.server.service.impl.LbUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Admin workspaces
 *
 * @author : Shital Raval
 * Date : 1/8/19
 * Time : 12:53 PM
 * Class Name : AdminController
 * Project Name : server
 */
@CrossOrigin(allowedHeaders = "*")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    ICompanyService companyService;

    @Autowired
    LbUserDetailsService userDetailsService;

    /**
     * REST Api to fetch a list of all companies
     *
     * @return List of companies
     * @throws Exception
     */
    @GetMapping(value = "/fetchCompanyList")
    @PreAuthorize("hasRole('" + IConstant.UserRole.Names.SUPER_ADMIN + "')")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    List<CompanyWorspaceBean> fetchCompanies() throws Exception {
        return companyService.getCompanyList();
    }

    /**
     * REST Api to fetch a list of all users for a company
     * @param companyId the company for which users need to be fetched
     * @return list of all users for the company
     * @throws Exception
     */
    @GetMapping(value = "/fetchUsers")
    @PreAuthorize(("hasRole('" + IConstant.UserRole.Names.SUPER_ADMIN + "') or hasRole('" + IConstant.UserRole.Names.CLIENT_ADMIN + "')"))
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    List<UserWorkspaceBean> fetchUsers(@RequestParam String companyId) throws Exception {
        return userDetailsService.fetchUsers(Long.parseLong(companyId));
    }
}
