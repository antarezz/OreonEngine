package org.oreon.core.util;

import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_info_from_memory;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lwjgl.BufferUtils;
import org.oreon.core.context.BaseOreonContext;
import org.oreon.core.context.ContextHolder;

public class ResourceLoader {

  public static String loadShader(String fileName) {
    InputStream is = ResourceLoader.class.getClassLoader().getResourceAsStream(fileName);

    StringBuilder shaderSource = new StringBuilder();
    BufferedReader shaderReader = null;

    try {
      shaderReader = new BufferedReader(new InputStreamReader(is));
      String line;
      while ((line = shaderReader.readLine()) != null) {
        shaderSource.append(line).append("\n");
      }

      shaderReader.close();
    } catch (Exception e) {
      System.err.println("Unable to load file [" + fileName + "]!");
      e.printStackTrace();
      System.exit(1);
    }

    return shaderSource.toString();
  }

  public static String loadShader(String fileName, String lib) {
    String shadersource = loadShader(fileName);

    InputStream is = ResourceLoader.class.getClassLoader().getResourceAsStream("shader/" + lib);
    StringBuilder shaderlibSource = new StringBuilder();
    BufferedReader shaderReader = null;

    try {
      shaderReader = new BufferedReader(new InputStreamReader(is));
      String line;
      while ((line = shaderReader.readLine()) != null) {
        shaderlibSource.append(line).append("\n");
      }

      shaderReader.close();
    } catch (Exception e) {
      System.err.println("Unable to load file [" + fileName + "]!");
      e.printStackTrace();
      System.exit(1);
    }

    // replace const paramter in glsl library
    String vlib = shaderlibSource.toString().replaceFirst("#var_shadow_map_resolution", Integer.toString(
        ContextHolder.getContext().getConfig().getShadowMapResolution()));

    return shadersource.replaceFirst("#lib.glsl", vlib);
  }

  public static ByteBuffer loadImageToByteBuffer(String file) {
    ByteBuffer imageBuffer;
    try {
      imageBuffer = ioResourceToByteBuffer(file, 128 * 128);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    IntBuffer w = BufferUtils.createIntBuffer(1);
    IntBuffer h = BufferUtils.createIntBuffer(1);
    IntBuffer c = BufferUtils.createIntBuffer(1);

    // Use info to read image metadata without decoding the entire image.
    if (!stbi_info_from_memory(imageBuffer, w, h, c)) {
      throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());
    }

//        System.out.println("Image width: " + w.get(0));
//        System.out.println("Image height: " + h.get(0));
//        System.out.println("Image components: " + c.get(0));
//        System.out.println("Image HDR: " + stbi_is_hdr_from_memory(imageBuffer));

    // Decode the image
    ByteBuffer image = stbi_load_from_memory(imageBuffer, w, h, c, 0);
    if (image == null) {
      throw new RuntimeException("Failed to load image: " + stbi_failure_reason());
    }

    return image;
  }

  public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {

    ByteBuffer buffer;

    Path path = Paths.get(resource);
    if (Files.isReadable(path)) {
      try (SeekableByteChannel fc = Files.newByteChannel(path)) {
        buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
        while (fc.read(buffer) != -1) {
          ;
        }
      }
    } else {
      try (
          InputStream source = ResourceLoader.class.getClassLoader().getResourceAsStream(resource);
          ReadableByteChannel rbc = Channels.newChannel(source)
      ) {
        buffer = BufferUtils.createByteBuffer(bufferSize);

        while (true) {
          int bytes = rbc.read(buffer);
          if (bytes == -1) {
            break;
          }
          if (buffer.remaining() == 0) {
            buffer = BufferUtil.resizeBuffer(buffer, buffer.capacity() * 2);
          }
        }
      }
    }

    buffer.flip();
    return buffer;
  }

}
