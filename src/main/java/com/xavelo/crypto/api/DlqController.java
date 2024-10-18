package com.xavelo.crypto.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xavelo.crypto.service.DlqReprocessorService;

import java.util.List;

@RestController
@RequestMapping("/api/dlq")
public class DlqController {

    private static final Logger logger = LoggerFactory.getLogger(DlqController.class);

    private DlqReprocessorService dlqReprocessorService;

    public DlqController(DlqReprocessorService dlqReprocessorService) {
        this.dlqReprocessorService = dlqReprocessorService;
    }
    
    @PostMapping("/reprocess")
    public ResponseEntity<List<String>> reProcessRecords(@RequestParam int numberOfRecords) {
        logger.info("--------------> dlq reprocess {} DLQ records", numberOfRecords);
        List<String> records = dlqReprocessorService.reprocessDlqMessages(numberOfRecords);
        return ResponseEntity.ok(records);
        
    }

}


