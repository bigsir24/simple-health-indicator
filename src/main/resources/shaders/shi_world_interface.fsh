#version 410 core

out vec4 FragColor;

uniform float uAlphaTest;

in vec4 Color;
in vec2 TexCoord;
in vec2 lightmap;

uniform vec4 uColor;
uniform sampler2D colortex;
uniform sampler2D lighttex;

void main() {
    vec4 tint = texture(lighttex, lightmap);
    FragColor = texture(colortex, TexCoord) * tint * Color * uColor;
    if (FragColor.a < uAlphaTest) {
        discard;
    }
}
