package org.oreon.core.gl.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.IntBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;
import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.gl.wrapper.texture.TextureImage2D;
import org.oreon.core.image.Image.SamplerFilter;
import org.oreon.core.math.Vec2f;
import org.oreon.core.math.Vec3f;
import org.oreon.core.model.Material;
import org.oreon.core.model.Mesh;
import org.oreon.core.model.Model;
import org.oreon.core.model.Vertex;
import org.oreon.core.util.Util;

public class GLAssimpModelLoader {

  private static FileSystem tempFileSystem = null;

  public static List<Model> loadModel(String path, String file) {

    List<Model> models = new ArrayList<>();
    List<Material> materials = new ArrayList<>();

    String tmpPath;

    boolean fromJar = false;
    URL vPathUrl = GLAssimpModelLoader.class.getResource("/" + path);

    if (vPathUrl.getProtocol().equals("jar")) {
      fromJar = true;
    }

    // if jar, copy directory to temp folder
    if (fromJar) {

      Path currentRelativePath = Paths.get("");
      String currentAbsolutePath = currentRelativePath.toAbsolutePath().toString();

      // create temp directory
      File directory = new File(currentAbsolutePath + "/temp");
      if (!directory.exists()) {
        directory.mkdir();
        directory.deleteOnExit();
      }

      tmpPath = createTempFile(path, file, currentAbsolutePath);

      URI uri = null;
      try {
        uri = GLAssimpModelLoader.class.getResource("/" + path).toURI();
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }

      try {
        if (tempFileSystem == null) {
          tempFileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
        }

        Path myPath = tempFileSystem.getPath("/" + path);

        for (Iterator<Path> it = Files.walk(myPath, 1).iterator(); it.hasNext(); ) {
          Path p = it.next();
          if (!Files.isDirectory(p)) {
            createTempFile(path, p.getFileName().toString(), currentAbsolutePath);
          }
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {

      tmpPath = GLAssimpModelLoader.class.getClassLoader().getResource(path + "/" + file).getPath().toString();

      // For Linux need to keep '/' or else the Assimp.aiImportFile(...) call below returns null!
      if (System.getProperty("os.name").contains("Windows")) {
        if (tmpPath.startsWith("/")) {
          tmpPath = tmpPath.substring(1);
        }
      }
    }

    AIScene aiScene = Assimp.aiImportFile(tmpPath, 0);

    if (aiScene.mMaterials() != null) {
      for (int i = 0; i < aiScene.mNumMaterials(); i++) {
        AIMaterial aiMaterial = AIMaterial.create(aiScene.mMaterials().get(i));
        Material material = processMaterial(aiMaterial, path);
        materials.add(material);
      }
    }

    for (int i = 0; i < aiScene.mNumMeshes(); i++) {
      AIMesh aiMesh = AIMesh.create(aiScene.mMeshes().get(i));
      Mesh mesh = processMesh(aiMesh);
      Model model = new Model();
      model.setMesh(mesh);
      int materialIndex = aiMesh.mMaterialIndex();
      model.setMaterial(materials.get(materialIndex));
      models.add(model);
    }

    return models;
  }

  private static Mesh processMesh(AIMesh aiMesh) {

    List<Vertex> vertexList = new ArrayList<>();
    List<Integer> indices = new ArrayList<>();

    List<Vec3f> vertices = new ArrayList<>();
    List<Vec2f> texCoords = new ArrayList<>();
    List<Vec3f> normals = new ArrayList<>();
    List<Vec3f> tangents = new ArrayList<>();
    List<Vec3f> bitangents = new ArrayList<>();

    AIVector3D.Buffer aiVertices = aiMesh.mVertices();
    while (aiVertices.remaining() > 0) {
      AIVector3D aiVertex = aiVertices.get();
      vertices.add(new Vec3f(aiVertex.x(), aiVertex.y(), aiVertex.z()));
    }

    AIVector3D.Buffer aiTexCoords = aiMesh.mTextureCoords(0);
    if (aiTexCoords != null) {
      while (aiTexCoords.remaining() > 0) {
        AIVector3D aiTexCoord = aiTexCoords.get();
        texCoords.add(new Vec2f(aiTexCoord.x(), aiTexCoord.y()));
      }
    }

    AIVector3D.Buffer aiNormals = aiMesh.mNormals();
    if (aiNormals != null) {
      while (aiNormals.remaining() > 0) {
        AIVector3D aiNormal = aiNormals.get();
        normals.add(new Vec3f(aiNormal.x(), aiNormal.y(), aiNormal.z()));
      }
    }
    AIVector3D.Buffer aiTangents = aiMesh.mTangents();
    if (aiTangents != null) {
      while (aiTangents.remaining() > 0) {
        AIVector3D aiTangent = aiTangents.get();
        tangents.add(new Vec3f(aiTangent.x(), aiTangent.y(), aiTangent.z()));
      }
    }

    AIVector3D.Buffer aiBitangents = aiMesh.mBitangents();
    if (aiBitangents != null) {
      while (aiBitangents.remaining() > 0) {
        AIVector3D aiBitangent = aiBitangents.get();
        bitangents.add(new Vec3f(aiBitangent.x(), aiBitangent.y(), aiBitangent.z()));
      }
    }

    AIFace.Buffer aifaces = aiMesh.mFaces();
    while (aifaces.remaining() > 0) {
      AIFace aiface = aifaces.get();

      if (aiface.mNumIndices() == 3) {
        IntBuffer indicesBuffer = aiface.mIndices();
        indices.add(indicesBuffer.get(0));
        indices.add(indicesBuffer.get(1));
        indices.add(indicesBuffer.get(2));
      }
      if (aiface.mNumIndices() == 4) {
        IntBuffer indicesBuffer = aiface.mIndices();
        indices.add(indicesBuffer.get(0));
        indices.add(indicesBuffer.get(1));
        indices.add(indicesBuffer.get(2));
        indices.add(indicesBuffer.get(0));
        indices.add(indicesBuffer.get(1));
        indices.add(indicesBuffer.get(3));
        indices.add(indicesBuffer.get(1));
        indices.add(indicesBuffer.get(2));
        indices.add(indicesBuffer.get(3));
      }

    }

    for (int i = 0; i < vertices.size(); i++) {
      Vertex vertex = new Vertex();
      vertex.setPosition(vertices.get(i));
      if (!normals.isEmpty()) {
        vertex.setNormal(normals.get(i));
      } else {
        vertex.setNormal(new Vec3f(0, 0, 0));
      }
      if (!texCoords.isEmpty()) {
        vertex.setUVCoord(texCoords.get(i));
      } else {
        vertex.setUVCoord(new Vec2f(0, 0));
      }
      if (!tangents.isEmpty()) {
        vertex.setTangent(tangents.get(i));
      }
      if (!bitangents.isEmpty()) {
        vertex.setBitangent(bitangents.get(i));
      }
      vertexList.add(vertex);
    }

    Vertex[] vertexData = Util.toVertexArray(vertexList);
    int[] facesData = Util.toIntArray(indices);

    return new Mesh(vertexData, facesData);
  }

  private static Material processMaterial(AIMaterial aiMaterial, String texturesDir) {

    // diffuse Texture
    AIString diffPath = AIString.calloc();
    Assimp.aiGetMaterialTexture(aiMaterial, Assimp.aiTextureType_DIFFUSE, 0, diffPath, (IntBuffer) null, null, null,
        null, null, null);
    String diffTexPath = diffPath.dataString();

    GLTexture diffuseTexture = null;
    if (diffTexPath != null && diffTexPath.length() > 0) {
      diffuseTexture = new TextureImage2D(texturesDir + "/" + diffTexPath, SamplerFilter.Trilinear);
    }

    // normal Texture
    AIString normalPath = AIString.calloc();
    Assimp.aiGetMaterialTexture(aiMaterial, Assimp.aiTextureType_NORMALS, 0, normalPath, (IntBuffer) null, null, null,
        null, null, null);
    String normalTexPath = normalPath.dataString();

    GLTexture normalTexture = null;
    if (normalTexPath != null && normalTexPath.length() > 0) {
      normalTexture = new TextureImage2D(texturesDir + "/" + normalTexPath, SamplerFilter.Trilinear);
    }

    AIColor4D color = AIColor4D.create();

    Vec3f diffuseColor = null;
    int result = Assimp.aiGetMaterialColor(aiMaterial, Assimp.AI_MATKEY_COLOR_AMBIENT, Assimp.aiTextureType_NONE, 0,
        color);
    if (result == 0) {
      diffuseColor = new Vec3f(color.r(), color.g(), color.b());
    }

    Material material = new Material();
    material.setDiffusemap(diffuseTexture);
    material.setNormalmap(normalTexture);
    material.setColor(diffuseColor);

    return material;
  }

  public static String createTempFile(String path, String file, String p) {

    System.out.println("Path " + path);
    System.out.println("File " + file);
    System.out.println("p " + p);

    try {
      InputStream input = GLAssimpModelLoader.class.getResourceAsStream("/" + path + "/" + file);
      File tmpFile = new File(p + "/temp/" + file);

      OutputStream out = new FileOutputStream(tmpFile);

      int read;
      byte[] bytes = new byte[1024];

      while ((read = input.read(bytes)) != -1) {
        out.write(bytes, 0, read);
      }
      out.close();
      tmpFile.deleteOnExit();

      return tmpFile.getAbsolutePath();

    } catch (Exception e) {
      e.printStackTrace();
    }

    return "";
  }

}
