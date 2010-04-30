/*
 * Helma License Notice
 *
 * The contents of this file are subject to the Helma License
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://adele.helma.org/download/helma/license.txt
 *
 * Copyright 1998-2003 Helma Software. All Rights Reserved.
 *
 * $RCSfile$
 * $Author$
 * $Revision$
 * $Date$
 */

package helma.image;

import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageFilter;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * Factory class for generating Image objects from various sources.
 *  
 */
public class ImageGenerator {
    protected static ImageGenerator generator = null;

    /**
     * Returns an ImageGenerator singleton, creating it if necessary.
     *
     * @return a new ImageGenerator instance
     */
    public static ImageGenerator getInstance() {
        if (generator == null)
            generator = new ImageGenerator();
        return generator;
    }

    /**
     * @param w ...
     * @param h ...
     * 
     * @return ...
     */
    public ImageWrapper createImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        return new ImageWrapper(img, w, h, this);
    }

    /**
     * @param src ...
     * 
     * @return ...
     * @throws IOException
     */
    public ImageWrapper createImage(byte[] src) throws IOException {
        Image img = read(src);
        return img != null ? new ImageWrapper(img, this) : null;
    }
    
    /**
     * @param filenamne ...
     * 
     * @return ...
     * @throws IOException
     */
    public ImageWrapper createImage(String filenamne)
        throws IOException {
        Image img = read(filenamne);
        return img != null ? new ImageWrapper(img, this) : null;
    }

    /**
     * @param url ...
     * 
     * @return ...
     * @throws MalformedURLException
     * @throws IOException
     */
    public ImageWrapper createImage(URL url)
        throws MalformedURLException, IOException {
        Image img = read(url);
        return img != null ? new ImageWrapper(img, this) : null;
    }

    /**
     * @param input ...
     * @return ...
     * @throws IOException
     */
    public ImageWrapper createImage(InputStream input)
        throws IOException {
        Image img = read(input);
        return img != null ? new ImageWrapper(img, this) : null;
    }


    /**
     * @param iw ...
     * @param filter ...
     * 
     * @return ...
     */
    public ImageWrapper createImage(ImageWrapper iw, ImageFilter filter) {
        // use the ImagFilterOp wrapper for ImageFilters that works directly
        // on BufferedImages. The filtering is much faster like that.
        // Attention: needs testing with all the filters!
        
        return createImage(iw, new ImageFilterOp(filter));
//        Image img = ImageWaiter.waitForImage(
//            Toolkit.getDefaultToolkit().createImage(
//                new FilteredImageSource(iw.getSource(), filter)));
//        return img != null ? new ImageWrapper(img, this) : null;
    }

    /**
     * @param iw ...
     * @param imageOp ...
     * 
     * @return ...
     */
    public ImageWrapper createImage(ImageWrapper iw, BufferedImageOp imageOp) {
        Image img = imageOp.filter(iw.getBufferedImage(), null);
        return img != null ? new ImageWrapper(img, this) : null;
    }

    /**
     * @param filename the filename of the image to create
     *
     * @return the newly created image
     * @throws IOException
     */
    public BufferedImage read(String filename) throws IOException {
        return read(ImageIO.createImageInputStream(new File(filename)));
    }

    /**
     * @param url the URL of the image to create
     *
     * @return the newly created image
     * @throws IOException
     */
    public BufferedImage read(URL url) throws IOException {
        return read(ImageIO.createImageInputStream(url));
    }

    /**
     * @param input the data of the image to create
     *
     * @return the newly created image
     * @throws IOException
     */
    public BufferedImage read(InputStream input) throws IOException {
        return read(ImageIO.createImageInputStream(input));
    }

    /**
     * @param src the data of the image to create
     *
     * @return the newly created image
     * @throws IOException
     */
    public BufferedImage read(byte[] src) throws IOException {
        return read(new ByteArrayInputStream(src));
    }

    public BufferedImage read(ImageInputStream stream) throws IOException {
        Iterator iter = ImageIO.getImageReaders(stream);
        if (!iter.hasNext())
            return null;

        ImageReader reader = (ImageReader) iter.next();
        reader.setInput(stream);
        try {
            // Try reading an image (including color conversion).
            return reader.read(0);
        } catch (IIOException e) {
            // Try reading a Raster (no color conversion)....
            Raster raster = reader.readRaster(0, null);
            // ...and hope thats a CMYK, take a CMYK-Profile
            ICC_Profile profile = ICC_Profile.getInstance(ImageGenerator.class
                    .getClassLoader().getResourceAsStream(
                            "helma/image/cmm/cmyk.icc"));
            //... and convert it to sRGB
            ICC_ColorSpace cmykSpace = new ICC_ColorSpace(profile);
            BufferedImage rgbImage = new BufferedImage(raster.getWidth(),
                    raster.getHeight(), BufferedImage.TYPE_INT_RGB);
            ColorSpace rgbSpace = rgbImage.getColorModel().getColorSpace();
            ColorConvertOp cmykToRgb = new ColorConvertOp(cmykSpace, rgbSpace, null);
            cmykToRgb.filter(raster, rgbImage.getRaster());
            return rgbImage;
        }
    }

    protected void write(ImageWrapper wrapper, ImageWriter writer,
            float quality, boolean alpha) throws IOException {
        // Turn off alpha if we're rendering JPEG
        if (alpha) {
            String[] names = writer.getOriginatingProvider().getFormatNames();
            if (Arrays.asList(names).contains("JPEG"))
                alpha = false;
        }
        BufferedImage bi = wrapper.getBufferedImage();
        // Set some parameters
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed() &&
            quality >= 0.0 && quality <= 1.0) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            String[] types = param.getCompressionTypes();
            // If compression types are defined, but none is set, set the first
            // one, since setCompressionQuality, which requires MODE_EXPLICIT to
            // be set, will complain otherwise.
            if (types != null && param.getCompressionType() == null) {
                param.setCompressionType(types[0]);
            }
            param.setCompressionQuality(quality);
        }
        // Turn off progress mode for now.
        // TODO: make this configurable, through a param object instead of
        // simply passing one alpha param?
        if (param.canWriteProgressive())
            param.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
        // If bi has type ARGB and alpha is false, we have to tell the writer to
        // not use the alpha channel: this is especially needed for Jpeg files
        // where ImageIO seems to produce wrong files right now...
        if (bi.getType() == BufferedImage.TYPE_INT_ARGB
            && !alpha) {
            param.setSourceBands(new int[] { 0,1,2 });
            DirectColorModel cm = (DirectColorModel) bi.getColorModel();
            DirectColorModel newCM = new DirectColorModel(24,
                    cm.getRedMask(), cm.getGreenMask(), cm.getBlueMask());
            param.setDestinationType(new ImageTypeSpecifier(newCM,
                    newCM.createCompatibleSampleModel(1, 1)));
        }
        writer.write(null, new IIOImage(bi, null, null), param);
    }

    /**
     * Saves the image. Image format is deduced from filename.
     * 
     * @param wrapper the image to write
     * @param filename the file to write to
     * @param quality image quality
     * @param alpha to enable alpha
     * @throws IOException
     * @see helma.image.ImageGenerator#write(helma.image.ImageWrapper,
     *      java.lang.String, float, boolean)
     */
    public void write(ImageWrapper wrapper, String filename, float quality,
            boolean alpha) throws IOException {
        // determine suffix:
        int pos = filename.lastIndexOf('.');
        if (pos != -1) {
            String extension = filename.substring(pos + 1, filename.length())
                    .toLowerCase();

            // Find a writer for that file suffix
            ImageWriter writer = null;
            Iterator iter = ImageIO.getImageWritersBySuffix(extension);
            if (iter.hasNext())
                writer = (ImageWriter) iter.next();
            if (writer != null) {
                ImageOutputStream ios = null;
                try {
                    // Prepare output file
                    File file = new File(filename);
                    if (file.exists())
                        file.delete();
                    ios = ImageIO.createImageOutputStream(file);
                    writer.setOutput(ios);
                    this.write(wrapper, writer, quality, alpha);
                 } finally {
                    if (ios != null)
                        ios.close();
                    writer.dispose();
                }
            }
        }
    }

    /**
     * Saves the image. Image format is deduced from type.
     * 
     * @param wrapper the image to write
     * @param out the outputstream to write to
     * @param mimeType the mime type
     * @param quality image quality
     * @param alpha to enable alpha
     * @throws IOException
     * @see helma.image.ImageGenerator#write(helma.image.ImageWrapper,
     *      java.io.OutputStream, java.lang.String, float, boolean)
     */
    public void write(ImageWrapper wrapper, OutputStream out, String mimeType,
            float quality, boolean alpha) throws IOException {
        // Find a writer for that type
        Iterator iter = ImageIO.getImageWritersByMIMEType(mimeType);
        if (iter.hasNext()) {
            ImageWriter writer = (ImageWriter) iter.next();
            ImageOutputStream ios = null;
            try {
                ios = ImageIO.createImageOutputStream(out);
                writer.setOutput(ios);
                this.write(wrapper, writer, quality, alpha);
            } finally {
                if (ios != null)
                    ios.close();
                writer.dispose();
            }
        }
    }
}