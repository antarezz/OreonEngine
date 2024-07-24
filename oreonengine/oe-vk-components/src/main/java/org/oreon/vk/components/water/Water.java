package org.oreon.vk.components.water;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_MEMORY_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DEPENDENCY_BY_REGION_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D16_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16B16A16_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_ALL_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_EXTERNAL;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Getter;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueue;
import org.oreon.common.water.WaterConfig;
import org.oreon.core.context.ContextHolder;
import org.oreon.core.math.Vec2f;
import org.oreon.core.math.Vec3f;
import org.oreon.core.math.Vec4f;
import org.oreon.core.model.Vertex.VertexLayout;
import org.oreon.core.scenegraph.NodeComponentType;
import org.oreon.core.scenegraph.RenderList;
import org.oreon.core.scenegraph.Renderable;
import org.oreon.core.scenegraph.Scenegraph;
import org.oreon.core.target.FrameBufferObject.Attachment;
import org.oreon.core.util.BufferUtil;
import org.oreon.core.util.Constants;
import org.oreon.core.util.MeshGenerator;
import org.oreon.core.util.Util;
import org.oreon.core.vk.command.CommandBuffer;
import org.oreon.core.vk.command.SubmitInfo;
import org.oreon.core.vk.context.DeviceManager.DeviceType;
import org.oreon.core.vk.context.VkOreonContext;
import org.oreon.core.vk.descriptor.DescriptorPool;
import org.oreon.core.vk.descriptor.DescriptorSet;
import org.oreon.core.vk.descriptor.DescriptorSetLayout;
import org.oreon.core.vk.device.LogicalDevice;
import org.oreon.core.vk.device.VkDeviceBundle;
import org.oreon.core.vk.framebuffer.FrameBufferColorAttachment;
import org.oreon.core.vk.framebuffer.FrameBufferDepthAttachment;
import org.oreon.core.vk.framebuffer.VkFrameBuffer;
import org.oreon.core.vk.framebuffer.VkFrameBufferObject;
import org.oreon.core.vk.image.VkImage;
import org.oreon.core.vk.image.VkImageView;
import org.oreon.core.vk.image.VkSampler;
import org.oreon.core.vk.memory.VkBuffer;
import org.oreon.core.vk.pipeline.RenderPass;
import org.oreon.core.vk.pipeline.ShaderPipeline;
import org.oreon.core.vk.pipeline.VkPipeline;
import org.oreon.core.vk.pipeline.VkVertexInput;
import org.oreon.core.vk.scenegraph.VkMeshData;
import org.oreon.core.vk.scenegraph.VkRenderInfo;
import org.oreon.core.vk.synchronization.Fence;
import org.oreon.core.vk.util.VkUtil;
import org.oreon.core.vk.wrapper.buffer.VkBufferHelper;
import org.oreon.core.vk.wrapper.buffer.VkUniformBuffer;
import org.oreon.core.vk.wrapper.command.PrimaryCmdBuffer;
import org.oreon.core.vk.wrapper.command.SecondaryDrawCmdBuffer;
import org.oreon.core.vk.wrapper.image.VkImageBundle;
import org.oreon.core.vk.wrapper.image.VkImageHelper;
import org.oreon.core.vk.wrapper.pipeline.GraphicsTessellationPipeline;
import org.oreon.vk.components.fft.FFT;
import org.oreon.vk.components.util.NormalRenderer;

public class Water extends Renderable {

  @Getter
  private WaterConfig waterConfig;

  private long systemTime = System.currentTimeMillis();
  private FFT fft;
  private NormalRenderer normalRenderer;
  private Vec4f clipplane;
  private float clip_offset;
  private float motion;
  private float distortion;
  private VkImage image_dudv;
  private VkImageView imageView_dudv;

  private VkUniformBuffer uniformBuffer;

  private VkSampler dxSampler;
  private VkSampler dySampler;
  private VkSampler dzSampler;
  private VkSampler dudvSampler;
  private VkSampler normalSampler;
  private VkSampler reflectionSampler;
  private VkSampler refractionSampler;

  // Reflection/Refraction Resources
  private VkFrameBufferObject reflectionFbo;
  private VkFrameBufferObject refractionFbo;

  private RenderList offScreenReflectionRenderList;
  private LinkedHashMap<String, CommandBuffer> reflectionSecondaryCmdBuffers;
  private PrimaryCmdBuffer offscreenReflectionCmdBuffer;
  private SubmitInfo offScreenReflectionSubmitInfo;
  private Fence reflectionFence;

  private RenderList offScreenRefractionRenderList;
  private LinkedHashMap<String, CommandBuffer> refractionSecondaryCmdBuffers;
  private PrimaryCmdBuffer offscreenRefractionCmdBuffer;
  private SubmitInfo offScreenRefractionSubmitInfo;
  private Fence refractionFence;

  // queues for render reflection/refraction
  private VkQueue graphicsQueue;

  public Water() {
    final VkOreonContext context = (VkOreonContext) ContextHolder.getContext();

    VkDeviceBundle deviceBundle = context.getDeviceManager().getDeviceBundle(DeviceType.MAJOR_GRAPHICS_DEVICE);
    LogicalDevice device = deviceBundle.getLogicalDevice();
    DescriptorPool descriptorPool = device.getDescriptorPool(Thread.currentThread().getId());
    VkPhysicalDeviceMemoryProperties memoryProperties =
        context.getDeviceManager().getPhysicalDevice(DeviceType.MAJOR_GRAPHICS_DEVICE).getMemoryProperties();
    graphicsQueue = device.getGraphicsQueue();

    offScreenReflectionRenderList = new RenderList();
    offScreenRefractionRenderList = new RenderList();
    reflectionSecondaryCmdBuffers = new LinkedHashMap<String, CommandBuffer>();
    refractionSecondaryCmdBuffers = new LinkedHashMap<String, CommandBuffer>();
    reflectionFbo = new ReflectionRefractionFbo(device.getHandle(), memoryProperties);
    refractionFbo = new ReflectionRefractionFbo(device.getHandle(), memoryProperties);
    context.getResources().setReflectionFbo(reflectionFbo);
    context.getResources().setRefractionFbo(refractionFbo);

    getWorldTransform().setScaling(Constants.ZFAR, 1, Constants.ZFAR);
    getWorldTransform().setTranslation(-Constants.ZFAR / 2, 0, -Constants.ZFAR / 2);

    clip_offset = 4;
    clipplane = new Vec4f(0, -1, 0, getWorldTransform().getTranslation().getY() + clip_offset);

    waterConfig = new WaterConfig();
    waterConfig.loadFile("water-config.properties");

    image_dudv = VkImageHelper.loadImageFromFileMipmap(
        device.getHandle(), memoryProperties,
        device.getGraphicsCommandPool(Thread.currentThread().getId()).getHandle(),
        device.getGraphicsQueue(),
        "textures/water/dudv/dudv1.jpg",
        VK_IMAGE_USAGE_SAMPLED_BIT,
        VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
        VK_ACCESS_SHADER_READ_BIT,
        VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
        VK_QUEUE_FAMILY_IGNORED);

    imageView_dudv = new VkImageView(device.getHandle(),
        VK_FORMAT_R8G8B8A8_UNORM, image_dudv.getHandle(),
        VK_IMAGE_ASPECT_COLOR_BIT, Util.getMipLevelCount(image_dudv.getMetaData()));

    dySampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR, false, 0,
        VK_SAMPLER_MIPMAP_MODE_NEAREST, 0, VK_SAMPLER_ADDRESS_MODE_REPEAT);
    dxSampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR, false, 0,
        VK_SAMPLER_MIPMAP_MODE_NEAREST, 0, VK_SAMPLER_ADDRESS_MODE_REPEAT);
    dzSampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR, false, 0,
        VK_SAMPLER_MIPMAP_MODE_NEAREST, 0, VK_SAMPLER_ADDRESS_MODE_REPEAT);
    dudvSampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR, false, 0,
        VK_SAMPLER_MIPMAP_MODE_LINEAR, Util.getMipLevelCount(image_dudv.getMetaData()),
        VK_SAMPLER_ADDRESS_MODE_REPEAT);
    normalSampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR, false, 0,
        VK_SAMPLER_MIPMAP_MODE_LINEAR, Util.getLog2N(waterConfig.getN()),
        VK_SAMPLER_ADDRESS_MODE_REPEAT);
    reflectionSampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR, false, 0,
        VK_SAMPLER_MIPMAP_MODE_LINEAR, Util.getLog2N(reflectionFbo.getWidth()),
        VK_SAMPLER_ADDRESS_MODE_REPEAT);
    refractionSampler = new VkSampler(device.getHandle(), VK_FILTER_LINEAR, false, 0,
        VK_SAMPLER_MIPMAP_MODE_LINEAR, Util.getLog2N(refractionFbo.getWidth()),
        VK_SAMPLER_ADDRESS_MODE_REPEAT);

    fft = new FFT(deviceBundle,
        waterConfig.getN(), waterConfig.getL(), waterConfig.getT_delta(),
        waterConfig.getAmplitude(), waterConfig.getWindDirection(),
        waterConfig.getWindSpeed(), waterConfig.getCapillarWavesSupression());

    normalRenderer = new NormalRenderer(
        context.getDeviceManager().getDeviceBundle(DeviceType.MAJOR_GRAPHICS_DEVICE),
        waterConfig.getN(), waterConfig.getNormalStrength(),
        fft.getDyImageView(), dySampler);

    normalRenderer.setWaitSemaphores(fft.getFftSignalSemaphore().getHandlePointer());

    ShaderPipeline graphicsShaderPipeline = new ShaderPipeline(device.getHandle());
    graphicsShaderPipeline.createVertexShader("shaders/water/water.vert.spv");
    graphicsShaderPipeline.createTessellationControlShader("shaders/water/water.tesc.spv");
    graphicsShaderPipeline.createTessellationEvaluationShader("shaders/water/water.tese.spv");
    graphicsShaderPipeline.createGeometryShader("shaders/water/water.geom.spv");
    graphicsShaderPipeline.createFragmentShader("shaders/water/water.frag.spv");
    graphicsShaderPipeline.createShaderPipeline();

    ShaderPipeline wireframeShaderPipeline = new ShaderPipeline(device.getHandle());
    wireframeShaderPipeline.createVertexShader("shaders/water/water.vert.spv");
    wireframeShaderPipeline.createTessellationControlShader("shaders/water/water.tesc.spv");
    wireframeShaderPipeline.createTessellationEvaluationShader("shaders/water/water.tese.spv");
    wireframeShaderPipeline.createGeometryShader("shaders/water/water_wireframe.geom.spv");
    wireframeShaderPipeline.createFragmentShader("shaders/water/water_wireframe.frag.spv");
    wireframeShaderPipeline.createShaderPipeline();

    ByteBuffer ubo = memAlloc(Float.BYTES * 2);
    ubo.putFloat(0);
    ubo.putFloat(0);
    ubo.flip();

    uniformBuffer = new VkUniformBuffer(device.getHandle(), memoryProperties, ubo);

    DescriptorSetLayout descriptorSetLayout = new DescriptorSetLayout(device.getHandle(), 8);
    descriptorSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_GEOMETRY_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
    descriptorSetLayout.addLayoutBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_GEOMETRY_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
    descriptorSetLayout.addLayoutBinding(2, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_GEOMETRY_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
    descriptorSetLayout.addLayoutBinding(3, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_FRAGMENT_BIT);
    descriptorSetLayout.addLayoutBinding(4, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_FRAGMENT_BIT);
    descriptorSetLayout.addLayoutBinding(5, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_FRAGMENT_BIT);
    descriptorSetLayout.addLayoutBinding(6, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
        VK_SHADER_STAGE_FRAGMENT_BIT);
    descriptorSetLayout.addLayoutBinding(7, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
        VK_SHADER_STAGE_FRAGMENT_BIT | VK_SHADER_STAGE_GEOMETRY_BIT);
    descriptorSetLayout.create();

    DescriptorSet descriptorSet = new DescriptorSet(device.getHandle(),
        descriptorPool.getHandle(),
        descriptorSetLayout.getHandlePointer());
    descriptorSet.updateDescriptorImageBuffer(
        fft.getDyImageView().getHandle(),
        VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
        dySampler.getHandle(), 0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    descriptorSet.updateDescriptorImageBuffer(
        fft.getDxImageView().getHandle(),
        VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
        dxSampler.getHandle(), 1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    descriptorSet.updateDescriptorImageBuffer(
        fft.getDzImageView().getHandle(),
        VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
        dzSampler.getHandle(), 2, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    descriptorSet.updateDescriptorImageBuffer(
        imageView_dudv.getHandle(),
        VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
        dudvSampler.getHandle(), 3, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    descriptorSet.updateDescriptorImageBuffer(
        normalRenderer.getNormalImageView().getHandle(),
        VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
        normalSampler.getHandle(), 4, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    descriptorSet.updateDescriptorImageBuffer(
        reflectionFbo.getAttachmentImageView(Attachment.COLOR).getHandle(),
        VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
        reflectionSampler.getHandle(), 5, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    descriptorSet.updateDescriptorImageBuffer(
        refractionFbo.getAttachmentImageView(Attachment.COLOR).getHandle(),
        VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
        refractionSampler.getHandle(), 6, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    descriptorSet.updateDescriptorBuffer(uniformBuffer.getHandle(),
        ubo.limit(), 0, 7, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);

    List<DescriptorSet> descriptorSets = new ArrayList<DescriptorSet>();
    List<DescriptorSetLayout> descriptorSetLayouts = new ArrayList<DescriptorSetLayout>();

    descriptorSets.add(context.getCamera().getDescriptorSet());
    descriptorSets.add(descriptorSet);
    descriptorSetLayouts.add(context.getCamera().getDescriptorSetLayout());
    descriptorSetLayouts.add(descriptorSetLayout);

    VkVertexInput vertexInput = new VkVertexInput(VertexLayout.POS2D);

    Vec2f[] vertices = MeshGenerator.generatePatch2D4x4(128);

    ByteBuffer vertexBuffer = BufferUtil.createByteBuffer(vertices);

    VkBuffer vertexBufferObject = VkBufferHelper.createDeviceLocalBuffer(
        device.getHandle(), memoryProperties,
        device.getTransferCommandPool(Thread.currentThread().getId()).getHandle(),
        device.getTransferQueue(),
        vertexBuffer, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);

    int pushConstantsRange = Float.BYTES * 35 + Integer.BYTES * 6
        + /*offset, since some devices casuing errors*/ +Float.BYTES;

    ByteBuffer pushConstants = memAlloc(pushConstantsRange);
    pushConstants.put(BufferUtil.createByteBuffer(getWorldTransform().getWorldMatrix()));
    pushConstants.putInt(waterConfig.getUvScale());
    pushConstants.putInt(waterConfig.getTessellationFactor());
    pushConstants.putFloat(waterConfig.getTessellationSlope());
    pushConstants.putFloat(waterConfig.getTessellationShift());
    pushConstants.putFloat(waterConfig.getDisplacementScale());
    pushConstants.putInt(waterConfig.getHighDetailRange());
    pushConstants.putFloat(waterConfig.getChoppiness());
    pushConstants.putFloat(waterConfig.getKReflection());
    pushConstants.putFloat(waterConfig.getKRefraction());
    pushConstants.putInt(context.getConfig().getFrameWidth());
    pushConstants.putInt(context.getConfig().getFrameHeight());
    pushConstants.putInt(waterConfig.isDiffuse() ? 1 : 0);
    pushConstants.putFloat(waterConfig.getEmission());
    pushConstants.putFloat(waterConfig.getSpecularFactor());
    pushConstants.putFloat(waterConfig.getSpecularAmplifier());
    pushConstants.putFloat(waterConfig.getReflectionBlendFactor());
    pushConstants.putFloat(waterConfig.getBaseColor().getX());
    pushConstants.putFloat(waterConfig.getBaseColor().getY());
    pushConstants.putFloat(waterConfig.getBaseColor().getZ());
    pushConstants.putFloat(waterConfig.getFresnelFactor());
    pushConstants.putFloat(waterConfig.getCapillarStrength());
    pushConstants.putFloat(waterConfig.getCapillarDownsampling());
    pushConstants.putFloat(waterConfig.getDudvDownsampling());
    pushConstants.putFloat(waterConfig.getWindDirection().getX());
    pushConstants.putFloat(waterConfig.getWindDirection().getY());
    pushConstants.flip();

    VkPipeline graphicsPipeline = new GraphicsTessellationPipeline(device.getHandle(),
        graphicsShaderPipeline, vertexInput, VkUtil.createLongBuffer(descriptorSetLayouts),
        context.getConfig().getFrameWidth(),
        context.getConfig().getFrameHeight(),
        context.getResources().getOffScreenFbo().getRenderPass().getHandle(),
        context.getResources().getOffScreenFbo().getColorAttachmentCount(),
        context.getConfig().getMultisampling_sampleCount(),
        pushConstantsRange, VK_SHADER_STAGE_ALL_GRAPHICS,
        16);

    CommandBuffer graphicsCommandBuffer = new SecondaryDrawCmdBuffer(
        device.getHandle(), device.getGraphicsCommandPool(Thread.currentThread().threadId()).getHandle(),
        graphicsPipeline.getHandle(), graphicsPipeline.getLayoutHandle(),
        context.getResources().getOffScreenFbo().getFrameBuffer().getHandle(),
        context.getResources().getOffScreenFbo().getRenderPass().getHandle(),
        0,
        VkUtil.createLongArray(descriptorSets),
        vertexBufferObject.getHandle(),
        vertices.length,
        pushConstants, VK_SHADER_STAGE_ALL_GRAPHICS);

    VkPipeline wireframeGraphicsPipeline = new GraphicsTessellationPipeline(device.getHandle(),
        wireframeShaderPipeline, vertexInput, VkUtil.createLongBuffer(descriptorSetLayouts),
        context.getConfig().getFrameWidth(),
        context.getConfig().getFrameHeight(),
        context.getResources().getOffScreenFbo().getRenderPass().getHandle(),
        context.getResources().getOffScreenFbo().getColorAttachmentCount(),
        context.getConfig().getMultisampling_sampleCount(),
        pushConstantsRange, VK_SHADER_STAGE_ALL_GRAPHICS,
        16);

    CommandBuffer wireframeCommandBuffer = new SecondaryDrawCmdBuffer(
        device.getHandle(), device.getGraphicsCommandPool(Thread.currentThread().threadId()).getHandle(),
        wireframeGraphicsPipeline.getHandle(), wireframeGraphicsPipeline.getLayoutHandle(),
        context.getResources().getOffScreenFbo().getFrameBuffer().getHandle(),
        context.getResources().getOffScreenFbo().getRenderPass().getHandle(),
        0,
        VkUtil.createLongArray(descriptorSets),
        vertexBufferObject.getHandle(),
        vertices.length,
        pushConstants, VK_SHADER_STAGE_ALL_GRAPHICS);

    offscreenReflectionCmdBuffer = new PrimaryCmdBuffer(device.getHandle(),
        device.getGraphicsCommandPool(Thread.currentThread().getId()).getHandle());

    offscreenRefractionCmdBuffer = new PrimaryCmdBuffer(device.getHandle(),
        device.getGraphicsCommandPool(Thread.currentThread().getId()).getHandle());

    reflectionFence = new Fence(device.getHandle());
    refractionFence = new Fence(device.getHandle());

    offScreenReflectionSubmitInfo = new SubmitInfo();
    offScreenReflectionSubmitInfo.setCommandBuffers(offscreenReflectionCmdBuffer.getHandlePointer());
    offScreenReflectionSubmitInfo.setFence(reflectionFence);

    offScreenRefractionSubmitInfo = new SubmitInfo();
    offScreenRefractionSubmitInfo.setCommandBuffers(offscreenRefractionCmdBuffer.getHandlePointer());
    offScreenRefractionSubmitInfo.setFence(refractionFence);

    VkMeshData meshData = VkMeshData.builder().vertexBufferObject(vertexBufferObject)
        .vertexBuffer(vertexBuffer).build();
    VkRenderInfo mainRenderInfo = VkRenderInfo.builder().commandBuffer(graphicsCommandBuffer)
        .descriptorSets(descriptorSets).descriptorSetLayouts(descriptorSetLayouts)
        .pipeline(graphicsPipeline).build();
    VkRenderInfo wireframeRenderInfo = VkRenderInfo.builder().commandBuffer(wireframeCommandBuffer)
        .descriptorSets(descriptorSets).descriptorSetLayouts(descriptorSetLayouts)
        .pipeline(wireframeGraphicsPipeline).build();

    addComponent(NodeComponentType.MESH_DATA, meshData);
    addComponent(NodeComponentType.MAIN_RENDERINFO, mainRenderInfo);
    addComponent(NodeComponentType.WIREFRAME_RENDERINFO, wireframeRenderInfo);

    // initially render to refraction fbo due to attachment deep ocean clear color
    offscreenRefractionCmdBuffer.reset();
    offscreenRefractionCmdBuffer.record(
        refractionFbo.getRenderPass().getHandle(),
        refractionFbo.getFrameBuffer().getHandle(),
        refractionFbo.getWidth(),
        refractionFbo.getHeight(),
        refractionFbo.getColorAttachmentCount(),
        refractionFbo.getDepthAttachmentCount(),
        new Vec3f(0.1f, 0.14f, 0.15f).mul(1.4f),
        null);
    offScreenRefractionSubmitInfo.submit(
        graphicsQueue);
    refractionFence.waitForFence();
  }

  public void render() {

    fft.render();
    normalRenderer.render(VK_QUEUE_FAMILY_IGNORED);

    // render reflection
    ContextHolder.getContext().getConfig().setClipplane(clipplane);

    // mirror scene to clipplane
    Scenegraph sceneGraph = getParentObject();
    sceneGraph.getWorldTransform().setScaling(1, -1, 1);
    sceneGraph.update();
    sceneGraph.getRoot().record(offScreenReflectionRenderList);

    for (String key : offScreenReflectionRenderList.getKeySet()) {

      if (!reflectionSecondaryCmdBuffers.containsKey(key)) {
        if (offScreenReflectionRenderList.get(key).getComponents()
            .containsKey(NodeComponentType.REFLECTION_RENDERINFO)) {
          VkRenderInfo renderInfo = offScreenReflectionRenderList.get(key)
              .getComponent(NodeComponentType.REFLECTION_RENDERINFO);
          reflectionSecondaryCmdBuffers.put(key, renderInfo.getCommandBuffer());
        }
      }
    }

    // render reflection scene
    if (!reflectionSecondaryCmdBuffers.isEmpty()) {
      offscreenReflectionCmdBuffer.reset();
      offscreenReflectionCmdBuffer.record(
          reflectionFbo.getRenderPass().getHandle(),
          reflectionFbo.getFrameBuffer().getHandle(),
          reflectionFbo.getWidth(),
          reflectionFbo.getHeight(),
          reflectionFbo.getColorAttachmentCount(),
          reflectionFbo.getDepthAttachmentCount(),
          VkUtil.createPointerBuffer(reflectionSecondaryCmdBuffers.values()));
      offScreenReflectionSubmitInfo.submit(
          graphicsQueue);
    }

    reflectionFence.waitForFence();

    // antimirror scene to clipplane
    sceneGraph.getWorldTransform().setScaling(1, 1, 1);
    sceneGraph.update();
    sceneGraph.getRoot().record(offScreenRefractionRenderList);

    for (String key : offScreenRefractionRenderList.getKeySet()) {

      if (!refractionSecondaryCmdBuffers.containsKey(key)) {
        if (offScreenRefractionRenderList.get(key).getComponents()
            .containsKey(NodeComponentType.REFRACTION_RENDERINFO)) {
          VkRenderInfo renderInfo = offScreenRefractionRenderList.get(key)
              .getComponent(NodeComponentType.REFRACTION_RENDERINFO);
          refractionSecondaryCmdBuffers.put(key, renderInfo.getCommandBuffer());
        }
      }
    }

    // render refraction scene
    if (!refractionSecondaryCmdBuffers.isEmpty()) {
      offscreenRefractionCmdBuffer.reset();
      offscreenRefractionCmdBuffer.record(
          refractionFbo.getRenderPass().getHandle(),
          refractionFbo.getFrameBuffer().getHandle(),
          refractionFbo.getWidth(),
          refractionFbo.getHeight(),
          refractionFbo.getColorAttachmentCount(),
          refractionFbo.getDepthAttachmentCount(),
          waterConfig.getBaseColor().mul(2.5f),
          VkUtil.createPointerBuffer(refractionSecondaryCmdBuffers.values()));
      offScreenRefractionSubmitInfo.submit(
          graphicsQueue);
    }

    refractionFence.waitForFence();

    motion += (System.currentTimeMillis() - systemTime) * waterConfig.getWaveMotion();
    distortion += (System.currentTimeMillis() - systemTime) * waterConfig.getDistortion();
    float[] v = {motion, distortion};
    uniformBuffer.mapMemory(BufferUtil.createByteBuffer(v));
    systemTime = System.currentTimeMillis();
  }

  @Override
  public void shutdown() {

    super.shutdown();

    fft.destroy();
    normalRenderer.destroy();
    image_dudv.destroy();
    imageView_dudv.destroy();
    uniformBuffer.destroy();
    dxSampler.destroy();
    dySampler.destroy();
    dzSampler.destroy();
    dudvSampler.destroy();
    normalSampler.destroy();
    reflectionSampler.destroy();
    refractionSampler.destroy();
    offscreenReflectionCmdBuffer.destroy();
    offscreenRefractionCmdBuffer.destroy();
    refractionFence.destroy();
    reflectionFence.destroy();
    reflectionFbo.destroy();
    refractionFbo.destroy();
  }


  public class ReflectionRefractionFbo extends VkFrameBufferObject {

    public ReflectionRefractionFbo(VkDevice device,
        VkPhysicalDeviceMemoryProperties memoryProperties) {

      width = ContextHolder.getContext().getConfig().getFrameWidth() / 2;
      height = ContextHolder.getContext().getConfig().getFrameHeight() / 2;

      VkImageBundle albedoBuffer = new FrameBufferColorAttachment(device, memoryProperties, width, height,
          VK_FORMAT_R16G16B16A16_SFLOAT, 1);
      VkImageBundle depthBuffer = new FrameBufferDepthAttachment(device, memoryProperties, width, height,
          VK_FORMAT_D16_UNORM, 1);

      attachments.put(Attachment.COLOR, albedoBuffer);
      attachments.put(Attachment.DEPTH, depthBuffer);

      renderPass = new RenderPass(device);
      renderPass.addColorAttachment(0, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
          VK_FORMAT_R16G16B16A16_SFLOAT, 1, VK_IMAGE_LAYOUT_UNDEFINED,
          VK_IMAGE_LAYOUT_GENERAL);
      renderPass.addDepthAttachment(1, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
          VK_FORMAT_D16_UNORM, 1, VK_IMAGE_LAYOUT_UNDEFINED,
          VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

      renderPass.addSubpassDependency(VK_SUBPASS_EXTERNAL, 0,
          VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT,
          VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
          VK_ACCESS_MEMORY_READ_BIT,
          VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,
          VK_DEPENDENCY_BY_REGION_BIT);
      renderPass.addSubpassDependency(0, VK_SUBPASS_EXTERNAL,
          VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
          VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT,
          VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,
          VK_ACCESS_MEMORY_READ_BIT,
          VK_DEPENDENCY_BY_REGION_BIT);

      renderPass.createSubpass();
      renderPass.createRenderPass();

      depthAttachmentCount = 1;
      colorAttachmentCount = renderPass.getAttachmentCount() - depthAttachmentCount;

      LongBuffer pImageViews = memAllocLong(renderPass.getAttachmentCount());
      pImageViews.put(0, attachments.get(Attachment.COLOR).getImageView().getHandle());
      pImageViews.put(1, attachments.get(Attachment.DEPTH).getImageView().getHandle());

      frameBuffer = new VkFrameBuffer(device, width, height, 1,
          pImageViews, renderPass.getHandle());
    }
  }
}
