package de.olipar.ssmct.controller;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.support.ServletContextResource;

@Controller
@SessionAttributes("comparisonResult")
public class DisplayController {
	@Autowired
	ServletContext context;

	@RequestMapping(value = "/output/output.png", method = RequestMethod.GET)
	public Resource getImageAsResource() {
		return new ServletContextResource(context, "/output/output.png");
	}

	@RequestMapping(value = "/display")
	public String createImage(HttpServletRequest request, @ModelAttribute("comparisonResult") int[][] compResult) {
		int width = compResult.length;
		int height = compResult.length;
		int[] rgbs = getRGB(compResult);

		DataBuffer rgbData = new DataBufferInt(rgbs, rgbs.length);

		WritableRaster raster = Raster.createPackedRaster(rgbData, width, height, width,
				new int[] { 0xff0000, 0xff00, 0xff }, null);

		ColorModel colorModel = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);

		BufferedImage img = new BufferedImage(colorModel, raster, false, null);

		String fname = "./output/output.png";
		try {
			ImageIO.write(img, "png", new File(fname));
		} catch (IOException e) {

			e.printStackTrace();
		}
		System.out.println("wrote to " + fname);
		return ("redirect:/output/output.png");
	}

	private static int[] getRGB(int[][] in) {
		int[] ret = new int[in.length * in.length];
		int minimum = min(in);
		int maximum = max(in);
		float rangeFactor = 1;
		if ((maximum - minimum) - 0xFF < 0)
			rangeFactor = 0xFF / (maximum - minimum);
		else if ((maximum - minimum) - 0xFF > 0)
			rangeFactor = (maximum - minimum) / 0xFF;
		System.out.println(rangeFactor);
		for (int i = 0; i < in.length; i++) {
			for (int j = 0; j < in.length; j++) {
				if (in[i][j] == maximum)
					ret[i * in.length + j] = 0xFF0000;
				else if (in[i][j] == minimum)
					ret[i * in.length + j] = 0x000000;
				else
					ret[i * in.length + j] = (int) ((in[i][j] * 0x010100) * rangeFactor);
			}
		}

		return ret;
	}

	private static int max(int[][] in) {
		int biggest = Integer.MIN_VALUE;
		for (int[] i : in) {
			for (int j : i) {
				if (j > biggest)
					biggest = j;
			}
		}
		return biggest;
	}

	private static int min(int[][] in) {
		int smallest = Integer.MAX_VALUE;
		for (int[] i : in) {
			for (int j : i) {
				if (j < smallest)
					smallest = j;
			}
		}
		return smallest;
	}
}
