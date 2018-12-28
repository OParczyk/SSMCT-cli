package de.olipar.ssmct.controller;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.olipar.ssmct.segmentation.Param;
import de.olipar.ssmct.segmentation.ParameterDisplayType;
import de.olipar.ssmct.segmentation.Segmenter;
import de.olipar.ssmct.storage.StorageFileNotFoundException;
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
		model.addAttribute("BOOL", ParameterDisplayType.BOOL);
		model.addAttribute("file", StringUtils.cleanPath(file.getOriginalFilename()));
		// TODO: ENUM support

		return "segmentation";
	}

	@PostMapping(value = "/segment")
	public String segmentAndCompare(HttpServletRequest request, RedirectAttributes redirectAttrs) {
		Map<String, String[]> payload = request.getParameterMap();

		System.out.println(payload.size());
		for (String i : payload.keySet()) {
			System.out.println(i + " :");
			for (String j : payload.get(i))
				System.out.println("  " + j);
		}
		Class<? extends Segmenter<? extends Comparable<?>>> clazz;
		try {
			clazz = (Class<? extends Segmenter<?>>) Class.forName(payload.get("segmenter")[0]);
		} catch (ClassNotFoundException e1) {
			redirectAttrs.addAttribute("message",
					"Error processing request: " + payload.get("segmenter")[0] + " not Found!");
			e1.printStackTrace();
			return ("redirect:/segmentation");
		}
		Constructor<? extends Segmenter<? extends Comparable<?>>>[] ctors = (Constructor<? extends Segmenter<? extends Comparable<?>>>[]) clazz
				.getConstructors();
		Constructor<? extends Segmenter<? extends Comparable<?>>> constr = null;

		// looks for the first constructor that fits all given parameters

		constructor: for (Constructor<? extends Segmenter<? extends Comparable<?>>> i : ctors) {
			Parameter[] cParams = i.getParameters();
			if (cParams.length != payload.size() - 2)
				continue;
			for (Parameter j : cParams) {
				if (!payload.containsKey(j.getName()))
					continue constructor;
			}
			constr = i;
			break;
		}

		// Create arguments for found constructor from post data
		ArrayList<Object> params = new ArrayList<Object>();
		for (Parameter i : constr.getParameters()) {
			String payloadContent = payload.get(i.getName())[0];
			switch (i.getAnnotation(Param.class).type()) {
			case BOOLEAN:
				if (payload.get(i.getName()).length>1)
					params.add(new Boolean(true));
				else
					params.add(new Boolean(false));
				break;
			case INT:
				params.add(new Integer(payloadContent));
				break;
			case LONG:
				params.add(new Long(payloadContent));
				break;
			case STRING:
				params.add(payloadContent);
				break;
			default:
				redirectAttrs.addAttribute("message", "invalid ParameterType in " + i.getName());
				break;
			}
		}
		Comparable[][] segments;
		Path filepath = storageService.load(payload.get("filename")[0]);
		// Create an instance of requested segmenter
		try {
			Segmenter<? extends Comparable<?>> segmenter = constr.newInstance(params.toArray());
			// and segment the file
			segments = segmenter.getSegments(Files.readAllBytes(filepath));
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			redirectAttrs.addAttribute("message", "Error parsing given Parameters");
			e.printStackTrace();
			return ("redirect:/segmentation");
		} catch (IOException e) {
			redirectAttrs.addAttribute("message", "Error reading given file");
			e.printStackTrace();
			return ("redirect:/segmentation");
		}
		
		redirectAttrs.addAttribute("segments", segments);

		return ("redirect:/compare");
	}
}
