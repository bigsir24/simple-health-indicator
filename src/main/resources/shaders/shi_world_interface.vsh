#version 410 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec4 aColor;
layout (location = 2) in vec2 aUV;
layout (location = 3) in float aLightmap;

out vec4 Color;
out vec2 TexCoord;
out vec2 lightmap;

uniform mat4 model;
layout (std140) uniform Matrices {
    mat4 projection;
    mat4 projectionInv;
    mat4 view;
    mat4 viewInv;
} matrices;

vec2 unpackLightCoord() {
    int asInt = int(aLightmap);
    return vec2(((asInt & 0xF) + 0.5)/16.0, (((asInt & 0xF0) >> 4) + 0.5)/16.0);
}

void main() {
    gl_Position = matrices.projection * matrices.view * model * vec4(aPos, 1);

    TexCoord = aUV;
    Color = aColor;
    lightmap = unpackLightCoord();
}
