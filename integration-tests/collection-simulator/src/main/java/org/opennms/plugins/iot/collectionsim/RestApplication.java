package org.opennms.plugins.iot.collectionsim;

import org.opennms.plugins.iot.collectionsim.model.LogToFileResponse;
import org.opennms.plugins.iot.collectionsim.model.SystemCpuStatsLogRecord;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;


@RestController
@RequestMapping("/nokia" )
public class RestApplication {

	@Autowired
	CollectionSimService collectionSimService;

	@GetMapping(value="/data", produces=MediaType.APPLICATION_XML_VALUE)
	public LogToFileResponse getResponse(@RequestParam(required = false, name="count") Integer n) {

		LogToFileResponse logToFileResponse = collectionSimService.getResponse(n);

		return logToFileResponse;
	}

}