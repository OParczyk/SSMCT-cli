package de.olipar.ssmct.controller;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.reflections.Reflections;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import de.olipar.ssmct.annotation.Param;
import de.olipar.ssmct.annotation.ParameterDisplayType;
import de.olipar.ssmct.segmentation.Segmenter;

@Controller
@SessionAttributes("inputBytes")
public class FileSegmentationController {
	private Reflections segmentersReflection = new Reflections("de.olipar.ssmct.segmentation");
	private Set<Class<? extends Segmenter>> segmenterClasses = segmentersReflection.getSubTypesOf(Segmenter.class);

	@PostMapping("/segmentation")
	public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model, HttpServletRequest request)
			throws IOException {
		request.getSession().setAttribute("inputBytes", file.getBytes());

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
		// TODO: ENUM support

		return "segmentation";
	}

	@PostMapping(value = "/segment")
	public RedirectView segment(HttpServletRequest request, RedirectAttributes redirectAttrs,
			@ModelAttribute("inputBytes") byte[] inputBytes) {
		Map<String, String[]> payload = request.getParameterMap();
		Comparable<?>[][] segments;
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
			return new RedirectView("segmentation");
		}
		Constructor<? extends Segmenter<? extends Comparable<?>>>[] ctors = (Constructor<? extends Segmenter<? extends Comparable<?>>>[]) clazz
				.getConstructors();
		Constructor<? extends Segmenter<? extends Comparable<?>>> constr = null;

		// looks for the first constructor that fits all given parameters

		constructor: for (Constructor<? extends Segmenter<? extends Comparable<?>>> i : ctors) {
			Parameter[] cParams = i.getParameters();
			if (cParams.length != payload.size() - 1)
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
				if (payload.get(i.getName()).length > 1)
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
		// Create an instance of requested segmenter
		try {
			Segmenter<? extends Comparable<?>> segmenter = constr.newInstance(params.toArray());
			// and segment the file
			segments = segmenter.getSegments(inputBytes);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			redirectAttrs.addAttribute("message", "Error parsing given Parameters");
			e.printStackTrace();
			return new RedirectView("segmentation");
		}

		request.getSession().setAttribute("segments", segments);
		return new RedirectView("comparison", false);
	}
}
