#include <jni.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES3/gl3.h>
#include <algorithm>
#include <iomanip>
#include <sstream>
#include <string>
#include <vector>

static std::string esc(const std::string& s) {
    std::ostringstream o;
    for (unsigned char c : s) {
        switch (c) {
            case '"': o << "\\\""; break;
            case '\\': o << "\\\\"; break;
            case '\b': o << "\\b"; break;
            case '\f': o << "\\f"; break;
            case '\n': o << "\\n"; break;
            case '\r': o << "\\r"; break;
            case '\t': o << "\\t"; break;
            default:
                if (c < 0x20) o << "\\u" << std::hex << std::setw(4) << std::setfill('0') << static_cast<int>(c) << std::dec;
                else o << c;
        }
    }
    return o.str();
}

static std::string q(const char* s) { return std::string("\"") + esc(s ? s : "Unknown") + "\""; }
static std::string q(const std::string& s) { return std::string("\"") + esc(s) + "\""; }
static std::string hexv(GLint v) { std::ostringstream o; o << "0x" << std::uppercase << std::hex << static_cast<unsigned int>(v); return o.str(); }

static void addInt(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLint v = 0;
    glGetError();
    glGetIntegerv(e, &v);
    if (glGetError() != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(std::to_string(v)) << "}";
}

static void addInt2(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLint v[2] = {0, 0};
    glGetError();
    glGetIntegerv(e, v);
    if (glGetError() != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(std::to_string(v[0]) + " × " + std::to_string(v[1])) << "}";
}

static std::vector<std::string> splitExt(const char* s) {
    std::vector<std::string> out;
    if (!s) return out;
    std::istringstream in(s);
    std::string x;
    while (in >> x) out.push_back(x);
    return out;
}

static std::vector<std::string> glExtensions() {
    std::vector<std::string> out;
    GLint n = 0;
    glGetError();
    glGetIntegerv(GL_NUM_EXTENSIONS, &n);
    if (glGetError() == GL_NO_ERROR && n > 0) {
        out.reserve(static_cast<size_t>(n));
        for (GLint i = 0; i < n; ++i) {
            const auto* s = reinterpret_cast<const char*>(glGetStringi(GL_EXTENSIONS, static_cast<GLuint>(i)));
            if (s) out.emplace_back(s);
        }
    } else {
        out = splitExt(reinterpret_cast<const char*>(glGetString(GL_EXTENSIONS)));
    }
    std::sort(out.begin(), out.end());
    out.erase(std::unique(out.begin(), out.end()), out.end());
    return out;
}

static void appendStringArray(std::ostringstream& o, const std::vector<std::string>& v) {
    o << '[';
    for (size_t i = 0; i < v.size(); ++i) {
        if (i) o << ',';
        o << q(v[i]);
    }
    o << ']';
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_efishell_openglesscope_MainActivity_nativeCollect(JNIEnv* env, jobject) {
    EGLDisplay d = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (d == EGL_NO_DISPLAY) return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL display unavailable\"}");
    EGLint maj = 0, min = 0;
    if (eglInitialize(d, &maj, &min) != EGL_TRUE) return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"eglInitialize failed\"}");
    const EGLint cfgAttrs3[] = {EGL_SURFACE_TYPE, EGL_PBUFFER_BIT, EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT_KHR, EGL_RED_SIZE, 8, EGL_GREEN_SIZE, 8, EGL_BLUE_SIZE, 8, EGL_ALPHA_SIZE, 8, EGL_NONE};
    const EGLint cfgAttrs2[] = {EGL_SURFACE_TYPE, EGL_PBUFFER_BIT, EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL_RED_SIZE, 8, EGL_GREEN_SIZE, 8, EGL_BLUE_SIZE, 8, EGL_ALPHA_SIZE, 8, EGL_NONE};
    EGLConfig cfg = nullptr;
    EGLint cfgCount = 0;
    if (eglChooseConfig(d, cfgAttrs3, &cfg, 1, &cfgCount) != EGL_TRUE || cfgCount < 1) {
        cfgCount = 0;
        if (eglChooseConfig(d, cfgAttrs2, &cfg, 1, &cfgCount) != EGL_TRUE || cfgCount < 1) {
            eglTerminate(d);
            return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"No compatible EGL pbuffer configuration\"}");
        }
    }
    const EGLint pbAttrs[] = {EGL_WIDTH, 16, EGL_HEIGHT, 16, EGL_NONE};
    EGLSurface s = eglCreatePbufferSurface(d, cfg, pbAttrs);
    if (s == EGL_NO_SURFACE) {
        eglTerminate(d);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL pbuffer creation failed\"}");
    }
    EGLContext c = EGL_NO_CONTEXT;
    const std::string displayExtensions = eglQueryString(d, EGL_EXTENSIONS) ? eglQueryString(d, EGL_EXTENSIONS) : "";
    if (displayExtensions.find("EGL_KHR_create_context") != std::string::npos) {
        const EGLint c32[] = {EGL_CONTEXT_MAJOR_VERSION_KHR, 3, EGL_CONTEXT_MINOR_VERSION_KHR, 2, EGL_NONE};
        const EGLint c31[] = {EGL_CONTEXT_MAJOR_VERSION_KHR, 3, EGL_CONTEXT_MINOR_VERSION_KHR, 1, EGL_NONE};
        c = eglCreateContext(d, cfg, EGL_NO_CONTEXT, c32);
        if (c == EGL_NO_CONTEXT) c = eglCreateContext(d, cfg, EGL_NO_CONTEXT, c31);
    }
    const EGLint c3[] = {EGL_CONTEXT_CLIENT_VERSION, 3, EGL_NONE};
    const EGLint c2[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
    if (c == EGL_NO_CONTEXT) c = eglCreateContext(d, cfg, EGL_NO_CONTEXT, c3);
    if (c == EGL_NO_CONTEXT) c = eglCreateContext(d, cfg, EGL_NO_CONTEXT, c2);
    if (c == EGL_NO_CONTEXT || eglMakeCurrent(d, s, s, c) != EGL_TRUE) {
        if (c != EGL_NO_CONTEXT) eglDestroyContext(d, c);
        eglDestroySurface(d, s);
        eglTerminate(d);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"OpenGL ES context creation failed\"}");
    }

    const char* glVendor = reinterpret_cast<const char*>(glGetString(GL_VENDOR));
    const char* glRenderer = reinterpret_cast<const char*>(glGetString(GL_RENDERER));
    const char* glVersion = reinterpret_cast<const char*>(glGetString(GL_VERSION));
    const char* glslVersion = reinterpret_cast<const char*>(glGetString(GL_SHADING_LANGUAGE_VERSION));
    const char* eglVendor = eglQueryString(d, EGL_VENDOR);
    const char* eglVersion = eglQueryString(d, EGL_VERSION);
    const char* eglApis = eglQueryString(d, EGL_CLIENT_APIS);
    auto glExt = glExtensions();
    auto eglExt = splitExt(eglQueryString(d, EGL_EXTENSIONS));
    std::sort(eglExt.begin(), eglExt.end());
    eglExt.erase(std::unique(eglExt.begin(), eglExt.end()), eglExt.end());

    std::ostringstream o;
    o << "{\"status\":\"available\",\"renderer\":" << q(glRenderer) << ",\"vendor\":" << q(glVendor)
      << ",\"glVersion\":" << q(glVersion) << ",\"glslVersion\":" << q(glslVersion)
      << ",\"egl\":{\"vendor\":" << q(eglVendor) << ",\"version\":" << q(eglVersion)
      << ",\"initializedVersion\":" << q(std::to_string(maj) + "." + std::to_string(min)) << ",\"clientApis\":" << q(eglApis) << ",\"extensions\":";
    appendStringArray(o, eglExt);
    o << "},\"extensions\":";
    appendStringArray(o, glExt);

    o << ",\"limits\":[";
    bool first = true;
    addInt(o, first, "GL_MAX_TEXTURE_SIZE", GL_MAX_TEXTURE_SIZE);
    addInt(o, first, "GL_MAX_CUBE_MAP_TEXTURE_SIZE", GL_MAX_CUBE_MAP_TEXTURE_SIZE);
    addInt(o, first, "GL_MAX_RENDERBUFFER_SIZE", GL_MAX_RENDERBUFFER_SIZE);
    addInt2(o, first, "GL_MAX_VIEWPORT_DIMS", GL_MAX_VIEWPORT_DIMS);
    addInt(o, first, "GL_MAX_VERTEX_ATTRIBS", GL_MAX_VERTEX_ATTRIBS);
    addInt(o, first, "GL_MAX_VERTEX_UNIFORM_VECTORS", GL_MAX_VERTEX_UNIFORM_VECTORS);
    addInt(o, first, "GL_MAX_FRAGMENT_UNIFORM_VECTORS", GL_MAX_FRAGMENT_UNIFORM_VECTORS);
    addInt(o, first, "GL_MAX_VARYING_VECTORS", GL_MAX_VARYING_VECTORS);
    addInt(o, first, "GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS", GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);
    addInt(o, first, "GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS", GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS);
    addInt(o, first, "GL_MAX_TEXTURE_IMAGE_UNITS", GL_MAX_TEXTURE_IMAGE_UNITS);
    addInt(o, first, "GL_MAX_SAMPLES", GL_MAX_SAMPLES);
    addInt(o, first, "GL_MAX_COLOR_ATTACHMENTS", GL_MAX_COLOR_ATTACHMENTS);
    addInt(o, first, "GL_MAX_DRAW_BUFFERS", GL_MAX_DRAW_BUFFERS);
    addInt(o, first, "GL_MAX_3D_TEXTURE_SIZE", GL_MAX_3D_TEXTURE_SIZE);
    addInt(o, first, "GL_MAX_ARRAY_TEXTURE_LAYERS", GL_MAX_ARRAY_TEXTURE_LAYERS);
    addInt(o, first, "GL_MAX_UNIFORM_BUFFER_BINDINGS", GL_MAX_UNIFORM_BUFFER_BINDINGS);
    addInt(o, first, "GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS", GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS);
    o << ']';

    GLint nfmt = 0;
    glGetError();
    glGetIntegerv(GL_NUM_COMPRESSED_TEXTURE_FORMATS, &nfmt);
    std::vector<GLint> fmts;
    if (glGetError() == GL_NO_ERROR && nfmt > 0 && nfmt < 4096) {
        fmts.resize(static_cast<size_t>(nfmt));
        glGetIntegerv(GL_COMPRESSED_TEXTURE_FORMATS, fmts.data());
    }
    o << ",\"compressedFormats\":[";
    for (size_t i = 0; i < fmts.size(); ++i) { if (i) o << ','; o << q(hexv(fmts[i])); }
    o << ']';

    const GLenum shaders[] = {GL_VERTEX_SHADER, GL_FRAGMENT_SHADER};
    const char* shaderNames[] = {"GL_VERTEX_SHADER", "GL_FRAGMENT_SHADER"};
    const GLenum precisions[] = {GL_LOW_FLOAT, GL_MEDIUM_FLOAT, GL_HIGH_FLOAT, GL_LOW_INT, GL_MEDIUM_INT, GL_HIGH_INT};
    const char* precisionNames[] = {"GL_LOW_FLOAT", "GL_MEDIUM_FLOAT", "GL_HIGH_FLOAT", "GL_LOW_INT", "GL_MEDIUM_INT", "GL_HIGH_INT"};
    o << ",\"precision\":[";
    bool pf = true;
    for (int si = 0; si < 2; ++si) {
        for (int pi = 0; pi < 6; ++pi) {
            GLint range[2] = {0,0}, precision = 0;
            glGetShaderPrecisionFormat(shaders[si], precisions[pi], range, &precision);
            if (!pf) o << ',';
            pf = false;
            o << "{\"shader\":" << q(shaderNames[si]) << ",\"type\":" << q(precisionNames[pi]) << ",\"rangeMin\":" << range[0] << ",\"rangeMax\":" << range[1] << ",\"precision\":" << precision << '}';
        }
    }
    o << ']';

    EGLint totalConfigs = 0;
    eglGetConfigs(d, nullptr, 0, &totalConfigs);
    std::vector<EGLConfig> configs(static_cast<size_t>(std::max(0, totalConfigs)));
    if (totalConfigs > 0) eglGetConfigs(d, configs.data(), totalConfigs, &totalConfigs);
    o << ",\"eglConfigs\":[";
    for (EGLint i = 0; i < totalConfigs; ++i) {
        if (i) o << ',';
        auto ga = [&](EGLint attr) { EGLint v = 0; eglGetConfigAttrib(d, configs[static_cast<size_t>(i)], attr, &v); return v; };
        o << "{\"id\":" << ga(EGL_CONFIG_ID) << ",\"rgba\":" << q(std::to_string(ga(EGL_RED_SIZE))+"/"+std::to_string(ga(EGL_GREEN_SIZE))+"/"+std::to_string(ga(EGL_BLUE_SIZE))+"/"+std::to_string(ga(EGL_ALPHA_SIZE)))
          << ",\"depth\":" << ga(EGL_DEPTH_SIZE) << ",\"stencil\":" << ga(EGL_STENCIL_SIZE) << ",\"samples\":" << ga(EGL_SAMPLES)
          << ",\"surfaceType\":" << q(hexv(ga(EGL_SURFACE_TYPE))) << ",\"renderableType\":" << q(hexv(ga(EGL_RENDERABLE_TYPE))) << '}';
    }
    o << "]}";

    eglMakeCurrent(d, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    eglDestroyContext(d, c);
    eglDestroySurface(d, s);
    eglTerminate(d);
    const std::string result = o.str();
    return env->NewStringUTF(result.c_str());
}
