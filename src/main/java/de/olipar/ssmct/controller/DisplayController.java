package de.olipar.ssmct.controller;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.metsci.glimpse.support.colormap.ColorGradients;
import com.metsci.glimpse.support.colormap.ColorMap;

@Controller
@SessionAttributes("comparisonResult")
public class DisplayController {

	@RequestMapping(value = "/display", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	public void createImage(HttpServletRequest request, @ModelAttribute("comparisonResult") int[][] compResult,
			HttpServletResponse response) throws IOException {
		int width = compResult.length;
		int height = compResult.length;
		int[] rgbs = getRGB(compResult);

		DataBuffer rgbData = new DataBufferInt(rgbs, rgbs.length);

		WritableRaster raster = Raster.createPackedRaster(rgbData, width, height, width,
				new int[] { 0xff0000, 0xff00, 0xff }, null);

		ColorModel colorModel = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);

		BufferedImage img = new BufferedImage(colorModel, raster, false, null);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "png", baos);
		baos.flush();
		byte[] imageBytes = baos.toByteArray();
		baos.close();
		IOUtils.copy(new ByteArrayInputStream(imageBytes), response.getOutputStream());
	}

	private static int[] getRGB(int[][] in) {
		ColorMap cm = ColorGradients.viridis;
		int[] ret = new int[in.length * in.length];
		int minimum = min(in);
		int maximum = max(in);
		float[] tempColor = new float[4];
		float range = (maximum - minimum);
		range = Math.max(range, 1);

		for (int i = 0; i < in.length; i++) {
			for (int j = 0; j < in.length; j++) {
//				if (in[i][j] == maximum)
//					ret[i * in.length + j] = 0xFF0000;
//				else if (in[i][j] == minimum)
//					ret[i * in.length + j] = 0x000000;
//				else {
				cm.toColor((in[i][j] - minimum) / range, tempColor);

				ret[i * in.length + j] = convertBack(tempColor);
//				}
			}
		}

		return ret;
	}

	private static int convertBack(float[] in) {
		return (int) ((int) (in[0] * 255) * 0x010000 + (int) (in[1] * 255) * 0x000100 + in[2] * 255 * 0x000001);
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
