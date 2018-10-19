
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
uniform float phongExp;

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

void main()
{
    vec3 light = vec3(lightPos[0], 0, 0);
    // Compute the s, v and r vectors
    vec3 s = normalize(view_matrix*vec4(light,1) - viewPosition).xyz;
    vec3 v = normalize(-viewPosition.xyz);
    vec3 r = normalize(reflect(-s,m));

    vec3 ambient = ambientIntensity*ambientCoeff;
    vec3 diffuse = max(lightIntensity*diffuseCoeff*dot(m,s), 0.0);
    vec3 specular = vec3(0);

    vec4 ambientAndDiffuse = vec4(ambient + diffuse, 1);

    outputColor = ambientAndDiffuse*input_color*texture(tex, texCoordFrag) + vec4(specular, 1);

    vec4 torchOutputColor = vec4(0);

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
        torchOutputColor = lightSrc * spotAtt;
        outputColor += torchOutputColor;
    }
}
