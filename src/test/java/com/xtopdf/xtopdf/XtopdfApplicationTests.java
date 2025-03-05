package com.xtopdf.xtopdf;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.xtopdf.xtopdf.controllers.FileConversionController;
import com.xtopdf.xtopdf.services.FileConversionService;

@SpringBootTest
class XtopdfApplicationTests {

	
	@Autowired
	private FileConversionController fileConversionController;

	@Autowired
	private FileConversionService fileConversionService;

	@Test
	void contextLoads() {
		assertNotNull(fileConversionController);
		assertNotNull(fileConversionService);
	}

}
