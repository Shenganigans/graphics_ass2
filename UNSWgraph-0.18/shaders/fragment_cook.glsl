out vec4 outputColor;

uniform vec4 input_color;

uniform mat4 view_matrix;

// Light properties
uniform vec3 lightPos;
uniform vec3 lightIntensity;
uniform vec3 ambientIntensity;

// Material properties
uniform vec3 ambientCoeff;
uniform vec3 diffuseCoeff;

// Torch properties
uniform float cutoff;
uniform float attenuation;
uniform vec3 torchPos;
uniform int torchOn;
uniform vec3 torchAmbientIntensity;
uniform vec3 torchLightIntensity;

uniform sampler2D tex;

in vec4 viewPosition;
in vec3 m;
in vec2 texCoordFrag;
in vec4 globalPosition;

const float PI = 3.14159265359;

float DistributionGGX(vec3 N, vec3 H, float roughness)
{
    float a      = roughness*roughness;
    float a2     = a*a;
    float NdotH  = max(dot(N, H), 0.0);
    float NdotH2 = NdotH*NdotH;

    float num   = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = PI * denom * denom;

    return num / denom;
}

float GeometrySchlickGGX(float NdotV, float roughness)
{
    float r = (roughness + 1.0);
    float k = (r*r) / 8.0;

    float num   = NdotV;
    float denom = NdotV * (1.0 - k) + k;

    return num / denom;
}
float GeometrySmith(vec3 N, vec3 V, vec3 L, float roughness)
{
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float ggx2  = GeometrySchlickGGX(NdotV, roughness);
    float ggx1  = GeometrySchlickGGX(NdotL, roughness);

    return ggx1 * ggx2;
}
vec3 fresnelSchlick(float cosTheta, vec3 F0)
{
    return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0);
}

void main() {
    vec4 light = vec4(lightPos, 1);

    // reflectance
    vec3 Lo = vec3(0);
    for(int i = 0; i < 4; i++) {
        // calculate per-light radiance
        vec4 L = normalize(lightPos[i] - globalPosition);
        vec3 H = vec3(normalize(viewPosition + L));

        float distance = length(light[i] - globalPosition);
        float attenuation = 1.0 / (distance * distance);

       // cook-torrance brdf
       float NDF = DistributionGGX(m, H, 1);
       float G   = GeometrySmith(m, vec3(viewPosition), vec3(L), 1);
       vec3 F    = fresnelSchlick(max(dot(vec4(H, 1), viewPosition), 0.0), vec3(0.04));

       vec3 kS = F;
       vec3 kD = vec3(1.0) - kS;
       kD *= 1.0;

       vec3 numerator    = NDF * G * F;
       float denominator = 4.0 * max(dot(vec4(m, 1), viewPosition), 0.0) * max(dot(vec4(m, 1), L), 0.0);
       vec3 specular     = numerator / max(denominator, 0.001);

        // add to outgoing radiance Lo
        float NdotL = max(dot(vec4(m, 1), L), 0.0);
        Lo += (kD * ambientIntensity / PI + specular) * NdotL; // * radiance
    }

    vec3 ambient = vec3(0.03) * ambientIntensity * ambientCoeff;
    vec3 color = ambient + Lo;

    color = color / (color + vec3(1.0));
    color = pow(color, vec3(1.0/2.2));

    outputColor = vec4(color, 1.0) + input_color*texture(tex, texCoordFrag);

    if (torchOn == 1) {
        // Compute the s, v and r vectors
        vec3 s = normalize(view_matrix*vec4(torchPos,1) - viewPosition).xyz;
        vec3 v = normalize(-viewPosition.xyz);
        vec3 r = normalize(reflect(-s,m));

        vec3 ambient = torchAmbientIntensity*ambientCoeff;
        vec3 diffuse = max(torchLightIntensity*diffuseCoeff*dot(m,s), 0.0);
        vec3 specular = vec3(0);

        vec4 ambientAndDiffuse = vec4(ambient + diffuse, 1);

        float spotD = dot(-s, vec3(0, 0, -1));
        float spotAtt;
        if(spotD > cutoff){
            spotAtt = pow(spotD, attenuation);
        } else {
            spotAtt=0;
        }

        vec4 lightSrc = ambientAndDiffuse*input_color*texture(tex, texCoordFrag) + vec4(specular, 1);
        outputColor += lightSrc * spotAtt;;
    }
}
