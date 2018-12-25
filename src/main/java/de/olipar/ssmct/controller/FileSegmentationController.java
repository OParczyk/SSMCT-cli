package de.olipar.ssmct.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import de.olipar.ssmct.segmentation.ByteSegmenter;
import de.olipar.ssmct.segmentation.Segmenter;
import de.olipar.ssmct.storage.StorageService;

@Controller
public class FileSegmentationController {
	private final StorageService storageService;

	@Autowired
	public FileSegmentationController(StorageService storageService) {
		this.storageService = storageService;
	}

	@PostMapping("/segmentation")
	public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {

		storageService.store(file);
		model.addAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");
		Segmenter<Byte> twoByteSegmenter=new ByteSegmenter(2);
		try {
			model.addAttribute("segments", twoByteSegmenter.getSegments(file.getBytes()));
		} catch (IOException e) {
			model.addAttribute("message", "getBytes failed on file "+file.getOriginalFilename()+"!");
		}
		
		return "segmentation";
	}
}
