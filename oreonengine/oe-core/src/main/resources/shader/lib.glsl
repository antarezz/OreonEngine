//-----------------------//
//--- const variables ---//
//-----------------------//

const int SHADOW_MAP_RESOLUTION = #var_shadow_map_resolution;
const int SHADOW_VARIANCE_HIGH_QUALITY[5] = int[5](2,2,4,4,11);
const int SHADOW_VARIANCE_MEDIUM_QUALITY[5] = int[5](1,1,2,2,7);
const int SHADOW_VARIANCE_LOW_QUALITY[5] = int[5](1,1,2,2,5);
const int SHADOW_VARIANCE_VERY_LOW_QUALITY[5] = int[5](1,1,1,1,1);
const float ZFAR = 10000;
const float ZNEAR = 0.1;
const vec3 SUN_BASECOLOR = vec3(1.0f,0.79f,0.43f);

//-----------------------//
//--- uniform buffers ---//
//-----------------------//

layout (std140, row_major) uniform Camera{
	vec3 eyePosition;
	mat4 m_View;
	mat4 m_ViewProjection;
	vec4 frustumPlanes[6];
} camera;

layout (std140) uniform DirectionalLight{
	vec3 direction;
	float intensity;
	vec3 ambient;
	vec3 color;
} directional_light;

layout (std140, row_major) uniform DirectionalLightViewProjections{
	mat4 m_lightViewProjection[5];
	float splitRange[4];
} directional_light_matrices;

//-----------------------//
//------- methods -------//
//-----------------------//

/*vec3 blurRgba16f(ivec2 computeCoord, int kernels, layout(rgba16f) image2D vImage)
{
	vec3 rgb = vec3(0,0,0);
	
	for (int i=-kernels; i<=kernels; i++){
		for (int j=-kernels; j<=kernels; j++){
			rgb += imageLoad(vImage, computeCoord + ivec2(i,j)).rgb;  
		}
	}

	rgb *= 1/ pow(kernels*2+1,2);
	
	return rgb;
}*/

float linearizeDepth(float depth)
{
	return (2 * ZNEAR) / (ZFAR + ZNEAR - depth * (ZFAR - ZNEAR));
}

float specular(vec3 direction, vec3 normal, float specularFactor, float emissionFactor, vec3 vertexToEye)
{
	vec3 reflectionVector = normalize(reflect(direction, normalize(normal)));
	float specular = max(0.0, dot(vertexToEye, reflectionVector));
	
	return pow(specular, specularFactor) * emissionFactor;
}

float specular(vec3 direction, vec3 normal, vec3 eyePosition, vec3 vertexPosition, float specularFactor, float emissionFactor)
{
	vec3 reflectionVector = normalize(reflect(direction, normal));
	vec3 vertexToEye = normalize(eyePosition - vertexPosition);
	float specular = max(0.0, dot(vertexToEye, reflectionVector));
	
	return pow(specular, specularFactor) * emissionFactor;
}

float diffuse(vec3 direction, vec3 normal, float intensity)
{
	return max(0.0, dot(normal, -direction) * intensity);
}

float diffuse(vec3 direction, vec3 normal, float intensity, float minDiffuseFactor)
{
	return max(minDiffuseFactor, dot(normal, -direction) * intensity);
}

float getFogFactor(float dist, float sightRangeFactor)
{
	return smoothstep(0,1,-0.0002/sightRangeFactor*(dist-(ZFAR)/10*sightRangeFactor) + 1);
}

float distancePointPlane(vec3 point, vec4 plane)
{
	return abs(plane.x*point.x + plane.y*point.y + plane.z*point.z + plane.w) / 
		   abs(sqrt(plane.x * plane.x + plane.y * plane.y + plane.z * plane.z));
}

float getLodfactor(float dist, int tessFactor, float tessSlope, float tessShift)
{
	float tessLevel = max(0.0,tessFactor/(pow(dist, tessSlope)) - tessShift);
	return tessLevel;
}

vec3 getTangent(vec3 v0, vec3 v1, vec3 v2, vec2 uv0, vec2 uv1, vec2 uv2)
{	
	// edges of the face/triangle
    vec3 e1 = v1 - v0;
    vec3 e2 = v2 - v0;

    vec2 deltaUV1 = uv1 - uv0;
	vec2 deltaUV2 = uv2 - uv0;
	
	float r = 1.0 / (deltaUV1.x * deltaUV2.y - deltaUV1.y * deltaUV2.x);
	
	return normalize((e1 * deltaUV2.y - e2 * deltaUV1.y)*r);
}
