package de.olipar.ssmct.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.reflections.Reflections;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import de.olipar.ssmct.annotation.Param;
import de.olipar.ssmct.annotation.ParameterDisplayType;
import de.olipar.ssmct.comparison.TheComparator;

@Controller
@SessionAttributes("segments")
public class CompareController {
	private Reflections comparatorsReflection = new Reflections("de.olipar.ssmct.comparison");
	private Set<Class<? extends Comparator>> comparatorClasses = comparatorsReflection.getSubTypesOf(Comparator.class);

	@RequestMapping("/comparison")
	public String compare(Model model, HttpServletRequest request,
			@ModelAttribute("segments") Comparable<?>[][] segments) {
		Map<Class<? extends Comparator>, Constructor[]> comparators = new HashMap<Class<? extends Comparator>, Constructor[]>();
		Map<Parameter, Annotation[]> annotations = new HashMap<Parameter, Annotation[]>();

		for (Class<? extends Comparator> i : comparatorClasses) {
			comparators.put(i, i.getConstructors());
		}

		model.addAttribute("comparators", comparators);
		model.addAttribute("paramClass", Param.class);
		model.addAttribute("NUMBER", ParameterDisplayType.NUMBER);
		model.addAttribute("STRING", ParameterDisplayType.STRING);
		model.addAttribute("BOOL", ParameterDisplayType.BOOL);
		// TODO: ENUM support

		return ("comparison");
	}

	@PostMapping(value = "/compare")
	public RedirectView segment(HttpServletRequest request, RedirectAttributes redirectAttrs,
			@ModelAttribute("segments") Comparable<?>[][] segments) {
		Map<String, String[]> payload = request.getParameterMap();

		System.out.println(payload.size());
		for (String i : payload.keySet()) {
			System.out.println(i + " :");
			for (String j : payload.get(i))
				System.out.println("  " + j);
		}
		Class<? extends Comparator<Comparable<?>[]>> clazz;
		try {
			clazz = (Class<? extends Comparator<Comparable<?>[]>>) Class.forName(payload.get("comparator")[0]);
		} catch (ClassNotFoundException e1) {
			redirectAttrs.addAttribute("message",
					"Error processing request: " + payload.get("comparator")[0] + " not Found!");
			e1.printStackTrace();
			return new RedirectView("comparison");
		}
		Constructor<? extends Comparator<Comparable<?>[]>>[] ctors = (Constructor<? extends Comparator<Comparable<?>[]>>[]) clazz
				.getConstructors();
		Constructor<? extends Comparator<Comparable<?>[]>> constr = null;

		// looks for the first constructor that fits all given parameters

		constructor: for (Constructor<? extends Comparator<Comparable<?>[]>> i : ctors) {
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
			case BYTE:
				params.add(new Byte(payloadContent));
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
		// Create an instance of requested comparator
		int[][] comparisonResult = null;
		try {
			Comparator<Comparable<?>[]> comparator = constr.newInstance(params.toArray());
			// and compare the segments
			comparisonResult = TheComparator.compare(comparator, segments);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			redirectAttrs.addAttribute("message", "Error parsing given Parameters");
			e.printStackTrace();
			return new RedirectView("comparison");
		}

		request.getSession().setAttribute("comparisonResult", comparisonResult);
		return new RedirectView("display", false);
	}

}
