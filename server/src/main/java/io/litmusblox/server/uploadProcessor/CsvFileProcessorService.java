/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.service.UploadResponseBean;
import lombok.extern.log4j.Log4j2;
import org.omg.CORBA.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


/**
 * @author : Sumit
 * Date : 19/7/19
 * Time : 3:31 PM
 * Class Name : CsvFileProcessorService
 * Project Name : server
 */
@PropertySource("classpath:appConfig.properties")
@Log4j2
public class CsvFileProcessorService implements IUploadFileProcessorService {

    @Autowired
    IUploadDataProcessService uploadDataProcessor;

    @Autowired
    Environment environment;

    @Transactional
    public List<Candidate> process(String fileName, UploadResponseBean responseBean, boolean ignoreMobile, String repoLocation) {
        List<Candidate> candidateList = new ArrayList<>();
        try {
            Reader fileReader = new FileReader(repoLocation + File.separator + fileName);
            CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(fileReader);
            Map<String, Integer> headers = parser.getHeaderMap();

            if (null == headers.get(IConstant.LITMUSBLOX_FILE_COLUMNS.FirstName.getValue()) || headers.get(IConstant.LITMUSBLOX_FILE_COLUMNS.FirstName.getValue()) != 0 ||
                    null == headers.get(IConstant.LITMUSBLOX_FILE_COLUMNS.LastName.getValue()) || headers.get(IConstant.LITMUSBLOX_FILE_COLUMNS.LastName.getValue()) != 1 ||
                    null == headers.get(IConstant.LITMUSBLOX_FILE_COLUMNS.Email.getValue()) || headers.get(IConstant.LITMUSBLOX_FILE_COLUMNS.Email.getValue()) != 2 ||
                    null == headers.get(IConstant.LITMUSBLOX_FILE_COLUMNS.Mobile.getValue()) || headers.get(IConstant.LITMUSBLOX_FILE_COLUMNS.Mobile.getValue()) != 3) {
                throw new WebException(IErrorMessages.MISSING_COLUMN_NAMES_FIRST_ROW, HttpStatus.UNPROCESSABLE_ENTITY);
            }

            for (CSVRecord record : parser.getRecords()) {
                try {
                    Candidate candidate = new Candidate(record.get(IConstant.LITMUSBLOX_FILE_COLUMNS.FirstName.getValue()).trim(),
                            record.get(IConstant.LITMUSBLOX_FILE_COLUMNS.LastName.getValue()).trim(),
                            record.get(IConstant.LITMUSBLOX_FILE_COLUMNS.Email.getValue()).trim(),
                            record.get(IConstant.LITMUSBLOX_FILE_COLUMNS.Mobile.getValue()).trim());
                    candidate.setCandidateSource(IConstant.CandidateSource.File.getValue());
                    candidateList.add(candidate);
                } catch (Exception pe) {
                    log.error("Error while processing row from CSV file: " + pe.getMessage());
                    Candidate candidate = new Candidate();
                    candidate.setFirstName(record.get(IConstant.LITMUSBLOX_FILE_COLUMNS.FirstName.getValue()).trim());
                    candidate.setLastName(record.get(IConstant.LITMUSBLOX_FILE_COLUMNS.LastName.getValue()).trim());
                    candidate.setEmail(record.get(IConstant.LITMUSBLOX_FILE_COLUMNS.Email.getValue()).trim());
                    if(ignoreMobile) {
                        candidateList.add(candidate);
                    }
                    else {
                        candidate.setUploadErrorMessage(IErrorMessages.MOBILE_NULL_OR_BLANK);
                        responseBean.getFailedCandidates().add(candidate);
                        responseBean.setFailureCount(responseBean.getFailureCount() + 1);
                    }
                }
            }
        } catch(WebException we) {
            log.error("Error while parsing file " + fileName + " :: " + we.getMessage());
            throw we;
        } catch(IOException ioe) {
            log.error("Error while parsing file " + fileName + " :: " + ioe.getMessage());
            responseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        } catch (Exception ex) {
            log.error("Error while processing file " + fileName + " :: " + ex.getMessage());
            responseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        }
        return candidateList;
    }
}