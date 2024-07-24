package org.oreon.core.gl.pipeline;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VALIDATE_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glValidateProgram;
import static org.lwjgl.opengl.GL30.glBindFragDataLocation;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40.GL_TESS_CONTROL_SHADER;
import static org.lwjgl.opengl.GL40.GL_TESS_EVALUATION_SHADER;
import static org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER;

import java.util.HashMap;

import org.oreon.core.gl.texture.GLTexture;
import org.oreon.core.math.Matrix4f;
import org.oreon.core.math.Vec2f;
import org.oreon.core.math.Vec3f;
import org.oreon.core.math.Vec4f;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.util.BufferUtil;

public abstract class GLShaderProgram {

  private int program;
  private HashMap<String, Integer> uniforms;

  public GLShaderProgram() {
    program = glCreateProgram();
    uniforms = new HashMap<String, Integer>();

    if (program == 0) {
      System.err.println("Shader creation failed");
      System.exit(1);
    }
  }

  public void bind() {
    glUseProgram(program);
  }

  public void updateUniforms(Renderable object) {
  }

  ;

  public <T> void updateUniforms(Class<T> clazz) {
  }

  ;

  public void updateUniforms(Matrix4f matrix0, Matrix4f matrix1, Matrix4f matrix2) {
  }

  ;

  public void updateUniforms(GLTexture texture, int i, float j) {
  }

  ;

  public void updateUniforms(GLTexture texture, float j) {
  }

  ;

  public void updateUniforms(GLTexture texture) {
  }

  ;

  public void updateUniforms(Matrix4f matrix0, Matrix4f matrix1) {
  }

  ;

  public void updateUniforms(Matrix4f matrix) {
  }

  ;

  public void updateUniforms(Vec4f vec4) {
  }

  ;

  public void updateUniforms(int value) {
  }

  ;

  public void updateUniforms(float value) {
  }

  ;

  public void updateUniforms(float value0, float value1) {
  }

  ;

  public void updateUniforms(int l, int n, float t) {
  }

  ;

  public void updateUniforms(int l, int n, int t) {
  }

  ;

  public void updateUniforms(int n, int l, float a, Vec2f w, float j) {
  }

  public void updateUniforms(int n, int l, float a, Vec2f w, float k, float v, float j) {
  }

  public void updateUniforms(int i, int j, int k, int l) {
  }

  public void updateUniforms(int n, int pingpong) {
  }

  public void updateUniforms(int i, float j) {
  }


  public void addUniform(String uniform) {
    int uniformLocation = glGetUniformLocation(program, uniform);

    if (uniformLocation == 0xFFFFFFFF) {
      System.err.println(this.getClass().getName() + " Error: Could not find uniform: " + uniform);
      new Exception().printStackTrace();
      System.exit(1);
    }

    uniforms.put(uniform, uniformLocation);
  }

  public void addUniformBlock(String uniform) {
    int uniformLocation = glGetUniformBlockIndex(program, uniform);
    if (uniformLocation == 0xFFFFFFFF) {
      System.err.println(this.getClass().getName() + " Error: Could not find uniform: " + uniform);
      new Exception().printStackTrace();
      System.exit(1);
    }

    uniforms.put(uniform, uniformLocation);
  }

  public void addVertexShader(String text) {
    addProgram(text, GL_VERTEX_SHADER);
  }

  public void addGeometryShader(String text) {
    addProgram(text, GL_GEOMETRY_SHADER);
  }

  public void addFragmentShader(String text) {
    addProgram(text, GL_FRAGMENT_SHADER);
  }

  public void addTessellationControlShader(String text) {
    addProgram(text, GL_TESS_CONTROL_SHADER);
  }

  public void addTessellationEvaluationShader(String text) {
    addProgram(text, GL_TESS_EVALUATION_SHADER);
  }

  public void addComputeShader(String text) {
    addProgram(text, GL_COMPUTE_SHADER);
  }

  public void compileShader() {
    glLinkProgram(program);

    if (glGetProgrami(program, GL_LINK_STATUS) == 0) {
      System.out.println(this.getClass().getName() + " " + glGetProgramInfoLog(program, 1024));
      System.exit(1);
    }

    glValidateProgram(program);

    if (glGetProgrami(program, GL_VALIDATE_STATUS) == 0) {
      System.err.println(this.getClass().getName() + " " + glGetProgramInfoLog(program, 1024));
      System.exit(1);
    }
  }

  private void addProgram(String text, int type) {
    int shader = glCreateShader(type);

    if (shader == 0) {
      System.err.println(this.getClass().getName() + " Shader creation failed");
      System.exit(1);
    }

    glShaderSource(shader, text);
    glCompileShader(shader);

    if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
      System.err.println(this.getClass().getName() + " " + glGetShaderInfoLog(shader, 1024));
      System.exit(1);
    }

    glAttachShader(program, shader);
  }

  public void setUniformi(String uniformName, int value) {
    glUniform1i(uniforms.get(uniformName), value);
  }

  public void setUniformf(String uniformName, float value) {
    glUniform1f(uniforms.get(uniformName), value);
  }

  public void setUniform(String uniformName, Vec2f value) {
    glUniform2f(uniforms.get(uniformName), value.getX(), value.getY());
  }

  public void setUniform(String uniformName, Vec3f value) {
    glUniform3f(uniforms.get(uniformName), value.getX(), value.getY(), value.getZ());
  }

  public void setUniform(String uniformName, Vec4f value) {
    glUniform4f(uniforms.get(uniformName), value.getX(), value.getY(), value.getZ(), value.getW());
  }

  public void setUniform(String uniformName, Matrix4f value) {
    glUniformMatrix4fv(uniforms.get(uniformName), true, BufferUtil.createFlippedBuffer(value));
  }

  public void bindUniformBlock(String uniformBlockName, int uniformBlockBinding) {
    glUniformBlockBinding(program, uniforms.get(uniformBlockName), uniformBlockBinding);
  }

  public void bindFragDataLocation(String name, int index) {
    glBindFragDataLocation(program, index, name);
  }

  public int getProgram() {
    return program;
  }
}
