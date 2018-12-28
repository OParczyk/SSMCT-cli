package de.olipar.ssmct.controller;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import de.olipar.ssmct.segmentation.ByteSegmenter;
import de.olipar.ssmct.segmentation.Param;
import de.olipar.ssmct.segmentation.ParameterDisplayType;
import de.olipar.ssmct.segmentation.Segmenter;
import de.olipar.ssmct.storage.StorageService;

@Controller
public class FileSegmentationController {
	private final StorageService storageService;
	private Reflections segmentersReflection = new Reflections("de.olipar.ssmct.segmentation");
	private Set<Class<? extends Segmenter>> segmenterClasses = segmentersReflection.getSubTypesOf(Segmenter.class);

	@Autowired
	public FileSegmentationController(StorageService storageService) {
		this.storageService = storageService;
	}

	@PostMapping("/segmentation")
	public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
		storageService.store(file);

		Map<Class<? extends Segmenter>, Constructor[]> segmenters = new HashMap<Class<? extends Segmenter>, Constructor[]>();
		Map<Parameter, Annotation[]> annotations = new HashMap<Parameter, Annotation[]>();

		for (Class<? extends Segmenter> i : segmenterClasses) {
			segmenters.put(i, i.getConstructors());
//			System.out.println(i.getConstructors()[0].getParameters()[0].getAnnotations().length);
		}

		model.addAttribute("segmenters", segmenters);
		model.addAttribute("paramClass", Param.class);
		model.addAttribute("NUMBER", ParameterDisplayType.NUMBER);
		model.addAttribute("STRING", ParameterDisplayType.STRING);
		model.addAttribute("BOOL",ParameterDisplayType.BOOL);
		//TODO: ENUM support

		return "segmentation";
	}
}
