uniform sampler2D tex;
 
bool isWhite(vec4 c) {
	return true;
}
 
void main(void){	
	vec2 texPos = gl_TexCoord[0].xy;
    vec4 texColor = texture2D(tex, texPos);

	vec2 pos = gl_FragCoord.xy;
	float br = 1f;
	gl_FragColor = texColor * vec4(br, br, br, 1.0f);
 
}
